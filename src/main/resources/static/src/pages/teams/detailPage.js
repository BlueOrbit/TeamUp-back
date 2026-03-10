import { loadUsers, request } from "../../api.js";
import { getUserName, isLoggedIn, parseMembers, state } from "../../state.js";
import { APPLICATION_STATE, escapeHtml, formatTime, isSuccess, stateText } from "../../utils.js";
import { detailSkeleton } from "../../ui/skeleton.js";
import { showToast } from "../../ui/toast.js";

function permissionHints(teamInfo) {
    const team = teamInfo.team || {};
    const applications = Array.isArray(teamInfo.applicationList) ? teamInfo.applicationList : [];
    const members = parseMembers(team.teammates);
    const isCreator = Number(team.creatorId) === Number(state.uid);
    const isMember = members.includes(Number(state.uid));
    const myApplication = applications.find((item) => Number(item.uid) === Number(state.uid) && Number(item.state) !== APPLICATION_STATE.DECLINE);
    if (!isLoggedIn()) {
        return {
            commentHint: "游客不可评论，请先登录。",
            applyHint: "游客不可申请，请先登录。",
            canComment: false,
            canApply: false,
            isCreator
        };
    }
    if (isCreator) {
        return {
            commentHint: "你是队长，可以发表评论并审批申请。",
            applyHint: "你是队长，不需要申请本队伍。",
            canComment: true,
            canApply: false,
            isCreator
        };
    }
    if (isMember) {
        return {
            commentHint: "你是队伍成员，可发表评论。",
            applyHint: "你已在队伍中，无需重复申请。",
            canComment: true,
            canApply: false,
            isCreator
        };
    }
    if (myApplication) {
        return {
            commentHint: "登录后可评论。",
            applyHint: "你已有待处理或已通过申请，暂不可重复提交。",
            canComment: true,
            canApply: false,
            isCreator
        };
    }
    return {
        commentHint: "登录用户可发表评论。",
        applyHint: "你可以提交入队申请。",
        canComment: true,
        canApply: true,
        isCreator
    };
}

function renderTemplate(teamInfo) {
    const team = teamInfo.team || {};
    const info = teamInfo.info || {};
    const comments = Array.isArray(teamInfo.commentList) ? teamInfo.commentList : [];
    const applications = Array.isArray(teamInfo.applicationList) ? teamInfo.applicationList : [];
    const members = parseMembers(team.teammates);
    const hints = permissionHints(teamInfo);

    const commentsHtml = comments.length
        ? comments.map((item) => `
            <li class="list-item">
                <p>${escapeHtml(item.content || "")}</p>
                <small>${escapeHtml(getUserName(item.senderId))} · ${escapeHtml(formatTime(item.date))}</small>
            </li>
        `).join("")
        : "<li class='list-item'><p>暂无评论，欢迎留言。</p></li>";

    const applicationsHtml = applications.length
        ? applications.map((item) => {
            const canHandle = hints.isCreator && Number(item.state) === APPLICATION_STATE.WAIT;
            return `
                <li class="list-item">
                    <p>${escapeHtml(item.msg || "（无留言）")}</p>
                    <small>申请人：${escapeHtml(getUserName(item.uid))} · 状态：${escapeHtml(stateText(item.state))}</small>
                    ${canHandle ? `
                        <div class="sub-actions">
                            <button class="btn btn-accent js-app-action" data-action="accept" data-app-id="${escapeHtml(item.id)}" type="button">通过</button>
                            <button class="btn btn-danger js-app-action" data-action="decline" data-app-id="${escapeHtml(item.id)}" type="button">拒绝</button>
                        </div>
                    ` : ""}
                </li>
            `;
        }).join("")
        : "<li class='list-item'><p>暂无申请。</p></li>";

    return `
        <section class="card detail-grid">
            <div class="btn-row">
                <button id="backBtn" class="btn btn-ghost" type="button">返回列表</button>
                ${hints.isCreator ? `<button id="editBtn" class="btn btn-outline" type="button">编辑队伍</button>` : ""}
                ${hints.isCreator ? `<button id="deleteBtn" class="btn btn-danger" type="button">删除队伍</button>` : ""}
            </div>
            <h2>${escapeHtml(team.name || "未命名队伍")}</h2>
            <p class="muted">课程：${escapeHtml(info.course || "未填写")} ｜ 创建者：${escapeHtml(getUserName(team.creatorId))} ｜ 成员：${members.length}/${escapeHtml(info.numberLimit ?? "-")}</p>
            <p>${escapeHtml(info.content || "暂无招募描述")}</p>
            <section class="card">
                <h3>权限提示</h3>
                <p class="hint">评论权限：${escapeHtml(hints.commentHint)}</p>
                <p class="hint">申请权限：${escapeHtml(hints.applyHint)}</p>
            </section>
            <div class="detail-columns">
                <section class="card">
                    <h3>评论区</h3>
                    <ul class="list">${commentsHtml}</ul>
                    <form id="commentForm" class="form-grid">
                        <label>发表评论
                            <textarea name="content" rows="3" maxlength="255" placeholder="输入你的看法或建议"></textarea>
                        </label>
                        <button class="btn btn-primary" type="submit">提交评论</button>
                    </form>
                </section>
                <section class="card">
                    <h3>入队申请</h3>
                    <ul class="list">${applicationsHtml}</ul>
                    <form id="applyForm" class="form-grid">
                        <label>申请留言
                            <textarea name="msg" rows="3" maxlength="255" placeholder="写下你的优势和可投入时间"></textarea>
                        </label>
                        <button class="btn btn-accent" type="submit">提交申请</button>
                    </form>
                </section>
            </div>
        </section>
    `;
}

