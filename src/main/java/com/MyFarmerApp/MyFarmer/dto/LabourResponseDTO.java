// src/main/java/com/MyFarmerApp/MyFarmer/dto/LabourResponseDTO.java
package com.MyFarmerApp.MyFarmer.dto;

import com.MyFarmerApp.MyFarmer.enums.LabourStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class LabourResponseDTO {

    private Long id;

    private Long userId;

    private String labourName;
    private String mobile;

    private String photoUrl;

    private String wageType; // DAILY | MONTHLY
    private Double dailyWage;
    private Double monthlySalary;

    private String role;

    private LocalDate joiningDate;

    private LabourStatus status;

    private String address;
    private String notes;
    private Boolean useAttendance;
    private String referralBy;

    private LocalDate createdAt;
    private LocalDate updatedAt;
}
