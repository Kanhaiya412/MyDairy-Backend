package com.MyFarmerApp.MyFarmer.dto;

import lombok.*;

@Data
public class LabourSalaryGenerateRequest {
    private Long labourId;
    private Integer month;
    private Integer year;

    private Integer manualDays;   // optional
    private Boolean fullMonth;    // optional
}
