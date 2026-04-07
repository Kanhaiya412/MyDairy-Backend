// src/main/java/com/MyFarmerApp/MyFarmer/dto/LabourRequest.java
package com.MyFarmerApp.MyFarmer.dto;

import com.MyFarmerApp.MyFarmer.enums.LabourStatus;
import lombok.Data;

@Data
public class LabourRequest {

    private String labourName;
    private String mobile;

    // ✅ NEW: Photo URL/path
    private String photoUrl;

    // "DAILY" | "MONTHLY"
    private String wageType;

    private Double dailyWage;
    private Double monthlySalary;
    private Double yearlySalary;
    private Integer allowedLeaves;

    private String role;          // optional, default "LABOUR"

    // yyyy-MM-dd
    private String joiningDate;

    private LabourStatus status;

    private String address;
    private String notes;
    private Boolean useAttendance;
    private String referralBy;
}
