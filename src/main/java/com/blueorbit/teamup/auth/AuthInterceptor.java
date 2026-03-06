package com.blueorbit.teamup.auth;

import com.blueorbit.teamup.controller.Code;
import com.blueorbit.teamup.controller.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final SessionTokenService sessionTokenService;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(SessionTokenService sessionTokenService, ObjectMapper objectMapper) {
        this.sessionTokenService = sessionTokenService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (isPublicRequest(request)) {
            return true;
        }
        String auth = request.getHeader(AuthConstants.AUTH_HEADER);
        if (auth == null || !auth.startsWith(AuthConstants.BEARER_PREFIX)) {
            writeUnauthorized(response, "Missing bearer token");
            return false;
        }
        String token = auth.substring(AuthConstants.BEARER_PREFIX.length()).trim();
        Long uid = sessionTokenService.verifyAndGetUid(token);
        if (uid == null) {
            writeUnauthorized(response, "Invalid or expired token");
            return false;
        }
        request.setAttribute(AuthConstants.CURRENT_USER_ID_ATTR, uid);
        return true;
    }

    private boolean isPublicRequest(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();
        if (HttpMethod.OPTIONS.matches(method) || HttpMethod.GET.matches(method)) {
            return true;
        }
        if ("/login".equals(path) && HttpMethod.POST.matches(method)) {
            return true;
        }
        if ("/users".equals(path) && HttpMethod.POST.matches(method)) {
            return true;
        }
        if ("/info/search".equals(path) && HttpMethod.POST.matches(method)) {
            return true;
        }
        return false;
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(new Result(Code.AUTH_ERR, null, msg)));
    }
}
