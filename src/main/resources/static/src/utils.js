export const APPLICATION_STATE = {
    WAIT: 0,
    ACCEPT: 1,
    DECLINE: 2
};

export function isSuccess(code) {
    return typeof code === "number" && code % 10 === 1;
}

export function formatTime(value) {
    if (!value) {
        return "未知时间";
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return String(value);
    }
    return date.toLocaleString("zh-CN");
}

export function stateText(stateValue) {
    if (stateValue === APPLICATION_STATE.ACCEPT) {
        return "已通过";
    }
    if (stateValue === APPLICATION_STATE.DECLINE) {
        return "已拒绝";
    }
    return "待处理";
}

export function escapeHtml(input) {
    return String(input ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#39;");
}
