import { loadTeams, loadUsers, request } from "../../api.js";
import { getUserName, isLoggedIn, parseMembers, setSearchKeyword, setSession, state } from "../../state.js";
import { APPLICATION_STATE, escapeHtml, isSuccess } from "../../utils.js";
import { teamListSkeleton } from "../../ui/skeleton.js";
import { showToast } from "../../ui/toast.js";

function permissionText(teamInfo) {
    const team = teamInfo.team || {};
    const applications = Array.isArray(teamInfo.applicationList) ? teamInfo.applicationList : [];
    const memberIds = parseMembers(team.teammates);
    if (!isLoggedIn()) {
        return "游客：登录后可申请和评论";
    }
    if (Number(team.creatorId) === Number(state.uid)) {
        return "队长：可编辑、删除、审批申请";
    }
    if (memberIds.includes(Number(state.uid))) {
        return "成员：可参与评论与交流";
    }
    const myApplication = applications.find((item) => Number(item.uid) === Number(state.uid));
    if (myApplication?.state === APPLICATION_STATE.ACCEPT) {
        return "你已通过申请并加入队伍";
    }
    if (myApplication && Number(myApplication.state) !== APPLICATION_STATE.DECLINE) {
        return "你已申请，等待队长审批";
    }
    return "可提交申请加入队伍";
}

function canApply(teamInfo) {
    if (!isLoggedIn()) {
        return false;
    }
    const team = teamInfo.team || {};
    if (Number(team.creatorId) === Number(state.uid)) {
        return false;
    }
    const memberIds = parseMembers(team.teammates);
    if (memberIds.includes(Number(state.uid))) {
        return false;
    }
    const applications = Array.isArray(teamInfo.applicationList) ? teamInfo.applicationList : [];
    return !applications.some((item) => Number(item.uid) === Number(state.uid) && Number(item.state) !== APPLICATION_STATE.DECLINE);
}

function roleHintText() {
    if (!isLoggedIn()) {
        return "当前为游客模式：可浏览、搜索、查看详情。登录后可创建队伍、发表评论和提交申请。";
    }
    const name = getUserName(state.uid);
    return `当前登录：${name}（UID: ${state.uid}）。你可创建队伍，并在自己创建的队伍中进行编辑/删除/审批。`;
}

function template() {
    return `
        <div class="layout-two-columns">
            <aside class="panel">
                <section class="card">
                    <h2>权限提示</h2>
                    <p id="roleHint" class="hint"></p>
                </section>
                <section class="card">
                    <h2>注册账号</h2>
                    <form id="registerForm" class="form-grid">
                        <label>昵称
                            <input name="name" type="text" maxlength="32" placeholder="例如：小蓝">
                        </label>
                        <label>邮箱
                            <input name="email" type="email" required placeholder="name@example.com">
                        </label>
                        <label>密码
                            <input name="password" type="password" required minlength="6" placeholder="至少 6 位">
                        </label>
                        <button class="btn btn-primary" type="submit">注册</button>
                    </form>
                </section>
                <section class="card">
                    <h2>登录</h2>
                    <form id="loginForm" class="form-grid">
                        <label>邮箱
                            <input name="email" type="email" required>
                        </label>
                        <label>密码
                            <input name="password" type="password" required>
                        </label>
                        <button class="btn btn-primary" type="submit">登录</button>
                    </form>
                </section>
                <section class="card">
                    <h2>创建队伍</h2>
                    <form id="createTeamForm" class="form-grid">
                        <label>队伍名称
                            <input name="name" type="text" maxlength="32" required placeholder="例如：软件工程第7组">
                        </label>
                        <label>课程名称
                            <input name="course" type="text" maxlength="32" required placeholder="例如：软件工程">
                        </label>
                        <label>人数上限
                            <input name="numberLimit" type="number" min="1" max="20" value="4" required>
                        </label>
                        <label>招募描述
                            <textarea name="content" rows="3" maxlength="255" required placeholder="写明项目方向和需求技能"></textarea>
                        </label>
                        <button class="btn btn-accent" type="submit">创建队伍</button>
                    </form>
                    <p id="createTeamHint" class="hint"></p>
                </section>
            </aside>
            <section class="panel">
                <section class="card">
                    <form id="searchForm" class="search-form">
                        <input id="searchInput" name="keyword" type="text" maxlength="255" placeholder="输入关键词搜索（如：前端、算法）">
                        <button class="btn btn-primary" type="submit">搜索</button>
                        <button id="resetSearchBtn" class="btn btn-ghost" type="button">清除</button>
                    </form>
                    <p id="summaryText" class="summary">正在加载队伍数据...</p>
                </section>
                <section id="teamList"></section>
            </section>
        </div>
    `;
}

