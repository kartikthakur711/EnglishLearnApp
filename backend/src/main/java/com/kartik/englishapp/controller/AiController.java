package com.kartik.englishapp.controller;

import com.kartik.englishapp.dto.AiChatRequest;
import com.kartik.englishapp.dto.AiChatResponse;
import com.kartik.englishapp.dto.ChatHistoryItem;
import com.kartik.englishapp.model.UserAccount;
import com.kartik.englishapp.service.AiConversationService;
import com.kartik.englishapp.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final SessionService sessionService;
    private final AiConversationService aiService;

    public AiController(SessionService sessionService, AiConversationService aiService) {
        this.sessionService = sessionService;
        this.aiService = aiService;
    }

    @PostMapping("/chat")
    public AiChatResponse chat(@RequestHeader("Authorization") String auth,
                               @Valid @RequestBody AiChatRequest request) {
        UserAccount user = sessionService.userFromAuthHeader(auth);
        return aiService.chat(user, request);
    }

    @GetMapping("/history")
    public List<ChatHistoryItem> history(@RequestHeader("Authorization") String auth) {
        UserAccount user = sessionService.userFromAuthHeader(auth);
        return aiService.recent(user);
    }
}
