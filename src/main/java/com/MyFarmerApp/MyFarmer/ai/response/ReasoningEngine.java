package com.MyFarmerApp.MyFarmer.ai.response;

import com.MyFarmerApp.MyFarmer.ai.response.models.InsightResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReasoningEngine {

    // Simulates deterministic math processing on raw DB outputs.
    // E.g., computing max(production), average(fat), variance.
    public InsightResult extractInsights(String dbData) {
        if (dbData == null || dbData.isEmpty() || dbData.equalsIgnoreCase("No records found")) {
            return InsightResult.builder().hasInsights(false).build();
        }

        boolean hasInsights = false;
        String anomalyMsg = null;
        String insightMsg = null;

        // Basic Native Trend Detection
        if (dbData.contains("totalQuantity") || dbData.contains("amount")) {
            hasInsights = true;
            insightMsg = "Is data mein totals maujood hain; please ensure LLM highlights the highest aggregated values accurately.";
            
            // Hard Anomaly Detection (E.g. finding '0' inside numeric fields using Java regex parsing)
            if (dbData.contains(": 0.0,") || dbData.contains(": 0,")) {
                anomalyMsg = "Dhyan dein: Data mein zero (0) value record ki gai hai. Ho sakta hai koi entry miss ho gai ho.";
            }
        }

        return InsightResult.builder()
                .hasInsights(hasInsights)
                .insightSummary(insightMsg)
                .anomalyDetails(anomalyMsg)
                .build();
    }
}
