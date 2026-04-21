package com.MyFarmerApp.MyFarmer.ai.response;

import com.MyFarmerApp.MyFarmer.ai.models.GroqModels;
import com.MyFarmerApp.MyFarmer.ai.response.models.InsightResult;
import com.MyFarmerApp.MyFarmer.ai.response.models.NextAction;
import com.MyFarmerApp.MyFarmer.ai.response.models.ResponseStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
public class ResponsePolicyService {

    private final RestClient groqRestClient;

    @Value("${groq.model}")
    private String groqModel;

    public ResponsePolicyService(RestClient groqRestClient) {
        this.groqRestClient = groqRestClient;
    }

    public String generateFinalResponse(
            String rawData, 
            InsightResult insight, 
            NextAction nextAction,
            ResponseStrategy strategy,
            String userQuery,
            String language
    ) {
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are the 'MyDairy Executive Assistant', a premium AI for business owners.\n");
        prompt.append("RULES:\n");
        prompt.append("1. STRICT TONE: Answer professionally. Use 'Aap' and 'Ji'. NEVER use slang like 'Bhaiya', 'Behna'.\n");
        prompt.append("2. NO HALLUCINATIONS: Only use the data provided below.\n");
        prompt.append("3. SCRIPT: Match the user's language (Hindi/Hinglish/English).\n");
        
        switch (strategy) {
            case CLARIFICATION_REQUIRED -> prompt.append("4. GOAL: The user forgot to provide details. Ask them a specific clarification question based on data context.\n");
            case RECOMMENDATION_ATTACHED -> prompt.append("4. GOAL: Provide the data, then politely offer the <NEXT_ACTION> at the end of your response.\n");
            case COMPARISON_INSIGHT, ANOMALY_DETECTED -> prompt.append("4. GOAL: Highlight the <INSIGHTS> clearly at the beginning, then show supporting data.\n");
            default -> prompt.append("4. GOAL: Provide a clear, concise summary of the data.\n");
        }

        if (rawData != null && !rawData.isEmpty()) {
            prompt.append("\n<DATA>\n").append(rawData).append("\n</DATA>\n");
        }

        if (insight != null && insight.isHasInsights()) {
            prompt.append("\n<INSIGHTS>\n");
            if (insight.getInsightSummary() != null) prompt.append(insight.getInsightSummary()).append("\n");
            if (insight.getAnomalyDetails() != null) prompt.append(insight.getAnomalyDetails()).append("\n");
            prompt.append("</INSIGHTS>\n");
        }

        if (nextAction != null && nextAction.isHasRecommendation()) {
            prompt.append("\n<NEXT_ACTION>\n");
            prompt.append(nextAction.getSuggestedPrompt()).append("\n");
            prompt.append("</NEXT_ACTION>\n");
        }

        return callGroq(prompt.toString(), userQuery);
    }
    
    public String generateGlobalKnowledge(String userQuery) {
         String systemPrompt = """
            You are the "MyDairy Executive Assistant", a highly professional expert in dairy farming practices.
            The user is asking a general knowledge question.
            
            RULES:
            1. STRICT TONE: Answer professionally. Use "Aap" and "Ji". NEVER use informal slang like "Bhaiya", "Behna", "Bhai".
            2. ONLY answer questions related to dairy, farming, cattle, agriculture, or milk.
            3. Do NOT answer political, medical (human), or off-topic queries.
            4. Give practical, structured, actionable advice.
            5. Respond in the user's script (Hindi, Hinglish, or English) but maintain the formal executive tone.
            """;
         return callGroq(systemPrompt, userQuery);
    }

    private String callGroq(String systemPrompt, String userQuery) {
        GroqModels.ChatRequest request = GroqModels.ChatRequest.builder()
                .model(groqModel)
                .temperature(0.3) // Lower temperature for professional output
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

        return "Main abhi thodi der ke liye uplabdh nahi hu. Kripya kuch minute baad dubara try karein.";
    }
}
