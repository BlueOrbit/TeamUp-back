package com.blueorbit.teamup.auth;

import javax.servlet.http.HttpServletRequest;

public final class AuthHelper {
    private AuthHelper() {
    }

    public static Long currentUserId(HttpServletRequest request) {
        Object uid = request.getAttribute(AuthConstants.CURRENT_USER_ID_ATTR);
        if (uid instanceof Long) {
            return (Long) uid;
        }
        return null;
    }
}
