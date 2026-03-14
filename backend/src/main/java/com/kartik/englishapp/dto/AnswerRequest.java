package com.kartik.englishapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnswerRequest(
        @NotNull Long lessonId,
        @NotBlank String answerText
) {}
