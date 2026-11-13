package com.example.attendance.service;

import com.example.attendance.dto.LoginRequest;
import com.example.attendance.dto.LoginResponse;
import com.example.attendance.entity.User;
import com.example.attendance.exception.ApiException;
import com.example.attendance.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String INVALID_LOGIN = "Invalid username or password";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** Checks username, password and the selected login type (Admin / Student). */
    public User authenticate(LoginRequest request) {
        User user = userRepository.findByUsernameIgnoreCase(request.username().trim())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, INVALID_LOGIN));

        boolean passwordMatches = passwordEncoder.matches(request.password(), user.getPassword());
        if (!passwordMatches || user.getRole() != request.role()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, INVALID_LOGIN);
        }
        return user;
    }

    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Please login to continue"));
    }

    public LoginResponse toResponse(User user) {
        return new LoginResponse(user.getId(), user.getUsername(), user.getFullName(), user.getRole());
    }
}
