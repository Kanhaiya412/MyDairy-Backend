package com.MyFarmerApp.MyFarmer.ai;

import com.MyFarmerApp.MyFarmer.enums.AiIntent;
import org.springframework.stereotype.Component;

@Component
public class AiIntentClassifier {

    public AiIntent classify(String message) {

        if (message == null) return AiIntent.UNKNOWN;

        String text = message.toLowerCase();

        if (text.contains("today") && text.contains("milk")) {
            return AiIntent.TODAY_MILK;
        }

        if (text.contains("last 7") || text.contains("week")) {
            return AiIntent.LAST_7_DAYS_MILK;
        }

        if (text.contains("month") || text.contains("monthly")) {
            return AiIntent.MONTHLY_MILK;
        }

        if (text.contains("cattle")) {
            return AiIntent.CATTLE_MILK;
        }

        if (
                text.contains("january") ||
                        text.contains("february") ||
                        text.contains("march") ||
                        text.contains("april") ||
                        text.contains("may") ||
                        text.contains("june") ||
                        text.contains("july") ||
                        text.contains("august") ||
                        text.contains("september") ||
                        text.contains("october") ||
                        text.contains("november") ||
                        text.contains("december") ||
                        text.contains("जनवरी") ||
                        text.contains("फरवरी") ||
                        text.contains("मार्च")
        ) {
            return AiIntent.MONTHLY_MILK;
        }

        return AiIntent.UNKNOWN;
    }
}
