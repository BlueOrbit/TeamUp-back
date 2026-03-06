const container = document.getElementById("toastContainer");

export function showToast(message, type = "info") {
    if (!container) {
        return;
    }
    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.textContent = message;
    container.appendChild(toast);
    window.setTimeout(() => toast.remove(), 2500);
}
