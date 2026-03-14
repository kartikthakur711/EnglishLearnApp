package com.kartik.englishapp;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void streakEndpointRequiresBearerToken() throws Exception {
        mockMvc.perform(get("/api/streak"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void repeatedCompletedSubmissionDoesNotFarmPoints() throws Exception {
        AuthContext auth = registerAndGetAuth();
        long lessonId = firstLessonId(auth.token());

        String strongAnswer = "If I had been practicing, I would have improved by this time already.";
        int pointsAfterFirst = submitAnswer(auth.token(), lessonId, strongAnswer).path("totalPoints").asInt();
        int pointsAfterSecond = submitAnswer(auth.token(), lessonId, strongAnswer).path("totalPoints").asInt();

        assertThat(pointsAfterFirst).isEqualTo(pointsAfterSecond);
    }

    @Test
    void progressingFromIncompleteToCompleteAwardsOnlyUpgradeBonus() throws Exception {
        AuthContext auth = registerAndGetAuth();
        long lessonId = firstLessonId(auth.token());

        JsonNode first = submitAnswer(auth.token(), lessonId, "test");
        JsonNode second = submitAnswer(auth.token(), lessonId, "If I had been practicing, I would have improved by this time already.");

        int pointsAfterFirst = first.path("totalPoints").asInt();
        int pointsAfterSecond = second.path("totalPoints").asInt();
        assertThat(pointsAfterSecond - pointsAfterFirst).isEqualTo(10);
    }

    private AuthContext registerAndGetAuth() throws Exception {
        String loginId = "u" + System.nanoTime();
        String payload = """
                {
                  "loginId":"%s",
                  "password":"pass12345",
                  "displayName":"Test User",
                  "levelBand":"MID"
                }
                """.formatted(loginId);

        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = objectMapper.readTree(body);
        return new AuthContext(json.path("token").asText(), loginId);
    }

    private long firstLessonId(String token) throws Exception {
        String body = mockMvc.perform(get("/api/learn/lessons")
                        .param("level", "MID")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode lessons = objectMapper.readTree(body);
        return lessons.get(0).path("id").asLong();
    }

    private JsonNode submitAnswer(String token, long lessonId, String answer) throws Exception {
        String payload = """
                {
                  "lessonId": %d,
                  "answerText": "%s"
                }
                """.formatted(lessonId, answer.replace("\"", "\\\""));
        String body = mockMvc.perform(post("/api/learn/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body);
    }

    private record AuthContext(String token, String loginId) {
    }
}
