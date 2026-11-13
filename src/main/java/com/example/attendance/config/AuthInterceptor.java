package com.example.attendance.config;

import com.example.attendance.entity.Role;
import com.example.attendance.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Runs before every /api/** request (except login/logout).
 *  - /api/me/**  -> only logged-in STUDENT
 *  - everything else -> only logged-in ADMIN (teacher)
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_ROLE = "role";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        Role role = (session == null) ? null : (Role) session.getAttribute(SESSION_ROLE);

        if (role == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Please login to continue");
        }

        boolean studentArea = request.getRequestURI().startsWith("/api/me/");
        if (studentArea && role != Role.STUDENT) {
            throw new ApiException(HttpStatus.FORBIDDEN, "This page is only for students");
        }
        if (!studentArea && role != Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only Admin / Teacher can access this");
        }
        return true;
    }
}
