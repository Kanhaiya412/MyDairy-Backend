package com.MyFarmerApp.MyFarmer.entity.contracts;

import com.MyFarmerApp.MyFarmer.entity.Labour;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "DIV_LABOUR_LEAVE_PENALTY")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LabourLeavePenalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "labour_id", nullable = false)
    @JsonIgnore
    private Labour labour;
    @Column(name = "U_DATE", nullable = false)
    private LocalDate date;

    @Column(name = "U_EXTRA_LEAVES", nullable = false)
    private Integer extraLeaves;

    @Column(name = "U_AMOUNT", nullable = false)
    private Double penaltyAmount;

    @Column(name = "U_REASON", length = 255)
    private String reason;

    @Column(name = "U_STATUS", length = 10)
    private String status = "UNPAID"; // UNPAID | PAID

    @Column(name = "U_PAID_DATE")
    private LocalDate paidDate;

    @Column(name = "U_CREATEDAT")
    private LocalDate createdAt = LocalDate.now();
}