export async function renderTeamsListPage({ app, navigate, signal }) {
    app.innerHTML = template();

    const roleHint = app.querySelector("#roleHint");
    const createTeamHint = app.querySelector("#createTeamHint");
    const registerForm = app.querySelector("#registerForm");
    const loginForm = app.querySelector("#loginForm");
    const createTeamForm = app.querySelector("#createTeamForm");
    const searchForm = app.querySelector("#searchForm");
    const searchInput = app.querySelector("#searchInput");
    const resetSearchBtn = app.querySelector("#resetSearchBtn");
    const summaryText = app.querySelector("#summaryText");
    const teamList = app.querySelector("#teamList");

    searchInput.value = state.searchKeyword;

    const updateAuthHints = () => {
        roleHint.textContent = roleHintText();
        const createDisabled = !isLoggedIn();
        for (const field of createTeamForm.elements) {
            field.disabled = createDisabled;
        }
        createTeamHint.textContent = createDisabled
            ? "请先登录后创建队伍"
            : "你创建的队伍会自动把你加入成员列表";
    };

    const renderList = (teamInfos) => {
        if (!teamInfos.length) {
            teamList.innerHTML = "<div class='empty'>暂无队伍，试试切换关键词或创建一个新队伍。</div>";
            return;
        }
        const html = teamInfos.map((teamInfo) => {
            const team = teamInfo.team || {};
            const info = teamInfo.info || {};
            const members = parseMembers(team.teammates);
            const applications = Array.isArray(teamInfo.applicationList) ? teamInfo.applicationList : [];
            const comments = Array.isArray(teamInfo.commentList) ? teamInfo.commentList : [];
            const isCreator = Number(team.creatorId) === Number(state.uid);
            const applyEnabled = canApply(teamInfo);
            return `
                <article class="team-item">
                    <div class="team-head">
                        <h3 class="team-name">${escapeHtml(team.name || "未命名队伍")}</h3>
                        <span class="badge">${escapeHtml(info.course || "未填写课程")}</span>
                    </div>
                    <p class="team-meta">创建者：${escapeHtml(getUserName(team.creatorId))} ｜ 成员：${members.length}/${escapeHtml(info.numberLimit ?? "-")}</p>
                    <p class="team-meta">${escapeHtml(info.content || "暂无招募描述")}</p>
                    <p class="hint">${escapeHtml(permissionText(teamInfo))}</p>
                    <p class="muted">评论 ${comments.length} 条 ｜ 申请 ${applications.length} 条</p>
                    <div class="team-actions">
                        <button class="btn btn-primary js-view" data-team-id="${escapeHtml(team.id)}" type="button">查看详情</button>
                        <button class="btn btn-accent js-apply" data-team-id="${escapeHtml(team.id)}" type="button" ${applyEnabled ? "" : "disabled"}>快速申请</button>
                        ${isCreator ? `<button class="btn btn-outline js-edit" data-team-id="${escapeHtml(team.id)}" type="button">编辑</button>` : ""}
                        ${isCreator ? `<button class="btn btn-danger js-delete" data-team-id="${escapeHtml(team.id)}" type="button">删除</button>` : ""}
                    </div>
                </article>
            `;
        }).join("");
        teamList.innerHTML = `<div class="team-list">${html}</div>`;

        for (const btn of teamList.querySelectorAll(".js-view")) {
            btn.addEventListener("click", () => navigate(`/teams/${btn.dataset.teamId}`));
        }
        for (const btn of teamList.querySelectorAll(".js-edit")) {
            btn.addEventListener("click", () => navigate(`/teams/${btn.dataset.teamId}/edit`));
        }
        for (const btn of teamList.querySelectorAll(".js-apply")) {
            btn.addEventListener("click", async () => {
                if (!isLoggedIn()) {
                    showToast("请先登录后申请", "error");
                    return;
                }
                const teamId = Number(btn.dataset.teamId);
                const msg = window.prompt("请输入申请留言：", "我有相关经验，愿意积极参与。");
                if (!msg || !msg.trim()) {
                    return;
                }
                const result = await request("/applications", {
                    method: "POST",
                    body: { uid: state.uid, tid: teamId, msg: msg.trim() }
                });
                if (isSuccess(result.code)) {
                    showToast("申请已提交", "success");
                    await fetchAndRender();
                } else {
                    showToast(result.msg || "申请失败", "error");
                }
            });
        }
        for (const btn of teamList.querySelectorAll(".js-delete")) {
            btn.addEventListener("click", async () => {
                const teamId = Number(btn.dataset.teamId);
                if (!window.confirm("确认删除该队伍吗？此操作不可撤销。")) {
                    return;
                }
                const result = await request(`/teams/${teamId}`, { method: "DELETE" });
                if (isSuccess(result.code)) {
                    showToast("队伍已删除", "success");
                    await fetchAndRender();
                } else {
                    showToast(result.msg || "删除失败", "error");
                }
            });
        }
    };

    const fetchAndRender = async () => {
        summaryText.textContent = "正在加载队伍数据...";
        teamList.innerHTML = teamListSkeleton(4);
        await loadUsers();
        if (signal.aborted) {
            return;
        }
        const result = await loadTeams(state.searchKeyword);
        if (signal.aborted) {
            return;
        }
        if (!isSuccess(result.code) || !Array.isArray(result.data)) {
            summaryText.textContent = `加载失败：${result.msg || "请稍后重试"}`;
            teamList.innerHTML = "<div class='empty'>数据加载失败，请稍后刷新。</div>";
            return;
        }
        renderList(result.data);
        summaryText.textContent = state.searchKeyword
            ? `搜索 "${state.searchKeyword}" 共找到 ${result.data.length} 个队伍`
            : `当前共有 ${result.data.length} 个队伍`;
    };

    registerForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const data = new FormData(registerForm);
        const body = {
            name: String(data.get("name") || "").trim(),
            email: String(data.get("email") || "").trim(),
            password: String(data.get("password") || "").trim()
        };
        const result = await request("/users", { method: "POST", auth: false, body });
        if (isSuccess(result.code)) {
            showToast("注册成功，请登录", "success");
            registerForm.reset();
            await loadUsers();
            updateAuthHints();
        } else {
            showToast(result.msg || "注册失败", "error");
        }
    });

    loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const data = new FormData(loginForm);
        const body = {
            email: String(data.get("email") || "").trim(),
            password: String(data.get("password") || "").trim()
        };
        const result = await request("/login", { method: "POST", auth: false, body });
        if (isSuccess(result.code) && result.data?.token && result.data?.uid != null) {
            setSession(String(result.data.token), Number(result.data.uid));
            showToast("登录成功", "success");
            loginForm.reset();
            updateAuthHints();
            await fetchAndRender();
        } else {
            showToast(result.msg || "登录失败", "error");
        }
    });

    createTeamForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        if (!isLoggedIn()) {
            showToast("请先登录", "error");
            return;
        }
        const data = new FormData(createTeamForm);
        const result = await request("/teams", {
            method: "POST",
            body: {
                team: {
                    creatorId: state.uid,
                    name: String(data.get("name") || "").trim()
                },
                info: {
                    course: String(data.get("course") || "").trim(),
                    numberLimit: Number(data.get("numberLimit") || 0),
                    content: String(data.get("content") || "").trim()
                }
            }
        });
        if (isSuccess(result.code)) {
            showToast("队伍创建成功", "success");
            createTeamForm.reset();
            await fetchAndRender();
        } else {
            showToast(result.msg || "创建队伍失败", "error");
        }
    });

    searchForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const keyword = String(new FormData(searchForm).get("keyword") || "");
        setSearchKeyword(keyword);
        await fetchAndRender();
    });

    resetSearchBtn.addEventListener("click", async () => {
        setSearchKeyword("");
        searchInput.value = "";
        await fetchAndRender();
    });

    updateAuthHints();
    await fetchAndRender();
}
