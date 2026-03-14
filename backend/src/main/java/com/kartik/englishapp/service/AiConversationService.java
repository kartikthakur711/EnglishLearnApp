package com.kartik.englishapp.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kartik.englishapp.dto.AiChatRequest;
import com.kartik.englishapp.dto.AiChatResponse;
import com.kartik.englishapp.dto.ChatHistoryItem;
import com.kartik.englishapp.model.ChatEntry;
import com.kartik.englishapp.model.UserAccount;
import com.kartik.englishapp.repository.ChatEntryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class AiConversationService {
    private final ChatEntryRepository chatEntryRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final String provider;
    private final String openAiApiKey;
    private final String openAiModel;

    public AiConversationService(ChatEntryRepository chatEntryRepository,
                                 ObjectMapper objectMapper,
                                 @Value("${app.ai.provider:LOCAL}") String provider,
                                 @Value("${app.ai.openai-api-key:}") String openAiApiKey,
                                 @Value("${app.ai.openai-model:gpt-4o-mini}") String openAiModel) {
        this.chatEntryRepository = chatEntryRepository;
        this.objectMapper = objectMapper;
        this.provider = provider;
        this.openAiApiKey = openAiApiKey;
        this.openAiModel = openAiModel;
    }

    public AiChatResponse chat(UserAccount user, AiChatRequest request) {
        String userMessage = request.message().trim();
        String mode = request.voiceInput() ? "VOICE" : "TYPE";

        save(user, "USER", userMessage, mode);

        String reply = generateReply(userMessage, user.getLevelBand().name());
        save(user, "AI", reply, "VOICE_AND_TYPE");

        String practice = "Try this: Write one sentence in " +
                (user.getLevelBand().name().equals("PRO") ? "mixed conditional tense." : "present perfect continuous tense.");

        return new AiChatResponse(reply, reply, mode, practice);
    }

    public List<ChatHistoryItem> recent(UserAccount user) {
        return chatEntryRepository.findTop20ByUserOrderByCreatedAtDesc(user).stream()
                .map(item -> new ChatHistoryItem(item.getRole(), item.getContent(), item.getInputMode(), item.getCreatedAt().toString()))
                .toList();
    }

    private void save(UserAccount user, String role, String content, String inputMode) {
        ChatEntry entry = new ChatEntry();
        entry.setUser(user);
        entry.setRole(role);
        entry.setContent(content);
        entry.setInputMode(inputMode);
        entry.setCreatedAt(Instant.now());
        chatEntryRepository.save(entry);
    }

    private String generateReply(String message, String levelBand) {
        if ("OPENAI".equalsIgnoreCase(provider) && !openAiApiKey.isBlank()) {
            String response = callOpenAi(message, levelBand);
            if (response != null && !response.isBlank()) {
                return response;
            }
        }
        return localReply(message, levelBand);
    }

    private String callOpenAi(String message, String levelBand) {
        try {
            String prompt = "You are an English tense tutor. Level: " + levelBand +
                    ". Give concise chapter-wise explanation, one corrected sentence, and one follow-up practice question. User: " + message;

            String payload = objectMapper.writeValueAsString(objectMapper.createObjectNode()
                    .put("model", openAiModel)
                    .set("input", objectMapper.createArrayNode()
                            .add(objectMapper.createObjectNode()
                                    .put("role", "user")
                                    .put("content", prompt))));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/responses"))
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return null;
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode outputText = root.path("output_text");
            if (outputText.isTextual() && !outputText.asText().isBlank()) {
                return outputText.asText();
            }
            JsonNode output = root.path("output");
            if (output.isArray() && output.size() > 0) {
                for (JsonNode item : output) {
                    JsonNode content = item.path("content");
                    if (content.isArray()) {
                        for (JsonNode c : content) {
                            JsonNode text = c.path("text");
                            if (text.isTextual() && !text.asText().isBlank()) {
                                return text.asText();
                            }
                        }
                    }
                }
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    private String localReply(String message, String levelBand) {
        String lower = message.toLowerCase(Locale.ROOT);
        if (lower.contains("difference") && lower.contains("past")) {
            return "Past simple states completed action, while past continuous shows ongoing action in past context.";
        }
        if (lower.contains("example") || lower.contains("sentence")) {
            if ("PRO".equals(levelBand)) {
                return "Pro example: If she had been preparing regularly, she would have been delivering flawless presentations by now.";
            }
            return "Mid example: I have been studying English for two hours.";
        }
        if (lower.contains("question")) {
            return "Ask me any tense rule and I will break it chapter-wise with one easy and one advanced example.";
        }
        return "Good question. Focus on tense timeline, helper verb, and verb form. I can also speak this explanation for practice.";
    }
}
