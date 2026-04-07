package com.MyFarmerApp.MyFarmer.repository.projection;

/**
 * High-performance summary projection for the Labour Dashboard.
 * Consolidates all-time metrics into a single row to eliminate the waterfall fetch.
 */
public interface LabourDashboardSummary {
    
    Long getLabourId();
    String getLabourName();
    String getMobile();
    String getPhotoUrl();
    String getStatus();
    String getJoiningDate();
    String getEndDate();
    
    // Wage Details
    String getWageType();
    Double getDailyWage();
    Double getMonthlySalary();
    Double getYearlySalary();
    Integer getAllowedLeaves();
    
    // Active Contract
    Long getActiveContractId();
    String getActiveContractType();
    Double getActiveContractAmount();
    String getActiveContractStartDate();
    String getActiveContractEndDate();
    
    // Totals
    Double getTotalDisbursed();
    Double getTotalRepaid();
    Double getTotalSalaryPaid();
    Double getTotalPenaltyPaid();
    Double getTotalPenaltyUnpaid();
    Long getTotalPresentDays();
    Long getTotalAbsentDays();
    Long getTotalHalfDays();
}
