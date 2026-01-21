package com.MyFarmerApp.MyFarmer.entity.contracts;

import com.MyFarmerApp.MyFarmer.entity.Labour;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "DIV_LABOUR_CONTRACT")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabourContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "labour_id", nullable = false)
    @JsonIgnore
    private Labour labour;

    @Column(name = "U_CONTRACT_TYPE", nullable = false, length = 20)
    private String contractType;   // YEARLY | MONTHLY | DAILY

    @Column(name = "U_CONTRACT_AMOUNT", nullable = false)
    private Double contractAmount;

    @Column(name = "U_START_DATE", nullable = false)
    private LocalDate startDate;

    @Column(name = "U_END_DATE", nullable = false)
    private LocalDate endDate;

    // 🔹 All these need @Builder.Default so Lombok keeps the defaults
    @Builder.Default
    @Column(name = "U_ADVANCE_PAID", nullable = false)
    private Boolean advancePaid = true; // yearly always advance

    @Builder.Default
    @Column(name = "U_ALLOWED_LEAVES")
    private Integer allowedLeaves = 21;

    @Builder.Default
    @Column(name = "U_MONTHLY_INTEREST_RATE")
    private Double monthlyInterestRate = 0.02; // default 2%

    @Builder.Default
    @Column(name = "U_ACTIVE")
    private Boolean active = true;

    @Builder.Default
    @Column(name = "U_CREATEDAT")
    private LocalDate createdAt = LocalDate.now();
}
