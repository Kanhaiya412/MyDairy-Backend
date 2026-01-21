// src/main/java/com/MyFarmerApp/MyFarmer/dto/labour/LabourEventDTO.java
package com.MyFarmerApp.MyFarmer.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class LabourEventDTO {

    private LocalDate date;
    private String type;        // CONTRACT_PAID, LOAN_DISBURSEMENT, LOAN_REPAYMENT, SALARY_PAID, PENALTY, ADVANCE
    private Double amount;
    private String description; // small text shown under amount
}
