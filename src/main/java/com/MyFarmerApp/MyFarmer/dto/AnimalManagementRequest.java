package com.MyFarmerApp.MyFarmer.dto;

import com.MyFarmerApp.MyFarmer.enums.AnimalHealthStatus;
import com.MyFarmerApp.MyFarmer.enums.AnimalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AnimalManagementRequest {

    @NotNull(message = "Cattle ID is required")
    private Long cattleId;

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

}
