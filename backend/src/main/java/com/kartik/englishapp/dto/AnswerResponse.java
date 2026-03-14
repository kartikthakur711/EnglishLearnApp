package com.kartik.englishapp.dto;

public record AnswerResponse(
        int score,
        boolean completed,
        String feedback,
        int streak,
        int totalPoints
) {}
