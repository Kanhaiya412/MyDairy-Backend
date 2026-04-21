package com.MyFarmerApp.MyFarmer.ai.generation;

import com.MyFarmerApp.MyFarmer.ai.models.GroqModels;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
public class ResponseGenerator {

    private final RestClient groqRestClient;

    @Value("${groq.model}")
    private String groqModel;

    public ResponseGenerator(RestClient groqRestClient) {
        this.groqRestClient = groqRestClient;
    }

    public String generateDataResponse(String dbData, String userQuery, String language) {
        String systemPrompt = """
            You are the "MyDairy Executive Assistant", a highly professional business AI for dairy owners.
            Your sole job is to take the raw JSON or text data provided below and explain it to the user.
            
            RULES (CRITICAL - YOU MUST OBEY):
            1. DO NOT HALLUCINATE OR INVENT NUMBERS. Only use the values present in <DATA>.
            2. If <DATA> says "No records found", tell the user politely that there is no data for that period.
            3. STRICT TONE: Answer professionally. Use "Aap" and "Ji". NEVER use informal slang like "Bhaiya", "Behna", "Bhai".
            4. Be clear, predictable, and act as a reliable financial assistant.
            5. MATCH SCRIPT: If the user wrote in Hinglish, reply in formal professional Hinglish. If Hindi, use formal Hindi.
            
            <DATA>
            %s
            </DATA>
            """.formatted(dbData);

        return callGroq(systemPrompt, userQuery);
    }

    public String generateGlobalKnowledgeResponse(String userQuery) {
         String systemPrompt = """
            You are the "MyDairy Executive Assistant", a highly professional expert in dairy farming, cattle health, and milk production.
            The user is asking a general knowledge question about dairy.
            
            RULES:
            1. STRICT TONE: Answer professionally. Use "Aap" and "Ji". NEVER use informal slang like "Bhaiya", "Behna", "Bhai".
            2. ONLY answer questions related to dairy, farming, cattle, agriculture, or milk.
            3. Do NOT answer political, medical (human), or off-topic queries. Briefly say you only specialize in dairy business.
            4. Give practical, structured, actionable advice.
            5. Respond in the user's script (Hindi, Hinglish, or English) but maintain the formal executive tone.
            """;

         return callGroq(systemPrompt, userQuery);
    }

    private String callGroq(String systemPrompt, String userQuery) {
        GroqModels.ChatRequest request = GroqModels.ChatRequest.builder()
                .model(groqModel)
                .temperature(0.4) // slight creativity for natural text
                .messages(List.of(
                        GroqModels.Message.builder().role("system").content(systemPrompt).build(),
                        GroqModels.Message.builder().role("user").content(userQuery).build()
                ))
                .build();

        try {
            GroqModels.ChatResponse response = groqRestClient.post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(GroqModels.ChatResponse.class);

            if (response != null && response.getChoices() != null && !response.getChoices().isEmpty()) {
                return response.getChoices().get(0).getMessage().getContent();
            }
        } catch (Exception e) {
            log.error("AI Generation failed", e);
        }

        return "Main abhi temporary error face kar raha hu. Kripya thodi der baad try karein.";
    }
}
