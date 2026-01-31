// src/main/java/com/MyFarmerApp/MyFarmer/mapper/LabourMapper.java
package com.MyFarmerApp.MyFarmer.mapper;

import com.MyFarmerApp.MyFarmer.dto.LabourResponseDTO;
import com.MyFarmerApp.MyFarmer.entity.Labour;

public class LabourMapper {

    private LabourMapper() {}

    public static LabourResponseDTO toDTO(Labour l) {
        if (l == null) return null;

        return LabourResponseDTO.builder()
                .id(l.getId())
                .userId(l.getUser() != null ? l.getUser().getId() : null)
                .labourName(l.getLabourName())
                .mobile(l.getMobile())
                .photoUrl(l.getPhotoUrl())
                .wageType(l.getWageType() != null ? l.getWageType().name() : null)
                .dailyWage(l.getDailyWage())
                .monthlySalary(l.getMonthlySalary())
                .role(l.getRole())
                .joiningDate(l.getJoiningDate())
                .status(l.getStatus())
                .address(l.getAddress())
                .notes(l.getNotes())
                .useAttendance(l.getUseAttendance())
                .referralBy(l.getReferralBy())
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .build();
    }
}
