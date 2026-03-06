import type {
  ApiResult,
  Application,
  Info,
  LoginPayload,
  TeamInfo,
  User,
  UserInfo,
} from './types'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080'

type HttpMethod = 'GET' | 'POST' | 'PUT' | 'DELETE'

interface RequestOptions {
  method?: HttpMethod
  token?: string
  body?: unknown
}

function isErrorCode(code: number): boolean {
  return code % 10 === 0
}

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = 'GET', token, body } = options
  const headers: Record<string, string> = {}
  if (body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  })

  let payload: ApiResult<T> | null = null
  try {
    payload = (await response.json()) as ApiResult<T>
  } catch {
    // ignore JSON parse error and throw below
  }

  if (!response.ok) {
    throw new Error(payload?.msg || `请求失败（HTTP ${response.status}）`)
  }
  if (!payload) {
    throw new Error('服务端返回了空响应')
  }
  if (isErrorCode(payload.code)) {
    throw new Error(payload.msg || '请求失败')
  }
  return payload.data
}

export const api = {
  register(user: Pick<User, 'name' | 'email'> & { password: string }) {
    return request<boolean>('/users', { method: 'POST', body: user })
  },
  login(payload: { email: string; password: string }) {
    return request<LoginPayload>('/login', { method: 'POST', body: payload })
  },
  logout(token: string) {
    return request<boolean>('/logout', { method: 'POST', token })
  },
  getTeams() {
    return request<TeamInfo[]>('/teams')
  },
  searchTeams(keyword: string) {
    return request<TeamInfo[]>('/info/search', {
      method: 'POST',
      body: { content: keyword },
    })
  },
  getTeam(id: number) {
    return request<TeamInfo>(`/teams/${id}`)
  },
  createTeam(token: string, payload: { team: { creatorId: number; name: string }; info: Info }) {
    return request<boolean>('/teams', {
      method: 'POST',
      token,
      body: payload,
    })
  },
  createComment(token: string, payload: { senderId: number; teamId: number; content: string }) {
    return request<boolean>('/comments', {
      method: 'POST',
      token,
      body: payload,
    })
  },
  createApplication(token: string, payload: { uid: number; tid: number; msg: string }) {
    return request<boolean>('/applications', {
      method: 'POST',
      token,
      body: payload,
    })
  },
  updateApplication(token: string, payload: Pick<Application, 'id' | 'state'>) {
    return request<boolean>('/applications', {
      method: 'PUT',
      token,
      body: payload,
    })
  },
  getProfile(uid: number) {
    return request<UserInfo>(`/users/${uid}`)
  },
}
