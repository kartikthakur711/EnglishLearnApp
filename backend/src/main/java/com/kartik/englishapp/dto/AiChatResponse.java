package com.kartik.englishapp.dto;

public record AiChatResponse(
        String reply,
        String speakText,
        String mode,
        String suggestedPractice
) {}
