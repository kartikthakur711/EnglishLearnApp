package com.kartik.englishapp.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank String loginId,
        @NotBlank String password,
        @NotBlank String displayName,
        @NotBlank String levelBand
) {}
