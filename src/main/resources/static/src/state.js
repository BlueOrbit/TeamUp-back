const TOKEN_KEY = "teamup_token";
const UID_KEY = "teamup_uid";
const THEME_KEY = "teamup_theme";

const initialTheme = localStorage.getItem(THEME_KEY) || "light";
const initialUid = Number.parseInt(localStorage.getItem(UID_KEY) || "", 10);

export const state = {
    token: localStorage.getItem(TOKEN_KEY) || "",
    uid: Number.isFinite(initialUid) ? initialUid : null,
    theme: initialTheme === "dark" ? "dark" : "light",
    searchKeyword: "",
    userNameMap: new Map()
};

function emitStateEvent(type) {
    document.dispatchEvent(new CustomEvent(`teamup:${type}`));
}

export function isLoggedIn() {
    return Boolean(state.token && Number.isFinite(state.uid));
}

export function setSession(token, uid) {
    state.token = token || "";
    state.uid = Number.isFinite(uid) ? uid : null;
    if (isLoggedIn()) {
        localStorage.setItem(TOKEN_KEY, state.token);
        localStorage.setItem(UID_KEY, String(state.uid));
    } else {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(UID_KEY);
    }
    emitStateEvent("session");
}

export function clearSession() {
    setSession("", null);
}

export function setTheme(theme) {
    state.theme = theme === "dark" ? "dark" : "light";
    localStorage.setItem(THEME_KEY, state.theme);
    emitStateEvent("theme");
}

export function toggleTheme() {
    setTheme(state.theme === "dark" ? "light" : "dark");
}

export function setSearchKeyword(keyword) {
    state.searchKeyword = String(keyword || "").trim();
}

export function setUserMap(map) {
    state.userNameMap = map instanceof Map ? map : new Map();
}

export function getUserName(uid) {
    const id = Number(uid);
    return state.userNameMap.get(id) || `用户#${uid ?? "-"}`;
}

export function parseMembers(raw) {
    if (!raw || typeof raw !== "string") {
        return [];
    }
    return raw
        .split(";")
        .map((item) => Number.parseInt(item.trim(), 10))
        .filter((item) => Number.isFinite(item));
}
