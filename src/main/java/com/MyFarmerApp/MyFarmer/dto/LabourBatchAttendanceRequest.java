package com.MyFarmerApp.MyFarmer.dto;

import lombok.Data;
import java.util.List;

@Data
public class LabourBatchAttendanceRequest {
    private String date; // yyyy-MM-dd
    private List<LabourAttendanceEntry> entries;

    @Data
    public static class LabourAttendanceEntry {
        private Long labourId;
        private String status;
        private String remarks;
        private String shift;
        private Double workHours;
    }
}
