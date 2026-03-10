export function teamListSkeleton(count = 4) {
    const cards = Array.from({ length: count })
        .map(() => `
            <div class="skeleton-card">
                <div class="skeleton skeleton-line w-50"></div>
                <div class="skeleton skeleton-line w-30"></div>
                <div class="skeleton skeleton-line w-100"></div>
                <div class="skeleton skeleton-line w-70"></div>
            </div>
        `)
        .join("");
    return `<div class="team-list">${cards}</div>`;
}

export function detailSkeleton() {
    return `
        <section class="card detail-grid">
            <div class="skeleton skeleton-line w-30"></div>
            <div class="skeleton skeleton-line w-70"></div>
            <div class="skeleton skeleton-line w-100"></div>
            <div class="skeleton skeleton-line w-100"></div>
            <div class="detail-columns">
                <div class="skeleton-card">
                    <div class="skeleton skeleton-line w-50"></div>
                    <div class="skeleton skeleton-line w-100"></div>
                    <div class="skeleton skeleton-line w-70"></div>
                </div>
                <div class="skeleton-card">
                    <div class="skeleton skeleton-line w-50"></div>
                    <div class="skeleton skeleton-line w-100"></div>
                    <div class="skeleton skeleton-line w-70"></div>
                </div>
            </div>
        </section>
    `;
}