function toggleFormDisabled(form, disabled) {
    if (!form) {
        return;
    }
    for (const field of form.elements) {
        field.disabled = disabled;
    }
}

export async function renderTeamDetailPage({ app, params, navigate, signal }) {
    const teamId = Number(params.teamId);
    if (!Number.isFinite(teamId)) {
        app.innerHTML = "<div class='card empty'>参数错误：队伍 ID 无效。</div>";
        return;
    }

    const render = async () => {
        app.innerHTML = detailSkeleton();
        await loadUsers();
        if (signal.aborted) {
            return;
        }
        const result = await request(`/teams/${teamId}`, { auth: false });
        if (signal.aborted) {
            return;
        }
        if (!isSuccess(result.code) || !result.data) {
            app.innerHTML = `
                <section class="card">
                    <h2>加载失败</h2>
                    <p class="hint">${escapeHtml(result.msg || "队伍详情不存在或已删除。")}</p>
                    <button id="goBackBtn" class="btn btn-ghost" type="button">返回队伍广场</button>
                </section>
            `;
            app.querySelector("#goBackBtn")?.addEventListener("click", () => navigate("/teams"));
            return;
        }
        const teamInfo = result.data;
        const hints = permissionHints(teamInfo);
        app.innerHTML = renderTemplate(teamInfo);

        const commentForm = app.querySelector("#commentForm");
        const applyForm = app.querySelector("#applyForm");
        toggleFormDisabled(commentForm, !hints.canComment);
        toggleFormDisabled(applyForm, !hints.canApply);

        app.querySelector("#backBtn")?.addEventListener("click", () => navigate("/teams"));
        app.querySelector("#editBtn")?.addEventListener("click", () => navigate(`/teams/${teamId}/edit`));
        app.querySelector("#deleteBtn")?.addEventListener("click", async () => {
            if (!window.confirm("确认删除该队伍吗？")) {
                return;
            }
            const deleteResult = await request(`/teams/${teamId}`, { method: "DELETE" });
            if (isSuccess(deleteResult.code)) {
                showToast("队伍删除成功", "success");
                navigate("/teams");
            } else {
                showToast(deleteResult.msg || "删除失败", "error");
            }
        });

        commentForm?.addEventListener("submit", async (event) => {
            event.preventDefault();
            if (!isLoggedIn()) {
                showToast("请先登录", "error");
                return;
            }
            const content = String(new FormData(commentForm).get("content") || "").trim();
            if (!content) {
                showToast("评论内容不能为空", "error");
                return;
            }
            const commentResult = await request("/comments", {
                method: "POST",
                body: {
                    senderId: state.uid,
                    teamId,
                    content
                }
            });
            if (isSuccess(commentResult.code)) {
                showToast("评论成功", "success");
                await render();
            } else {
                showToast(commentResult.msg || "评论失败", "error");
            }
        });

        applyForm?.addEventListener("submit", async (event) => {
            event.preventDefault();
            if (!isLoggedIn()) {
                showToast("请先登录", "error");
                return;
            }
            const msg = String(new FormData(applyForm).get("msg") || "").trim();
            if (!msg) {
                showToast("申请留言不能为空", "error");
                return;
            }
            const applyResult = await request("/applications", {
                method: "POST",
                body: {
                    uid: state.uid,
                    tid: teamId,
                    msg
                }
            });
            if (isSuccess(applyResult.code)) {
                showToast("申请已提交", "success");
                await render();
            } else {
                showToast(applyResult.msg || "申请失败", "error");
            }
        });

        for (const button of app.querySelectorAll(".js-app-action")) {
            button.addEventListener("click", async () => {
                const appId = Number(button.dataset.appId);
                if (!Number.isFinite(appId)) {
                    return;
                }
                const nextState = button.dataset.action === "accept"
                    ? APPLICATION_STATE.ACCEPT
                    : APPLICATION_STATE.DECLINE;
                const updateResult = await request("/applications", {
                    method: "PUT",
                    body: { id: appId, state: nextState }
                });
                if (isSuccess(updateResult.code)) {
                    showToast("申请状态更新成功", "success");
                    await render();
                } else {
                    showToast(updateResult.msg || "申请处理失败", "error");
                }
            });
        }
    };

    await render();
}
