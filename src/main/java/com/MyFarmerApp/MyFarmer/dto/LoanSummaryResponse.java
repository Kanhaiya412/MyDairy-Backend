package com.MyFarmerApp.MyFarmer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder

@NoArgsConstructor
@AllArgsConstructor
public class LoanSummaryResponse {
    private Long accountId;
    private Double totalDisbursed;
    private Double totalRepaid;
    private Double totalInterest;
    private Double outstandingAmount;
}