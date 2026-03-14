package com.kartik.englishapp.dto;

public record StreakResponse(
        int streak,
        int totalPoints,
        String lastActiveDate
) {}
