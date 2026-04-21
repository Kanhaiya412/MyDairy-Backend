package com.MyFarmerApp.MyFarmer.ai.response;

import com.MyFarmerApp.MyFarmer.ai.response.models.InsightResult;
import com.MyFarmerApp.MyFarmer.ai.response.models.NextAction;
import com.MyFarmerApp.MyFarmer.ai.response.models.ResponseStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResponseOrchestrator {

    private final ResponsePolicyService responsePolicyService;
    private final ClarificationManager clarificationManager;
    private final ReasoningEngine reasoningEngine;
    private final RecommendationEngine recommendationEngine;

    public String orchestrateResponse(String intentParam, String rawDbData, String userQuery, String language) {
        
        InsightResult insight = InsightResult.builder().hasInsights(false).build();
        NextAction nextAction = NextAction.builder().hasRecommendation(false).build();
        ResponseStrategy strategy = ResponseStrategy.DIRECT_ANSWER;

        // 1. Missing Data Check (Trigger Clarification)
        if (rawDbData == null || rawDbData.isEmpty() || rawDbData.equalsIgnoreCase("No records found")) {
            nextAction = clarificationManager.generateClarificationPrompt(intentParam);
            strategy = ResponseStrategy.CLARIFICATION_REQUIRED;
        } else {
            // 2. Reason over valid data
            insight = reasoningEngine.extractInsights(rawDbData);
            if (insight.isHasInsights() || insight.getAnomalyDetails() != null) {
                strategy = ResponseStrategy.ANOMALY_DETECTED;
            }

            // 3. Propose Next Best Action
            nextAction = recommendationEngine.getNextBestAction(intentParam, rawDbData);
            if (nextAction.isHasRecommendation() && strategy == ResponseStrategy.DIRECT_ANSWER) {
                strategy = ResponseStrategy.RECOMMENDATION_ATTACHED;
            }
        }
        
        return responsePolicyService.generateFinalResponse(rawDbData, insight, nextAction, strategy, userQuery, language);
    }
    
    public String orchestrateGlobal(String userQuery) {
        return responsePolicyService.generateGlobalKnowledge(userQuery);
    }
}
