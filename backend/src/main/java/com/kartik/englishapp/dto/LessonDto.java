package com.kartik.englishapp.dto;

public record LessonDto(
        Long id,
        int chapterNo,
        String chapterTitle,
        String tenseName,
        String levelBand,
        String lessonText,
        String practiceQuestion,
        String sampleAnswer,
        boolean completed,
        int score
) {}
