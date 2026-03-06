(() => {
    const STORAGE_TOKEN_KEY = "teamup_token";
    const STORAGE_UID_KEY = "teamup_uid";
    const APPLICATION_STATE = {
        WAIT: 0,
        ACCEPT: 1,
        DECLINE: 2
    };

    const state = {
        token: localStorage.getItem(STORAGE_TOKEN_KEY) || "",
        uid: parseUid(localStorage.getItem(STORAGE_UID_KEY)),
        teams: [],
        selectedTeam: null,
        userNameMap: new Map(),
        searchKeyword: ""
    };

    const elements = {
        sessionHint: document.getElementById("sessionHint"),
        refreshBtn: document.getElementById("refreshBtn"),
        logoutBtn: document.getElementById("logoutBtn"),
        summaryText: document.getElementById("summaryText"),
        teamList: document.getElementById("teamList"),
        teamDetail: document.getElementById("teamDetail"),
        detailTitle: document.getElementById("detailTitle"),
        detailMeta: document.getElementById("detailMeta"),
        detailContent: document.getElementById("detailContent"),
        commentList: document.getElementById("commentList"),
        applicationList: document.getElementById("applicationList"),
        registerForm: document.getElementById("registerForm"),
        loginForm: document.getElementById("loginForm"),
        createTeamForm: document.getElementById("createTeamForm"),
        searchForm: document.getElementById("searchForm"),
        searchInput: document.getElementById("searchInput"),
        resetSearchBtn: document.getElementById("resetSearchBtn"),
        closeDetailBtn: document.getElementById("closeDetailBtn"),
        commentForm: document.getElementById("commentForm"),
        applicationForm: document.getElementById("applicationForm"),
        toastContainer: document.getElementById("toastContainer")
    };

    function parseUid(raw) {
        const value = Number.parseInt(raw || "", 10);
        return Number.isFinite(value) ? value : null;
    }

    function saveSession(token, uid) {
        state.token = token || "";
        state.uid = Number.isFinite(uid) ? uid : null;
        if (state.token && state.uid) {
            localStorage.setItem(STORAGE_TOKEN_KEY, state.token);
            localStorage.setItem(STORAGE_UID_KEY, String(state.uid));
        } else {
            localStorage.removeItem(STORAGE_TOKEN_KEY);
            localStorage.removeItem(STORAGE_UID_KEY);
        }
        syncSessionUI();
    }

    function syncSessionUI() {
        const loggedIn = Boolean(state.token && state.uid);
        elements.sessionHint.textContent = loggedIn
            ? `已登录：用户 #${state.uid}`
            : "未登录（可浏览队伍、搜索信息）";
        elements.logoutBtn.classList.toggle("hidden", !loggedIn);
        toggleFormDisabled(elements.createTeamForm, !loggedIn);
    }

    function toggleFormDisabled(form, disabled) {
        if (!form) {
            return;
        }
        for (const field of form.elements) {
            field.disabled = disabled;
        }
    }

    function isSuccess(code) {
        return typeof code === "number" && code % 10 === 1;
    }

    // 统一请求封装：自动带 token、兼容 JSON 与文本错误信息。
    async function api(path, { method = "GET", body, auth = method !== "GET" } = {}) {
        const headers = {};
        if (body !== undefined) {
            headers["Content-Type"] = "application/json";
        }
        if (auth && state.token) {
            headers.Authorization = `Bearer ${state.token}`;
        }
        try {
            const response = await fetch(path, {
                method,
                headers,
                body: body !== undefined ? JSON.stringify(body) : undefined
            });
            const contentType = response.headers.get("content-type") || "";
            let payload;
            if (contentType.includes("application/json")) {
                payload = await response.json();
            } else {
                payload = {
                    code: response.ok ? 200001 : 200000,
                    data: null,
                    msg: (await response.text()) || ""
                };
            }
            if (!response.ok && !payload.msg) {
                payload.msg = `请求失败（HTTP ${response.status}）`;
            }
            if (payload && payload.code === 200000 && /(token|unauthorized|expired|bearer)/i.test(payload.msg || "")) {
                saveSession("", null);
                showToast("登录状态已失效，请重新登录", "error");
            }
            return payload;
        } catch (error) {
            return {
                code: 200000,
                data: null,
                msg: error instanceof Error ? error.message : "网络错误"
            };
        }
    }

    function parseMembers(raw) {
        if (!raw || typeof raw !== "string") {
            return [];
        }
        return raw.split(";").map((item) => item.trim()).filter(Boolean);
    }

    function getUserName(uid) {
        const key = Number(uid);
        return state.userNameMap.get(key) || `用户#${uid ?? "-"}`;
    }

    function formatTime(raw) {
        if (!raw) {
            return "未知时间";
        }
        const date = new Date(raw);
        if (Number.isNaN(date.getTime())) {
            return raw;
        }
        return date.toLocaleString("zh-CN");
    }

    function applicationStateText(stateValue) {
        if (stateValue === APPLICATION_STATE.ACCEPT) {
            return "已通过";
        }
        if (stateValue === APPLICATION_STATE.DECLINE) {
            return "已拒绝";
        }
        return "待处理";
    }

    function escapeHtml(input) {
        return String(input ?? "")
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("\"", "&quot;")
            .replaceAll("'", "&#39;");
    }

    function showToast(message, type = "info") {
        const toast = document.createElement("div");
        toast.className = `toast ${type}`;
        toast.textContent = message;
        elements.toastContainer.appendChild(toast);
        setTimeout(() => {
            toast.remove();
        }, 2600);
    }

    async function loadUsers() {
        const result = await api("/users", { auth: false });
        if (!isSuccess(result.code) || !Array.isArray(result.data)) {
            return;
        }
        const map = new Map();
        for (const item of result.data) {
            if (item && item.user && item.user.id != null) {
                map.set(Number(item.user.id), item.user.name || `用户#${item.user.id}`);
            }
        }
        state.userNameMap = map;
    }

    async function loadTeams() {
        const keyword = state.searchKeyword.trim();
        const result = keyword
            ? await api("/info/search", {
                method: "POST",
                auth: false,
                body: { content: keyword }
            })
            : await api("/teams", { auth: false });

        if (!isSuccess(result.code) || !Array.isArray(result.data)) {
            state.teams = [];
            renderTeamList();
            elements.summaryText.textContent = `加载失败：${result.msg || "请稍后重试"}`;
            return;
        }
        state.teams = result.data;
        renderTeamList();
        elements.summaryText.textContent = keyword
            ? `搜索 "${keyword}" 共找到 ${state.teams.length} 个队伍`
            : `当前共有 ${state.teams.length} 个队伍`;
    }

    function renderTeamList() {
        if (!state.teams.length) {
            elements.teamList.innerHTML = "<div class='card empty'>暂无数据，试试修改关键词或先创建一个队伍。</div>";
            return;
        }
        const loggedIn = Boolean(state.uid && state.token);
        const cards = state.teams.map((teamInfo) => {
            const team = teamInfo.team || {};
            const info = teamInfo.info || {};
            const memberCount = parseMembers(team.teammates).length;
            const creatorName = getUserName(team.creatorId);
            const commentCount = Array.isArray(teamInfo.commentList) ? teamInfo.commentList.length : 0;
            const applicationCount = Array.isArray(teamInfo.applicationList) ? teamInfo.applicationList.length : 0;
            const canApply = loggedIn && Number(team.creatorId) !== Number(state.uid);
            return `
                <article class="team-item">
                    <div class="team-head">
                        <h3 class="team-name">${escapeHtml(team.name || "未命名队伍")}</h3>
                        <span class="pill">${escapeHtml(info.course || "未填写课程")}</span>
                    </div>
                    <p class="team-meta">创建者：${escapeHtml(creatorName)} · 成员：${memberCount}/${escapeHtml(info.numberLimit ?? "-")}</p>
                    <p class="team-meta">评论 ${commentCount} 条 · 申请 ${applicationCount} 条</p>
                    <p class="team-meta">${escapeHtml(info.content || "暂无招募描述")}</p>
                    <div class="team-actions">
                        <button class="btn btn-primary js-open-detail" data-team-id="${escapeHtml(team.id)}" type="button">查看详情</button>
                        <button class="btn btn-accent js-quick-apply" data-team-id="${escapeHtml(team.id)}" type="button" ${canApply ? "" : "disabled"}>快速申请</button>
                    </div>
                </article>
            `;
        }).join("");
        elements.teamList.innerHTML = cards;

        for (const btn of elements.teamList.querySelectorAll(".js-open-detail")) {
            btn.addEventListener("click", () => {
                const id = Number(btn.dataset.teamId);
                if (Number.isFinite(id)) {
                    openTeamDetail(id);
                }
            });
        }
        for (const btn of elements.teamList.querySelectorAll(".js-quick-apply")) {
            btn.addEventListener("click", async () => {
                const id = Number(btn.dataset.teamId);
                if (Number.isFinite(id)) {
                    await quickApply(id);
                }
            });
        }
    }

    async function openTeamDetail(teamId) {
        const result = await api(`/teams/${teamId}`, { auth: false });
        if (!isSuccess(result.code) || !result.data) {
            showToast(result.msg || "队伍详情加载失败", "error");
            return;
        }
        state.selectedTeam = result.data;
        renderTeamDetail();
        elements.teamDetail.classList.remove("hidden");
        elements.teamDetail.scrollIntoView({ behavior: "smooth", block: "start" });
    }

    function renderTeamDetail() {
        const teamInfo = state.selectedTeam;
        const team = teamInfo?.team || {};
        const info = teamInfo?.info || {};
        const comments = Array.isArray(teamInfo?.commentList) ? teamInfo.commentList : [];
        const applications = Array.isArray(teamInfo?.applicationList) ? teamInfo.applicationList : [];
        const isCreator = Number(state.uid) === Number(team.creatorId);

        elements.detailTitle.textContent = team.name || "队伍详情";
        elements.detailMeta.textContent = `课程：${info.course || "未填写"} ｜ 创建者：${getUserName(team.creatorId)} ｜ 成员 ${parseMembers(team.teammates).length}/${info.numberLimit ?? "-"}`;
        elements.detailContent.textContent = info.content || "暂无招募描述";

        elements.commentList.innerHTML = comments.length
            ? comments.map((comment) => `
                <li class="list-item">
                    <p>${escapeHtml(comment.content || "")}</p>
                    <small>${escapeHtml(getUserName(comment.senderId))} · ${escapeHtml(formatTime(comment.date))}</small>
                </li>
            `).join("")
            : "<li class='list-item'><p>还没有评论，欢迎第一个发言。</p></li>";

        const appHtml = applications.length
            ? applications.map((item) => {
                const stateText = applicationStateText(item.state);
                const canHandle = isCreator && Number(item.state) === APPLICATION_STATE.WAIT;
                return `
                    <li class="list-item">
                        <p>${escapeHtml(item.msg || "（无留言）")}</p>
                        <small>申请人：${escapeHtml(getUserName(item.uid))} · 状态：${escapeHtml(stateText)}</small>
                        ${canHandle ? `
                            <div class="application-actions">
                                <button class="btn btn-accent js-app-action" data-action="accept" data-app-id="${escapeHtml(item.id)}" type="button">通过</button>
                                <button class="btn btn-danger js-app-action" data-action="decline" data-app-id="${escapeHtml(item.id)}" type="button">拒绝</button>
                            </div>
                        ` : ""}
                    </li>
                `;
            }).join("")
            : "<li class='list-item'><p>暂无申请。</p></li>";
        elements.applicationList.innerHTML = appHtml;

        for (const btn of elements.applicationList.querySelectorAll(".js-app-action")) {
            btn.addEventListener("click", async () => {
                const id = Number(btn.dataset.appId);
                if (!Number.isFinite(id)) {
                    return;
                }
                const nextState = btn.dataset.action === "accept"
                    ? APPLICATION_STATE.ACCEPT
                    : APPLICATION_STATE.DECLINE;
                await updateApplicationState(id, nextState);
            });
        }

        const canComment = Boolean(state.uid && state.token);
        toggleFormDisabled(elements.commentForm, !canComment);
        if (canComment) {
            elements.commentForm.querySelector("textarea").placeholder = "输入你的建议或问题";
        } else {
            elements.commentForm.querySelector("textarea").placeholder = "请先登录后发表评论";
        }

        const hasApplied = applications.some((item) => Number(item.uid) === Number(state.uid) && Number(item.state) !== APPLICATION_STATE.DECLINE);
        const canApply = Boolean(state.uid && state.token) && !isCreator && !hasApplied;
        toggleFormDisabled(elements.applicationForm, !canApply);
        if (isCreator) {
            elements.applicationForm.querySelector("textarea").placeholder = "你是队长，可在上方处理申请";
        } else if (hasApplied) {
            elements.applicationForm.querySelector("textarea").placeholder = "你已有待处理或已通过的申请";
        } else if (!state.uid) {
            elements.applicationForm.querySelector("textarea").placeholder = "请先登录后提交申请";
        } else {
            elements.applicationForm.querySelector("textarea").placeholder = "简要说明你的优势与可投入时间";
        }
    }

    async function quickApply(teamId) {
        if (!(state.uid && state.token)) {
            showToast("请先登录后申请入队", "error");
            return;
        }
        const msg = window.prompt("请输入申请留言：", "我有相关经验，愿意积极参与。");
        if (!msg || !msg.trim()) {
            return;
        }
        const result = await api("/applications", {
            method: "POST",
            body: { uid: state.uid, tid: teamId, msg: msg.trim() }
        });
        if (isSuccess(result.code)) {
            showToast("申请已提交", "success");
            await loadTeams();
            if (state.selectedTeam?.team?.id === teamId) {
                await openTeamDetail(teamId);
            }
            return;
        }
        showToast(result.msg || "申请提交失败", "error");
    }

    async function updateApplicationState(applicationId, nextState) {
        const result = await api("/applications", {
            method: "PUT",
            body: { id: applicationId, state: nextState }
        });
        if (isSuccess(result.code)) {
            showToast("申请状态已更新", "success");
            const currentTeamId = Number(state.selectedTeam?.team?.id);
            await loadTeams();
            if (Number.isFinite(currentTeamId)) {
                await openTeamDetail(currentTeamId);
            }
            return;
        }
        showToast(result.msg || "申请处理失败", "error");
    }

    elements.registerForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const formData = new FormData(elements.registerForm);
        const body = {
            name: String(formData.get("name") || "").trim(),
            email: String(formData.get("email") || "").trim(),
            password: String(formData.get("password") || "").trim()
        };
        const result = await api("/users", { method: "POST", auth: false, body });
        if (isSuccess(result.code)) {
            showToast("注册成功，请继续登录", "success");
            elements.registerForm.reset();
            await loadUsers();
            return;
        }
        showToast(result.msg || "注册失败", "error");
    });

    elements.loginForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const formData = new FormData(elements.loginForm);
        const body = {
            email: String(formData.get("email") || "").trim(),
            password: String(formData.get("password") || "").trim()
        };
        const result = await api("/login", { method: "POST", auth: false, body });
        const payload = result.data || {};
        if (isSuccess(result.code) && payload.token && payload.uid != null) {
            saveSession(String(payload.token), Number(payload.uid));
            showToast("登录成功", "success");
            elements.loginForm.reset();
            await loadUsers();
            await loadTeams();
            return;
        }
        showToast(result.msg || "登录失败", "error");
    });

    elements.logoutBtn.addEventListener("click", async () => {
        await api("/logout", { method: "POST" });
        saveSession("", null);
        showToast("已退出登录", "info");
    });

    elements.createTeamForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        if (!(state.uid && state.token)) {
            showToast("请先登录后创建队伍", "error");
            return;
        }
        const formData = new FormData(elements.createTeamForm);
        const body = {
            team: {
                creatorId: state.uid,
                name: String(formData.get("name") || "").trim()
            },
            info: {
                course: String(formData.get("course") || "").trim(),
                numberLimit: Number(formData.get("numberLimit") || 0),
                content: String(formData.get("content") || "").trim()
            }
        };
        const result = await api("/teams", { method: "POST", body });
        if (isSuccess(result.code)) {
            showToast("队伍创建成功", "success");
            elements.createTeamForm.reset();
            await loadTeams();
            return;
        }
        showToast(result.msg || "创建队伍失败", "error");
    });

    elements.searchForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        state.searchKeyword = String(new FormData(elements.searchForm).get("keyword") || "").trim();
        await loadTeams();
    });

    elements.resetSearchBtn.addEventListener("click", async () => {
        state.searchKeyword = "";
        elements.searchInput.value = "";
        await loadTeams();
    });

    elements.refreshBtn.addEventListener("click", async () => {
        await loadUsers();
        await loadTeams();
        showToast("数据已刷新", "info");
    });

    elements.closeDetailBtn.addEventListener("click", () => {
        state.selectedTeam = null;
        elements.teamDetail.classList.add("hidden");
    });

    elements.commentForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const teamId = Number(state.selectedTeam?.team?.id);
        if (!Number.isFinite(teamId)) {
            showToast("请先选择队伍", "error");
            return;
        }
        if (!(state.uid && state.token)) {
            showToast("请先登录后评论", "error");
            return;
        }
        const formData = new FormData(elements.commentForm);
        const content = String(formData.get("content") || "").trim();
        if (!content) {
            showToast("评论内容不能为空", "error");
            return;
        }
        const result = await api("/comments", {
            method: "POST",
            body: {
                senderId: state.uid,
                teamId,
                content
            }
        });
        if (isSuccess(result.code)) {
            showToast("评论发布成功", "success");
            elements.commentForm.reset();
            await loadTeams();
            await openTeamDetail(teamId);
            return;
        }
        showToast(result.msg || "评论失败", "error");
    });

    elements.applicationForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        const teamId = Number(state.selectedTeam?.team?.id);
        if (!Number.isFinite(teamId)) {
            showToast("请先选择队伍", "error");
            return;
        }
        if (!(state.uid && state.token)) {
            showToast("请先登录后申请", "error");
            return;
        }
        const formData = new FormData(elements.applicationForm);
        const msg = String(formData.get("msg") || "").trim();
        if (!msg) {
            showToast("申请留言不能为空", "error");
            return;
        }
        const result = await api("/applications", {
            method: "POST",
            body: {
                uid: state.uid,
                tid: teamId,
                msg
            }
        });
        if (isSuccess(result.code)) {
            showToast("申请提交成功", "success");
            elements.applicationForm.reset();
            await loadTeams();
            await openTeamDetail(teamId);
            return;
        }
        showToast(result.msg || "申请失败", "error");
    });

    async function bootstrap() {
        syncSessionUI();
        await loadUsers();
        await loadTeams();
        showToast("欢迎来到 TeamUp", "info");
    }

    bootstrap();
})();
