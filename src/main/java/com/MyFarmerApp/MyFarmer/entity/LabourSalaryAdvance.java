package com.MyFarmerApp.MyFarmer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "DIV_LABOUR_SALARY_ADVANCE")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabourSalaryAdvance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "labour_id", nullable = false)
    private Labour labour;

    @Column(name = "U_AMOUNT", nullable = false)
    private Double amount;

    @Column(name = "U_DATE", nullable = false)
    private LocalDate date;

    @Column(name = "U_REMARKS")
    private String remarks;

    @Column(name = "U_STATUS", nullable = false)
    private String status = "PENDING";  // PENDING | SETTLED

    @Column(name = "U_SETTLED_DATE")
    private LocalDate settledDate;

    @Column(name = "U_CREATEDAT")
    private LocalDate createdAt = LocalDate.now();
}
