package com.MyFarmerApp.MyFarmer.dto;

import com.MyFarmerApp.MyFarmer.enums.AnimalHealthStatus;
import com.MyFarmerApp.MyFarmer.enums.AnimalStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AnimalManagementResponse {

    private Long id;

    private Long cattleId;
    private String cattleName;
    private String cattleCode;

    private String animalColor;

    private LocalDate birthDate;
    private Integer age;

    private AnimalHealthStatus healthStatus;

    private LocalDate lastCheckupDate;
    private LocalDate nextCheckupDate;

    private LocalDate lastVaccinationDate;
    private LocalDate nextVaccinationDate;

    private LocalDate lastHeatDate;
    private LocalDate lastBullMeetDate;
    private LocalDate lastAIDate;

    private Double avgMilkProduction;

    private String remarks;
    private AnimalStatus status;

    private LocalDate createdAt;
    private LocalDate updatedAt;
}
