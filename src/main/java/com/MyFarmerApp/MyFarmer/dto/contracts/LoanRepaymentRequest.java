package com.MyFarmerApp.MyFarmer.dto.contracts;

import lombok.Data;

@Data
public class LoanRepaymentRequest {
    private Double amount;
    private String date; // yyyy-MM-dd
    private String notes;
}
