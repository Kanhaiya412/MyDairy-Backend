// src/main/java/com/MyFarmerApp/MyFarmer/dto/labour/LabourDashboardDTO.java
package com.MyFarmerApp.MyFarmer.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class LabourDashboardDTO {

    // BASIC INFO
    private Long labourId;
    private String labourName;
    private String mobile;
    private String wageType;        // DAILY / MONTHLY / YEARLY (from contractType or wageType)
    private Double dailyWage;
    private Double monthlySalary;

    // ACTIVE CONTRACT
    private Long activeContractId;
    private String contractType;    // YEARLY / MONTHLY / DAILY
    private Double contractAmount;
    private LocalDate contractStartDate;
    private LocalDate contractEndDate;

    // LOAN SUMMARY (EXTRA BORROWED)
    private Double totalDisbursed;      // principal
    private Double totalRepaid;
    private Double outstandingPrincipal;
    private Double totalInterest;
    private Double outstandingWithInterest;

    // SALARY SUMMARY
    private Double totalSalaryPaid;

    // PENALTY SUMMARY
    private Double totalPenaltyUnpaid;
    private Double totalPenaltyPaid;

    // TIMELINE
    private List<LabourEventDTO> timeline;
}
