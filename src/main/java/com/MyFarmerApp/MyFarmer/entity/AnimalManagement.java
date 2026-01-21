package com.MyFarmerApp.MyFarmer.entity;

import com.MyFarmerApp.MyFarmer.enums.AnimalHealthStatus;
import com.MyFarmerApp.MyFarmer.enums.AnimalStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
@Entity
@Table(name = "DIV_ANIMALMANAGEMENT")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnimalManagement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cattle_id", nullable = false)
    @JsonIgnore
    private CattleEntry cattleEntry;

    @Column(name = "U_ANIMAL_COLOR")
    private String animalColor;

    @Column(name = "U_BIRTHDATE")
    private LocalDate birthDate;

    @Column(name = "U_AGE")
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "U_HEALTHSTATUS")
    private AnimalHealthStatus healthStatus;

    @Column(name = "U_LASTCHECKUPDATE")
    private LocalDate lastCheckupDate;

    @Column(name = "U_NEXTCHECKUPDATE")
    private LocalDate nextCheckupDate;

    @Column(name = "U_LASTVACCINATIONDATE")
    private LocalDate lastVaccinationDate;

    @Column(name = "U_NEXTVACCINATIONDATE")
    private LocalDate nextVaccinationDate;

    @Column(name = "U_LAST_HEAT_DATE")
    private LocalDate lastHeatDate;

    @Column(name = "U_LAST_BULL_MEET_DATE")
    private LocalDate lastBullMeetDate;

    @Column(name = "U_LAST_AI_DATE")
    private LocalDate lastAIDate;

    @Column(name = "U_AVGMILKPRODUCTION")
    private Double avgMilkProduction;

    @Column(name = "U_REMARKS")
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "U_STATUS", nullable = false)
    @Builder.Default
    private AnimalStatus status = AnimalStatus.ACTIVE;

    @Column(name = "U_CREATEDAT", updatable = false)
    private LocalDate createdAt;

    @Column(name = "U_UPDATEDAT")
    private LocalDate updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDate now = LocalDate.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = AnimalStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDate.now();
    }
}
