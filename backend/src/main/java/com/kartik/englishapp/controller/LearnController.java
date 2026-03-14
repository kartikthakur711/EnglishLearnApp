package com.kartik.englishapp.controller;

import com.kartik.englishapp.dto.AnswerRequest;
import com.kartik.englishapp.dto.AnswerResponse;
import com.kartik.englishapp.dto.LessonDto;
import com.kartik.englishapp.service.LessonService;
import com.kartik.englishapp.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learn")
public class LearnController {
    private final SessionService sessionService;
    private final LessonService lessonService;

    public LearnController(SessionService sessionService, LessonService lessonService) {
        this.sessionService = sessionService;
        this.lessonService = lessonService;
    }

    @GetMapping("/chapters")
    public List<Integer> chapters(@RequestHeader("Authorization") String auth,
                                  @RequestParam(required = false) String level) {
        return lessonService.getChapterNumbers(sessionService.userFromAuthHeader(auth), level);
    }

    @GetMapping("/lessons")
    public List<LessonDto> lessons(@RequestHeader("Authorization") String auth,
                                   @RequestParam(required = false) String level,
                                   @RequestParam(required = false) Integer chapterNo) {
        return lessonService.getLessons(sessionService.userFromAuthHeader(auth), level, chapterNo);
    }

    @PostMapping("/answer")
    public AnswerResponse answer(@RequestHeader("Authorization") String auth,
                                 @Valid @RequestBody AnswerRequest request) {
        return lessonService.submitAnswer(sessionService.userFromAuthHeader(auth), request);
    }
}
