import { clearSession, isLoggedIn, setUserMap, state } from "./state.js";
import { isSuccess } from "./utils.js";

export async function request(path, { method = "GET", body, auth = method !== "GET" } = {}) {
    const headers = {};
    if (body !== undefined) {
        headers["Content-Type"] = "application/json";
    }
    if (auth && isLoggedIn()) {
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
        if (payload?.code === 200000 && /(token|bearer|unauthorized|expired)/i.test(payload.msg || "")) {
            clearSession();
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

export async function loadUsers() {
    const result = await request("/users", { auth: false });
    if (!isSuccess(result.code) || !Array.isArray(result.data)) {
        return result;
    }
    const map = new Map();
    for (const item of result.data) {
        if (item?.user?.id != null) {
            map.set(Number(item.user.id), item.user.name || `用户#${item.user.id}`);
        }
    }
    setUserMap(map);
    return result;
}

export async function loadTeams(keyword) {
    const value = String(keyword || "").trim();
    if (value) {
        return request("/info/search", {
            method: "POST",
            auth: false,
            body: { content: value }
        });
    }
    return request("/teams", { auth: false });
}
