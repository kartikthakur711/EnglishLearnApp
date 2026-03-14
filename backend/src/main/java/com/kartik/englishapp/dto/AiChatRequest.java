package com.kartik.englishapp.dto;

import jakarta.validation.constraints.NotBlank;

public record AiChatRequest(
        @NotBlank String message,
        boolean voiceInput
) {}
