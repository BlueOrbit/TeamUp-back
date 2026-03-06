import { request } from "./api.js";
import { clearSession, getUserName, isLoggedIn, state, toggleTheme } from "./state.js";
import { createRouter } from "./router.js";
import { showToast } from "./ui/toast.js";
import { renderTeamDetailPage } from "./pages/teams/detailPage.js";
import { renderTeamEditPage } from "./pages/teams/editPage.js";
import { renderTeamsListPage } from "./pages/teams/listPage.js";

const routeLabelEl = document.getElementById("routeLabel");
const sessionHintEl = document.getElementById("sessionHint");
const themeBtnEl = document.getElementById("themeBtn");
const refreshBtnEl = document.getElementById("refreshBtn");
const logoutBtnEl = document.getElementById("logoutBtn");
const app = document.getElementById("app");

function applyTheme() {
    document.body.dataset.theme = state.theme;
    themeBtnEl.textContent = state.theme === "dark" ? "切换浅色" : "切换暗色";
}

function updateSessionHint() {
    if (!isLoggedIn()) {
        sessionHintEl.textContent = "游客模式：可浏览、搜索";
        logoutBtnEl.classList.add("hidden");
        return;
    }
    sessionHintEl.textContent = `已登录：${getUserName(state.uid)}（UID: ${state.uid}）`;
    logoutBtnEl.classList.remove("hidden");
}

const router = createRouter({
    app,
    routes: [
        {
            pattern: /^\/teams$/,
            label: "队伍广场",
            render: renderTeamsListPage
        },
        {
            pattern: /^\/teams\/(\d+)\/edit$/,
            label: "编辑队伍",
            mapParams: (match) => ({ teamId: Number(match[1]) }),
            render: renderTeamEditPage
        },
        {
            pattern: /^\/teams\/(\d+)$/,
            label: "队伍详情",
            mapParams: (match) => ({ teamId: Number(match[1]) }),
            render: renderTeamDetailPage
        }
    ],
    onRouteResolved: (label) => {
        routeLabelEl.textContent = label;
    }
});

document.addEventListener("teamup:session", () => {
    updateSessionHint();
});

document.addEventListener("teamup:theme", () => {
    applyTheme();
});

themeBtnEl.addEventListener("click", () => {
    toggleTheme();
});

refreshBtnEl.addEventListener("click", () => {
    router.reload();
    showToast("已刷新当前页面", "info");
});

logoutBtnEl.addEventListener("click", async () => {
    await request("/logout", { method: "POST" });
    clearSession();
    showToast("已退出登录", "info");
    router.navigate("/teams");
});

applyTheme();
updateSessionHint();
router.start();
