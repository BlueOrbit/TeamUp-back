import { loadUsers, request } from "../../api.js";
import { getUserName, isLoggedIn, parseMembers, state } from "../../state.js";
import { escapeHtml, isSuccess } from "../../utils.js";
import { detailSkeleton } from "../../ui/skeleton.js";
import { showToast } from "../../ui/toast.js";

function editTemplate(teamInfo) {
    const team = teamInfo.team || {};
    const info = teamInfo.info || {};
    const members = parseMembers(team.teammates);
    return `
        <section class="card detail-grid">
            <div class="btn-row">
                <button id="backDetailBtn" class="btn btn-ghost" type="button">返回详情</button>
                <button id="deleteTeamBtn" class="btn btn-danger" type="button">删除队伍</button>
            </div>
            <h2>编辑队伍：${escapeHtml(team.name || "")}</h2>
            <p class="muted">创建者：${escapeHtml(getUserName(team.creatorId))} ｜ 当前成员：${members.length}</p>
            <form id="editTeamForm" class="form-grid">
                <label>队伍名称
                    <input name="name" type="text" maxlength="32" required value="${escapeHtml(team.name || "")}">
                </label>
                <label>课程名称
                    <input name="course" type="text" maxlength="32" required value="${escapeHtml(info.course || "")}">
                </label>
                <label>人数上限
                    <input name="numberLimit" type="number" min="1" max="20" required value="${escapeHtml(info.numberLimit ?? 4)}">
                </label>
                <label>招募描述
                    <textarea name="content" rows="4" maxlength="255" required>${escapeHtml(info.content || "")}</textarea>
                </label>
                <div class="btn-row">
                    <button class="btn btn-primary" type="submit">保存修改</button>
                    <button id="cancelBtn" class="btn btn-outline" type="button">取消</button>
                </div>
            </form>
        </section>
    `;
}

export async function renderTeamEditPage({ app, params, navigate, signal }) {
    const teamId = Number(params.teamId);
    if (!Number.isFinite(teamId)) {
        app.innerHTML = "<div class='card empty'>参数错误：队伍 ID 无效。</div>";
        return;
    }
    if (!isLoggedIn()) {
        app.innerHTML = `
            <section class="card">
                <h2>无权限</h2>
                <p class="hint">请先登录后编辑队伍。</p>
                <button id="goListBtn" class="btn btn-ghost" type="button">返回队伍列表</button>
            </section>
        `;
        app.querySelector("#goListBtn")?.addEventListener("click", () => navigate("/teams"));
        return;
    }

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
                <p class="hint">${escapeHtml(result.msg || "队伍不存在。")}</p>
                <button id="backListBtn" class="btn btn-ghost" type="button">返回队伍列表</button>
            </section>
        `;
        app.querySelector("#backListBtn")?.addEventListener("click", () => navigate("/teams"));
        return;
    }
    const teamInfo = result.data;
    const team = teamInfo.team || {};
    if (Number(team.creatorId) !== Number(state.uid)) {
        app.innerHTML = `
            <section class="card">
                <h2>无权限</h2>
                <p class="hint">仅队伍创建者可编辑和删除队伍。</p>
                <button id="goDetailBtn" class="btn btn-ghost" type="button">返回队伍详情</button>
            </section>
        `;
        app.querySelector("#goDetailBtn")?.addEventListener("click", () => navigate(`/teams/${teamId}`));
        return;
    }

    app.innerHTML = editTemplate(teamInfo);

    const form = app.querySelector("#editTeamForm");
    const deleteBtn = app.querySelector("#deleteTeamBtn");

    app.querySelector("#backDetailBtn")?.addEventListener("click", () => navigate(`/teams/${teamId}`));
    app.querySelector("#cancelBtn")?.addEventListener("click", () => navigate(`/teams/${teamId}`));

    form?.addEventListener("submit", async (event) => {
        event.preventDefault();
        const data = new FormData(form);
        const updateResult = await request("/teams", {
            method: "PUT",
            body: {
                team: {
                    id: teamId,
                    name: String(data.get("name") || "").trim()
                },
                info: {
                    course: String(data.get("course") || "").trim(),
                    numberLimit: Number(data.get("numberLimit") || 0),
                    content: String(data.get("content") || "").trim()
                }
            }
        });
        if (isSuccess(updateResult.code)) {
            showToast("队伍更新成功", "success");
            navigate(`/teams/${teamId}`);
        } else {
            showToast(updateResult.msg || "更新失败", "error");
        }
    });

    deleteBtn?.addEventListener("click", async () => {
        if (!window.confirm("确认删除该队伍吗？该操作不可恢复。")) {
            return;
        }
        const deleteResult = await request(`/teams/${teamId}`, { method: "DELETE" });
        if (isSuccess(deleteResult.code)) {
            showToast("队伍已删除", "success");
            navigate("/teams");
        } else {
            showToast(deleteResult.msg || "删除失败", "error");
        }
    });
}
