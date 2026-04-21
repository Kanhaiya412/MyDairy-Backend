package com.MyFarmerApp.MyFarmer.ai.nlu;

import com.MyFarmerApp.MyFarmer.ai.models.GroqModels;
import com.MyFarmerApp.MyFarmer.ai.models.IntentType;
import com.MyFarmerApp.MyFarmer.ai.models.NluResult;
import com.MyFarmerApp.MyFarmer.ai.memory.SessionContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class NluEngine {

    private final RestClient groqRestClient;
    private final ObjectMapper objectMapper;

    @Value("${groq.model}")
    private String groqModel;

    public NluEngine(RestClient groqRestClient, ObjectMapper objectMapper) {
        this.groqRestClient = groqRestClient;
        this.objectMapper = objectMapper;
    }

    public NluResult parseIntentAndEntities(String userMessage, SessionContext currentContext) {
        String today = LocalDate.now().toString();

        String contextStr = "{}";
        if (currentContext != null) {
            try {
                contextStr = objectMapper.writeValueAsString(currentContext);
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize context", e);
            }
        }

        String systemPrompt = """
            You are an expert Natural Language Understanding (NLU) engine for a Dairy Farm Management App in India. 
            Timezone context: Asia/Kolkata. TODAY'S DATE IS: %s
            ACTIVE CONTEXT (If any): %s
            
            Your goal is to parse user queries (in Hindi, Hinglish, or English) into a strict JSON object mapping to backend architectures.
            
            Strictly output this JSON schema:
            {
              "intent": "TODAY_MILK" | "DATE_REPORT" | "MONTHLY_REPORT" | "YEARLY_REPORT" | "CUSTOM_RANGE_REPORT" | "DAY_WISE_REPORT" | "COMPARE_PREVIOUS" | "ANALYTICS_QUERY" | "PAYMENT_REPORT" | "EMPLOYEE_REPORT" | "CATTLE_REPORT" | "VERIFY_PREVIOUS_RESPONSE" | "FOLLOW_UP_QUERY" | "DETAILS_REQUEST" | "CONFIRMATION_QUERY" | "GLOBAL_AI_QUERY" | "UNKNOWN" | "UNKNOWN_UNSUPPORTED",
              "startDate": "YYYY-MM-DD" (or null),
              "endDate": "YYYY-MM-DD" (or null),
              "month": integer 1-12 (or null),
              "year": integer (or null),
              "isDayWise": boolean,
              "targetEntity": "string" (or null),
              "comparisonTarget": "string" (or null),
              "verificationReference": "string" (or null),
              "reportMode": "summary" | "detailed" (or null),
              "detectedLanguage": "HI" | "EN" | "HINGLISH",
              "isFollowUp": boolean,
              "needsClarification": boolean,
              "clarificationMessage": "string" (or null)
            }
            
            RULES for Context & Intent Resolution:
            1. If asking for "pure 2026 ka", "saal bhar ka", "pura saal" -> Intent: YEARLY_REPORT, year: <year>.
            2. If asking for "march ka" -> Intent: MONTHLY_REPORT, month: 3, year: <current_year_unless_specified>.
            3. "is it right", "sahi hai?", "pakka?" -> Intent: VERIFY_PREVIOUS_RESPONSE, isFollowUp: true.
            4. "aur detail batao", "same report" -> Intent: DETAILS_REQUEST (or FOLLOW_UP_QUERY), isFollowUp: true.
            5. "day wise bhi", "din ke hisab se" -> Intent: DAY_WISE_REPORT, isDayWise: true, isFollowUp: true.
            6. "kal ka bhi", "uska bhi", "compare karo" -> Intent: COMPARE_PREVIOUS, isFollowUp: true, comparisonTarget: "previous_period".
            7. "aj ka dud", "aaj ka doodh" -> Intent: TODAY_MILK. (Do not set dates, routing handles it).
            8. "pichle hafte ka doodh", "7 din ka hisab" -> Intent: CUSTOM_RANGE_REPORT, set startDate and endDate appropriately.
            9. If "isFollowUp" is true, DO NOT overwrite/invent fields that the user didn't mention. Leave them null so they inherit from ACTIVE CONTEXT.
            10. If query is ambiguous without context -> needsClarification: true, generating a safe standard Hindi/Hinglish `clarificationMessage` (e.g. "Aap kis cheez ki report dekhna chahte hain?").
            11. Never hallucinate intents. If completely off-topic -> Intent: UNKNOWN.
            """.formatted(today, contextStr);

        GroqModels.ChatRequest request = GroqModels.ChatRequest.builder()
                .model(groqModel)
                .temperature(0.0) // 0 for deterministic NLU extraction
                .responseFormat(Map.of("type", "json_object"))
                .messages(List.of(
                        GroqModels.Message.builder().role("system").content(systemPrompt).build(),
                        GroqModels.Message.builder().role("user").content(userMessage).build()
                ))
                .build();

        try {
            log.info("Calling Groq NLU for query: {}", userMessage);
            GroqModels.ChatResponse response = groqRestClient.post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(GroqModels.ChatResponse.class);

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                String jsonContent = response.getChoices().get(0).getMessage().getContent();
                log.info("NLU Output JSON: {}", jsonContent);
                return objectMapper.readValue(jsonContent, NluResult.class);
            }
        } catch (Exception e) {
            log.error("NLU Engine processing failed", e);
        }

        // Fallback
        return NluResult.builder()
                .intent(IntentType.UNKNOWN_UNSUPPORTED)
                .rawQuery(userMessage)
                .build();
    }
}
