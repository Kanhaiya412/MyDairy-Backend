package com.MyFarmerApp.MyFarmer.ai.response;

import com.MyFarmerApp.MyFarmer.ai.response.models.NextAction;
import org.springframework.stereotype.Service;

@Service
public class ClarificationManager {

    public NextAction generateClarificationPrompt(String intentName) {
        if (intentName == null) {
            return NextAction.builder()
                    .hasRecommendation(true)
                    .suggestedPrompt("Aap exactly kis cheez ki detail chahte hain?")
                    .build();
        }

        if (intentName.contains("MILK") || intentName.contains("REPORT")) {
             return NextAction.builder()
                .hasRecommendation(true)
                .suggestedPrompt("Aapko kis date, mahine ya saal ka hisab dekhna hai? Kripya bataein (jaise: 'March 2026').")
                .build();
        } else if (intentName.contains("LABOUR") || intentName.contains("EMPLOYEE")) {
             return NextAction.builder()
                .hasRecommendation(true)
                .suggestedPrompt("Kripya staff ya labour ka naam aur samay bataein jiska record aapko dekhna hai.")
                .build();
        } else if (intentName.contains("CATTLE")) {
             return NextAction.builder()
                .hasRecommendation(true)
                .suggestedPrompt("Aap kis gai(cow) ya bhains(buffalo) ka record dekhna chahte hain?")
                .build();
        }

        return NextAction.builder()
            .hasRecommendation(true)
            .suggestedPrompt("Kripya apna sawal thoda aur clear karke bataein.")
            .build();
    }
}
