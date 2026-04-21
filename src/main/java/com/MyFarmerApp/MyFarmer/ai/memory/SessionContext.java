package com.MyFarmerApp.MyFarmer.ai.memory;

import com.MyFarmerApp.MyFarmer.ai.models.IntentType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Structured Business Memory Object
 */
@Data
@Builder
public class SessionContext {
    private Long userId;
    private IntentType lastIntent;
    
    // Slot Variables
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isDayWise;
    private String targetEntity; // e.g., "Gauri" (cow name) or "Ramu" (labourer)
    
    // Explicit Entity Tracking for follow-ups
    private Integer lastMonth;
    private Integer lastYear;
    private String comparisonTarget;      // e.g., "last_month"
    private String verificationReference; // hash or type of what was just checked
    private String lastResponseType;      // e.g., "table", "summary", "number"
    
    // TTL tracking
    @Builder.Default
    private LocalDateTime lastAccessedAt = LocalDateTime.now();

    public void updateAccessTime() {
        this.lastAccessedAt = LocalDateTime.now();
    }
}
