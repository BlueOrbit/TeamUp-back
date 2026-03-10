function normalizePath(raw) {
    const value = (raw || "").trim();
    if (!value) {
        return "/teams";
    }
    const [path] = value.split("?");
    const normalized = path.startsWith("/") ? path : `/${path}`;
    if (normalized.length > 1 && normalized.endsWith("/")) {
        return normalized.slice(0, -1);
    }
    return normalized;
}

export function createRouter({ app, routes, onRouteResolved }) {
    let currentController = null;

    const navigate = (path) => {
        const target = normalizePath(path);
        if (location.hash === `#${target}`) {
            handleRoute();
            return;
        }
        location.hash = `#${target}`;
    };

    const reload = () => {
        handleRoute();
    };

    const matchRoute = (path) => {
        for (const route of routes) {
            const match = path.match(route.pattern);
            if (match) {
                const params = route.mapParams ? route.mapParams(match) : {};
                return { route, params };
            }
        }
        return null;
    };

    const handleRoute = async () => {
        const path = normalizePath(location.hash.replace(/^#/, ""));
        const matched = matchRoute(path);
        if (!matched) {
            navigate("/teams");
            return;
        }
        if (currentController) {
            currentController.abort();
        }
        currentController = new AbortController();
        onRouteResolved(matched.route.label || "TeamUp");
        await matched.route.render({
            app,
            params: matched.params,
            navigate,
            reload,
            signal: currentController.signal
        });
    };

    window.addEventListener("hashchange", handleRoute);

    return {
        start() {
            if (!location.hash) {
                navigate("/teams");
            } else {
                handleRoute();
            }
        },
        navigate,
        reload
    };
}
