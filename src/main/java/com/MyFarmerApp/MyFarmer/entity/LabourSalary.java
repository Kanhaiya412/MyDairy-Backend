package com.MyFarmerApp.MyFarmer.entity;

import com.MyFarmerApp.MyFarmer.enums.LabourPaymentStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "DIV_LABOUR_SALARY",
        uniqueConstraints = @UniqueConstraint(columnNames = {"labour_id", "U_MONTH", "U_YEAR"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabourSalary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "labour_id", nullable = false)
    @JsonIgnore
    private Labour labour;

    @Column(name = "U_MONTH", nullable = false)
    private Integer month; // 1-12

    @Column(name = "U_YEAR", nullable = false)
    private Integer year;

    @Column(name = "U_PRESENT_DAYS")
    private Integer presentDays;

    @Column(name = "U_MANUAL_DAYS")
    private Integer manualDays;

    @Column(name = "U_TOTAL_SALARY")
    private Double totalSalary;

    @Enumerated(EnumType.STRING)
    @Column(name = "U_PAYMENT_STATUS", nullable = false, length = 10)
    private LabourPaymentStatus paymentStatus = LabourPaymentStatus.UNPAID;

    @Column(name = "U_GENERATED_DATE")
    private LocalDate generatedDate;

    @Column(name = "U_PAID_DATE")
    private LocalDate paidDate;

    @Column(name = "U_NOTES", length = 255)
    private String notes;

    @Column(name = "U_CREATEDAT")
    private LocalDate createdAt = LocalDate.now();

    @Column(name = "U_UPDATEDAT")
    private LocalDate updatedAt = LocalDate.now();
}
