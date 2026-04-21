package com.MyFarmerApp.MyFarmer.ai.response.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InsightResult {
    private boolean hasInsights;
    private String insightSummary; 
    private String anomalyDetails; 
}
