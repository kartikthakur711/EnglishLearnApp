package com.kartik.englishapp.controller;

import com.kartik.englishapp.dto.StreakResponse;
import com.kartik.englishapp.model.UserAccount;
import com.kartik.englishapp.service.SessionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/streak")
public class StreakController {
    private final SessionService sessionService;

    public StreakController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @GetMapping
    public StreakResponse streak(@RequestHeader("Authorization") String auth) {
        UserAccount user = sessionService.userFromAuthHeader(auth);
        String last = user.getLastActiveDate() == null ? "" : user.getLastActiveDate().toString();
        return new StreakResponse(user.getDailyStreak(), user.getTotalPoints(), last);
    }
}
