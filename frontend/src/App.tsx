import { useEffect, useMemo, useState } from 'react'
import type { FormEvent } from 'react'
import { api } from './api'
import './App.css'
import type { TabKey, TeamInfo, UserInfo } from './types'

interface AuthState {
  uid: number
  token: string
  email: string
}

const AUTH_STORAGE_KEY = 'teamup.auth'

function parseIdList(raw?: string): number[] {
  if (!raw) {
    return []
  }
  return raw
    .split(';')
    .map((item) => item.trim())
    .filter((item) => item.length > 0)
    .map((item) => Number(item))
    .filter((item) => Number.isFinite(item))
}

function stateLabel(state: number): string {
  if (state === 0) {
    return '待处理'
  }
  if (state === 1) {
    return '已通过'
  }
  if (state === 2) {
    return '已拒绝'
  }
  return `未知(${state})`
}

function loadAuth(): AuthState | null {
  const raw = localStorage.getItem(AUTH_STORAGE_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw) as AuthState
  } catch {
    return null
  }
}

function App() {
  const [auth, setAuth] = useState<AuthState | null>(() => loadAuth())
  const [activeTab, setActiveTab] = useState<TabKey>('plaza')
  const [teams, setTeams] = useState<TeamInfo[]>([])
  const [selectedTeamId, setSelectedTeamId] = useState<number | null>(null)
  const [selectedTeam, setSelectedTeam] = useState<TeamInfo | null>(null)
  const [profile, setProfile] = useState<UserInfo | null>(null)
  const [loading, setLoading] = useState(false)
  const [message, setMessage] = useState<string>('')
  const [error, setError] = useState<string>('')

  const [loginEmail, setLoginEmail] = useState('')
  const [loginPassword, setLoginPassword] = useState('')
  const [regName, setRegName] = useState('')
  const [regEmail, setRegEmail] = useState('')
  const [regPassword, setRegPassword] = useState('')
  const [keyword, setKeyword] = useState('')

  const [newTeamName, setNewTeamName] = useState('')
  const [newTeamCourse, setNewTeamCourse] = useState('')
  const [newTeamLimit, setNewTeamLimit] = useState('4')
  const [newTeamContent, setNewTeamContent] = useState('')

  const [newComment, setNewComment] = useState('')
  const [applyMessage, setApplyMessage] = useState('')

  const myTeamIds = useMemo(() => parseIdList(profile?.user?.teams), [profile?.user?.teams])
  const isTeamCreator = useMemo(() => {
    if (!auth || !selectedTeam) {
      return false
    }
    return selectedTeam.team.creatorId === auth.uid
  }, [auth, selectedTeam])

  async function withLoading(task: () => Promise<void>) {
    setLoading(true)
    setError('')
    setMessage('')
    try {
      await task()
    } catch (err) {
      setError(err instanceof Error ? err.message : '请求失败')
    } finally {
      setLoading(false)
    }
  }

  function saveAuth(nextAuth: AuthState | null) {
    setAuth(nextAuth)
    if (nextAuth) {
      localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(nextAuth))
      return
    }
    localStorage.removeItem(AUTH_STORAGE_KEY)
  }

  async function loadTeams() {
    await withLoading(async () => {
      const data = await api.getTeams()
      setTeams(data)
      if (selectedTeamId !== null) {
        const found = data.find((item) => item.team.id === selectedTeamId)
        if (!found) {
          setSelectedTeamId(null)
          setSelectedTeam(null)
        }
      }
    })
  }

  async function loadTeamDetail(id: number) {
    await withLoading(async () => {
      const detail = await api.getTeam(id)
      setSelectedTeam(detail)
    })
  }

  async function loadProfile() {
    if (!auth) {
      setProfile(null)
      return
    }
    await withLoading(async () => {
      const data = await api.getProfile(auth.uid)
      setProfile(data)
    })
  }

  useEffect(() => {
    void loadTeams()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    if (selectedTeamId === null) {
      return
    }
    void loadTeamDetail(selectedTeamId)
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [selectedTeamId])

  useEffect(() => {
    if (activeTab === 'profile' && auth) {
      void loadProfile()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [activeTab, auth?.uid])

  async function onLogin(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    await withLoading(async () => {
      const data = await api.login({ email: loginEmail.trim(), password: loginPassword })
      saveAuth({ uid: data.uid, token: data.token, email: loginEmail.trim() })
      setLoginPassword('')
      setMessage('登录成功')
      await loadTeams()
    })
  }

  async function onRegister(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    await withLoading(async () => {
      await api.register({ name: regName.trim(), email: regEmail.trim(), password: regPassword })
      setMessage('注册成功，请登录')
      setRegPassword('')
    })
  }

  async function onLogout() {
    await withLoading(async () => {
      if (auth) {
        await api.logout(auth.token)
      }
      saveAuth(null)
      setProfile(null)
      setSelectedTeam(null)
      setSelectedTeamId(null)
      setActiveTab('plaza')
      setMessage('已退出登录')
    })
  }

  async function onSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    await withLoading(async () => {
      if (keyword.trim().length === 0) {
        const data = await api.getTeams()
        setTeams(data)
        setMessage('已显示全部队伍')
        return
      }
      const data = await api.searchTeams(keyword.trim())
      setTeams(data)
      setMessage(`搜索到 ${data.length} 条队伍`)
    })
  }

  async function onCreateTeam(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!auth) {
      setError('请先登录')
      return
    }
    await withLoading(async () => {
      await api.createTeam(auth.token, {
        team: {
          creatorId: auth.uid,
          name: newTeamName.trim(),
        },
        info: {
          course: newTeamCourse.trim(),
          numberLimit: Number(newTeamLimit),
          content: newTeamContent.trim(),
        },
      })
      setMessage('创建队伍成功')
      setNewTeamName('')
      setNewTeamCourse('')
      setNewTeamLimit('4')
      setNewTeamContent('')
      setActiveTab('plaza')
      await loadTeams()
    })
  }

  async function onSendComment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!auth || !selectedTeam) {
      setError('请先登录并选择队伍')
      return
    }
    await withLoading(async () => {
      await api.createComment(auth.token, {
        senderId: auth.uid,
        teamId: selectedTeam.team.id,
        content: newComment.trim(),
      })
      setNewComment('')
      setMessage('评论已发送')
      await loadTeamDetail(selectedTeam.team.id)
    })
  }

  async function onApplyTeam(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!auth || !selectedTeam) {
      setError('请先登录并选择队伍')
      return
    }
    await withLoading(async () => {
      await api.createApplication(auth.token, {
        uid: auth.uid,
        tid: selectedTeam.team.id,
        msg: applyMessage.trim(),
      })
      setApplyMessage('')
      setMessage('申请已提交')
      await loadTeamDetail(selectedTeam.team.id)
      await loadProfile()
    })
  }

  async function onReviewApplication(applicationId: number, state: 1 | 2) {
    if (!auth || !selectedTeam) {
      return
    }
    await withLoading(async () => {
      await api.updateApplication(auth.token, { id: applicationId, state })
      setMessage(state === 1 ? '已通过申请' : '已拒绝申请')
      await loadTeamDetail(selectedTeam.team.id)
    })
  }

  return (
    <div className="page">
      <header className="header">
        <h1>TeamUp 前端</h1>
        <p>课程组队平台（已对接当前后端接口）</p>
      </header>

      <nav className="tabs">
        <button
          className={activeTab === 'plaza' ? 'tab active' : 'tab'}
          onClick={() => setActiveTab('plaza')}
        >
          队伍广场
        </button>
        <button
          className={activeTab === 'create' ? 'tab active' : 'tab'}
          onClick={() => setActiveTab('create')}
        >
          创建队伍
        </button>
        <button
          className={activeTab === 'profile' ? 'tab active' : 'tab'}
          onClick={() => setActiveTab('profile')}
        >
          个人中心
        </button>
      </nav>

      {message && <div className="notice ok">{message}</div>}
      {error && <div className="notice err">{error}</div>}

      <div className="layout">
        <aside className="panel">
          <h2>账号</h2>
          {auth ? (
            <div className="account">
              <p>当前用户：UID {auth.uid}</p>
              <p>邮箱：{auth.email}</p>
              <button className="danger" onClick={() => void onLogout()} disabled={loading}>
                退出登录
              </button>
            </div>
          ) : (
            <>
              <form className="form" onSubmit={(event) => void onLogin(event)}>
                <h3>登录</h3>
                <input
                  placeholder="邮箱"
                  value={loginEmail}
                  onChange={(event) => setLoginEmail(event.target.value)}
                  required
                />
                <input
                  placeholder="密码"
                  type="password"
                  value={loginPassword}
                  onChange={(event) => setLoginPassword(event.target.value)}
                  required
                />
                <button type="submit" disabled={loading}>
                  登录
                </button>
              </form>

              <form className="form" onSubmit={(event) => void onRegister(event)}>
                <h3>注册</h3>
                <input
                  placeholder="用户名（可选）"
                  value={regName}
                  onChange={(event) => setRegName(event.target.value)}
                />
                <input
                  placeholder="邮箱"
                  value={regEmail}
                  onChange={(event) => setRegEmail(event.target.value)}
                  required
                />
                <input
                  placeholder="密码"
                  type="password"
                  value={regPassword}
                  onChange={(event) => setRegPassword(event.target.value)}
                  required
                />
                <button type="submit" disabled={loading}>
                  注册
                </button>
              </form>
            </>
          )}
        </aside>

        <main className="panel main">
          {activeTab === 'plaza' && (
            <>
              <form className="inline-form" onSubmit={(event) => void onSearch(event)}>
                <input
                  placeholder="按简介关键字搜索队伍"
                  value={keyword}
                  onChange={(event) => setKeyword(event.target.value)}
                />
                <button type="submit" disabled={loading}>
                  搜索
                </button>
                <button type="button" onClick={() => void loadTeams()} disabled={loading}>
                  刷新
                </button>
              </form>

              <div className="team-grid">
                <section>
                  <h2>队伍列表（{teams.length}）</h2>
                  {teams.map((item) => (
                    <article key={item.team.id} className="card">
                      <h3>{item.team.name}</h3>
                      <p>队伍 ID：{item.team.id}</p>
                      <p>创建者 UID：{item.team.creatorId}</p>
                      <p>课程：{item.info?.course || '-'}</p>
                      <p>人数上限：{item.info?.numberLimit ?? '-'}</p>
                      <p className="multiline">{item.info?.content || '暂无简介'}</p>
                      <button
                        onClick={() => setSelectedTeamId(item.team.id)}
                        disabled={loading}
                      >
                        查看详情
                      </button>
                    </article>
                  ))}
                </section>

                <section>
                  <h2>队伍详情</h2>
                  {!selectedTeam && <p>请选择左侧队伍查看详情</p>}
                  {selectedTeam && (
                    <div className="detail">
                      <h3>
                        {selectedTeam.team.name}（ID: {selectedTeam.team.id}）
                      </h3>
                      <p>创建者 UID：{selectedTeam.team.creatorId}</p>
                      <p>课程：{selectedTeam.info?.course || '-'}</p>
                      <p>人数上限：{selectedTeam.info?.numberLimit ?? '-'}</p>
                      <p className="multiline">{selectedTeam.info?.content || '暂无简介'}</p>

                      <h4>评论（{selectedTeam.commentList.length}）</h4>
                      <ul>
                        {selectedTeam.commentList.map((item) => (
                          <li key={item.id}>
                            <strong>UID {item.senderId}：</strong>
                            {item.content}
                          </li>
                        ))}
                      </ul>

                      {auth && (
                        <form className="inline-form" onSubmit={(event) => void onSendComment(event)}>
                          <input
                            placeholder="输入评论内容"
                            value={newComment}
                            onChange={(event) => setNewComment(event.target.value)}
                            required
                          />
                          <button type="submit" disabled={loading}>
                            发布评论
                          </button>
                        </form>
                      )}

                      <h4>申请（{selectedTeam.applicationList.length}）</h4>
                      <ul>
                        {selectedTeam.applicationList.map((item) => (
                          <li key={item.id}>
                            <span>申请ID {item.id} / 申请人 UID {item.uid} / 状态：{stateLabel(item.state)}</span>
                            <span className="multiline">{item.msg}</span>
                            {isTeamCreator && item.state === 0 && (
                              <span className="row-actions">
                                <button
                                  onClick={() => void onReviewApplication(item.id, 1)}
                                  disabled={loading}
                                >
                                  通过
                                </button>
                                <button
                                  onClick={() => void onReviewApplication(item.id, 2)}
                                  disabled={loading}
                                >
                                  拒绝
                                </button>
                              </span>
                            )}
                          </li>
                        ))}
                      </ul>

                      {auth && !isTeamCreator && (
                        <form className="inline-form" onSubmit={(event) => void onApplyTeam(event)}>
                          <input
                            placeholder="申请留言（可选）"
                            value={applyMessage}
                            onChange={(event) => setApplyMessage(event.target.value)}
                          />
                          <button type="submit" disabled={loading}>
                            申请加入
                          </button>
                        </form>
                      )}
                    </div>
                  )}
                </section>
              </div>
            </>
          )}

          {activeTab === 'create' && (
            <section>
              <h2>创建队伍</h2>
              {!auth && <p>请先登录后创建队伍。</p>}
              {auth && (
                <form className="form create" onSubmit={(event) => void onCreateTeam(event)}>
                  <input
                    placeholder="队伍名称"
                    value={newTeamName}
                    onChange={(event) => setNewTeamName(event.target.value)}
                    required
                  />
                  <input
                    placeholder="课程名称"
                    value={newTeamCourse}
                    onChange={(event) => setNewTeamCourse(event.target.value)}
                    required
                  />
                  <input
                    placeholder="人数上限"
                    value={newTeamLimit}
                    onChange={(event) => setNewTeamLimit(event.target.value)}
                    type="number"
                    min={2}
                    max={20}
                    required
                  />
                  <textarea
                    placeholder="队伍简介"
                    value={newTeamContent}
                    onChange={(event) => setNewTeamContent(event.target.value)}
                    required
                  />
                  <button type="submit" disabled={loading}>
                    创建
                  </button>
                </form>
              )}
            </section>
          )}

          {activeTab === 'profile' && (
            <section>
              <h2>个人中心</h2>
              {!auth && <p>请先登录后查看个人中心。</p>}
              {auth && (
                <>
                  <button type="button" onClick={() => void loadProfile()} disabled={loading}>
                    刷新个人数据
                  </button>
                  {!profile && <p>暂无数据</p>}
                  {profile && (
                    <div className="detail">
                      <p>用户ID：{profile.user.id}</p>
                      <p>用户名：{profile.user.name}</p>
                      <p>邮箱：{profile.user.email}</p>
                      <p>我加入的队伍ID：{myTeamIds.length > 0 ? myTeamIds.join(', ') : '暂无'}</p>

                      <h4>我的申请</h4>
                      <ul>
                        {profile.applicationList.map((item) => (
                          <li key={item.id}>
                            队伍 {item.tid} / 状态：{stateLabel(item.state)} / 留言：{item.msg}
                          </li>
                        ))}
                      </ul>

                      <h4>我的评论</h4>
                      <ul>
                        {profile.commentList.map((item) => (
                          <li key={item.id}>
                            队伍 {item.teamId} / {item.content}
                          </li>
                        ))}
                      </ul>
                    </div>
                  )}
                </>
              )}
            </section>
          )}
        </main>
      </div>
    </div>
  )
}

export default App
