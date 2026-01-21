package com.MyFarmerApp.MyFarmer.dto.contracts;

import lombok.Data;

@Data
public class CreateContractRequest {
    private Long labourId;
    private String contractType; // YEARLY | MONTHLY | DAILY
    private Double contractAmount;
    private String startDate;
    private String endDate;
    private Integer allowedLeaves;
    private Double monthlyInterestRate;
}
