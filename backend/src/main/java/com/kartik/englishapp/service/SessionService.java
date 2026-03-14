package com.kartik.englishapp.service;

import com.kartik.englishapp.config.AppException;
import com.kartik.englishapp.config.JwtService;
import com.kartik.englishapp.model.UserAccount;
import com.kartik.englishapp.repository.UserAccountRepository;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class SessionService {
    private final UserAccountRepository userRepo;
    private final JwtService jwtService;

    public SessionService(UserAccountRepository userRepo, JwtService jwtService) {
        this.userRepo = userRepo;
        this.jwtService = jwtService;
    }

    public String createToken(UserAccount user) {
        return jwtService.issueToken(user.getId(), user.getLoginId());
    }

    public UserAccount userFromAuthHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Missing bearer token");
        }
        String token = authHeader.substring("Bearer ".length()).trim();
        final Long userId;
        try {
            userId = jwtService.parseUserId(token);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Invalid or expired session token");
        }
        return userRepo.findById(userId)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "User not found"));
    }
}
