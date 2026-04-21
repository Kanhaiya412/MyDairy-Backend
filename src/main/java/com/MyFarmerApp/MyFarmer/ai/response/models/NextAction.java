package com.MyFarmerApp.MyFarmer.ai.response.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NextAction {
    private boolean hasRecommendation;
    private String suggestedPrompt; 
}
