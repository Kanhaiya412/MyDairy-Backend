// src/main/java/com/MyFarmerApp/MyFarmer/dto/LabourAttendanceRequest.java
package com.MyFarmerApp.MyFarmer.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabourAttendanceRequest {

    private Long labourId;
    private String date;    // "yyyy-MM-dd"
    private String status;  // "PRESENT" | "ABSENT"
    private String remarks;
}
