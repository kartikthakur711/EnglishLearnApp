package com.kartik.englishapp.dto;

public record ChatHistoryItem(
        String role,
        String content,
        String inputMode,
        String createdAt
) {}
