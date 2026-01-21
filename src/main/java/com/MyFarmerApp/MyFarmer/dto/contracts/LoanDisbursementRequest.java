package com.MyFarmerApp.MyFarmer.dto.contracts;

import lombok.*;

@Data
public class LoanDisbursementRequest {
    private Double amount;
    private String date; // yyyy-MM-dd
    private String reason;
    private String notes;
}
