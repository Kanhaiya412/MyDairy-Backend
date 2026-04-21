package com.MyFarmerApp.MyFarmer.ai.models;

public enum IntentType {
    // Reports & Analytics
    TODAY_MILK,
    DATE_REPORT,
    MONTHLY_REPORT,
    YEARLY_REPORT,
    CUSTOM_RANGE_REPORT,
    DAY_WISE_REPORT,
    COMPARE_PREVIOUS,
    ANALYTICS_QUERY,
    
    // Entities
    PAYMENT_REPORT,
    EMPLOYEE_REPORT,
    CATTLE_REPORT,
    
    // Conversation Management
    VERIFY_PREVIOUS_RESPONSE, // "sahi hai?", "pakka?"
    FOLLOW_UP_QUERY,          // "day wise bhi", "aur detail do"
    DETAILS_REQUEST,
    CONFIRMATION_QUERY,
    
    // Fallbacks
    GLOBAL_AI_QUERY,          // General farming tips
    UNKNOWN,
    UNKNOWN_UNSUPPORTED
}
