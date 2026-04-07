package com.MyFarmerApp.MyFarmer.event;

import lombok.Builder;
import lombok.Getter;

/**
 * Domain Event representing changes in Labour, Attendance or Salaries.
 * Published to decouple the main request thread from Kafka dispatches.
 */
@Getter
@Builder
public class LabourEvent {
    private final String eventType;
    private final String username;
    private final String role;
    private final boolean success;
    private final String message;
    private final Long labourId;
    private final String labourName;
    private final Double dailyWage;
    private final String mobile;
    private final String date;
    private final Integer presentDays;
    private final Integer manualDays;
    private final Double amount;
    private final Integer month;
    private final Integer year;
    private final String topic;
}
