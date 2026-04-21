package com.MyFarmerApp.MyFarmer.ai.response;

import com.MyFarmerApp.MyFarmer.ai.response.models.NextAction;
import org.springframework.stereotype.Service;

@Service
public class RecommendationEngine {

    public NextAction getNextBestAction(String intentName, String rawDbData) {
        if (intentName == null) return NextAction.builder().hasRecommendation(false).build();

        if (intentName.equals("YEARLY_REPORT")) {
            return NextAction.builder()
                    .hasRecommendation(true)
                    .suggestedPrompt("Kya aap month-wise comparison dekhna chahenge?")
                    .build();
        } else if (intentName.equals("MONTHLY_REPORT")) {
            return NextAction.builder()
                    .hasRecommendation(true)
                    .suggestedPrompt("Kya main is mahine ka highest production day detail batau?")
                    .build();
        } else if (intentName.contains("CATTLE_REPORT") && rawDbData.contains("low yield")) {
            return NextAction.builder()
                    .hasRecommendation(true)
                    .suggestedPrompt("Production thodi low hai. Kya aap diet/health tips dekhna chahenge?")
                    .build();
        }

        return NextAction.builder()
                .hasRecommendation(false)
                .build();
    }
}
