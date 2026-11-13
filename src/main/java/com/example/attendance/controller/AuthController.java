package com.example.attendance.controller;

import com.example.attendance.config.AuthInterceptor;
import com.example.attendance.dto.LoginRequest;
import com.example.attendance.dto.LoginResponse;
import com.example.attendance.dto.MessageResponse;
import com.example.attendance.entity.User;
import com.example.attendance.exception.ApiException;
import com.example.attendance.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        User user = authService.authenticate(request);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(AuthInterceptor.SESSION_USER_ID, user.getId());
        session.setAttribute(AuthInterceptor.SESSION_ROLE, user.getRole());
        return authService.toResponse(user);
    }

    @PostMapping("/logout")
    public MessageResponse logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return new MessageResponse("Logged out successfully");
    }

    /** Used by every page to find out who is logged in. */
    @GetMapping("/me")
    public LoginResponse me(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        Long userId = (session == null) ? null : (Long) session.getAttribute(AuthInterceptor.SESSION_USER_ID);
        if (userId == null) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Please login to continue");
        }
        return authService.toResponse(authService.getUser(userId));
    }
}
