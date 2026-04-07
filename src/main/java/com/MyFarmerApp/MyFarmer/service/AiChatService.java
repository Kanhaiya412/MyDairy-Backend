package com.MyFarmerApp.MyFarmer.service;

import com.MyFarmerApp.MyFarmer.ai.AiIntentClassifier;
import com.MyFarmerApp.MyFarmer.ai.AiMilkQueryService;
import com.MyFarmerApp.MyFarmer.enums.AiIntent;
import com.MyFarmerApp.MyFarmer.util.MonthExtractor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiChatService {

    private final AiIntentClassifier intentClassifier;
    private final AiMilkQueryService milkQueryService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${groq.api-key}")
    private String groqApiKey;

    @Value("${groq.base-url}")
    private String groqBaseUrl;

    @Value("${groq.model}")
    private String groqModel;

    public AiChatService(
            AiIntentClassifier intentClassifier,
            AiMilkQueryService milkQueryService
    ) {
        this.intentClassifier = intentClassifier;
        this.milkQueryService = milkQueryService;
    }

    // ================= MAIN ENTRY =================
    public String askGroq(String message) {

        Long userId = extractUserIdFromSecurity();

        AiIntent intent = intentClassifier.classify(message);

        // ✅ REAL DATA FIRST
        if (intent != AiIntent.UNKNOWN) {

            // 🔹 extract month if required
            Integer month = null;

            if (intent == AiIntent.MONTHLY_MILK) {
                month = MonthExtractor.extractMonth(message);

                if (month == null) {
                    return "Please tell me which month you want (e.g. January milk data).";
                }
            }

            return milkQueryService.resolve(intent, userId, month);
        }

        // 🧠 FALLBACK → Groq for general questions
        return askGroqLLM(message);
    }

    // ================= SECURITY =================
    private Long extractUserIdFromSecurity() {
        // Username stored in JWT subject
        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // ⚠️ TEMP SOLUTION (Phase 1)
        // Replace later with UserRepository lookup
        return 1L;
    }

    // ================= GROQ CALL =================
    private String askGroqLLM(String prompt) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", groqModel);
        body.put("messages", List.of(
                Map.of("role", "system", "content",
                        "You are a helpful dairy assistant. Answer clearly."),
                Map.of("role", "user", "content", prompt)
        ));

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    groqBaseUrl + "/chat/completions",
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            Map<String, Object> choice =
                    (Map<String, Object>) ((List<?>) response.getBody()
                            .get("choices")).get(0);

            Map<String, Object> message =
                    (Map<String, Object>) choice.get("message");

            return message.get("content").toString();

        } catch (Exception e) {
            return "Sorry, I'm unable to answer that right now.";
        }
    }
}
