package com.kartik.englishapp.service;

import com.kartik.englishapp.config.AppException;
import com.kartik.englishapp.dto.AuthResponse;
import com.kartik.englishapp.dto.LoginRequest;
import com.kartik.englishapp.dto.RegisterRequest;
import com.kartik.englishapp.model.LevelBand;
import com.kartik.englishapp.model.UserAccount;
import com.kartik.englishapp.repository.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserAccountRepository userRepo;
    private final SessionService sessionService;
    private final StreakService streakService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserAccountRepository userRepo,
                       SessionService sessionService,
                       StreakService streakService,
                       PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.sessionService = sessionService;
        this.streakService = streakService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request) {
        String loginId = request.loginId().trim();
        userRepo.findByLoginId(loginId).ifPresent(u -> {
            throw new AppException("Login ID already exists");
        });

        LevelBand levelBand = parseLevel(request.levelBand());

        UserAccount user = new UserAccount();
        user.setLoginId(loginId);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName().trim());
        user.setLevelBand(levelBand);
        user.setDailyStreak(0);
        user.setTotalPoints(0);
        streakService.updateForToday(user);
        UserAccount saved = userRepo.save(user);

        String token = sessionService.createToken(saved);
        return toAuthResponse(saved, token);
    }

    public AuthResponse login(LoginRequest request) {
        UserAccount user = userRepo.findByLoginId(request.loginId().trim())
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "Invalid login ID or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AppException(HttpStatus.UNAUTHORIZED, "Invalid login ID or password");
        }

        streakService.updateForToday(user);
        UserAccount saved = userRepo.save(user);
        String token = sessionService.createToken(saved);
        return toAuthResponse(saved, token);
    }

    private LevelBand parseLevel(String value) {
        try {
            return LevelBand.valueOf(value.toUpperCase());
        } catch (Exception ex) {
            throw new AppException("Level must be MID or PRO");
        }
    }

    public AuthResponse toAuthResponse(UserAccount user, String token) {
        return new AuthResponse(
                token,
                user.getLoginId(),
                user.getDisplayName(),
                user.getLevelBand().name(),
                user.getDailyStreak(),
                user.getTotalPoints()
        );
    }
}
