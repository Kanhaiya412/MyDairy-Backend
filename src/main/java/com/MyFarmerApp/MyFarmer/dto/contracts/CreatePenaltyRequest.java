package com.MyFarmerApp.MyFarmer.dto.contracts;

import lombok.Data;

@Data
public class CreatePenaltyRequest {
    private Long labourId;
    private String date; // yyyy-MM-dd
    private Integer extraLeaves;
    private Double penaltyAmount;
    private String reason;
}
