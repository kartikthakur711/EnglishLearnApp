package com.kartik.englishapp.dto;

public record AuthResponse(
        String token,
        String loginId,
        String displayName,
        String levelBand,
        int streak,
        int totalPoints
) {}
