package com.MyFarmerApp.MyFarmer.dto;

import lombok.Data;

@Data
public class SalaryAdvanceRequest {
    private Long labourId;
    private Double amount;
    private String date;
    private String remarks;
}
