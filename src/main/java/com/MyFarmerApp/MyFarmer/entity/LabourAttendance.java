package com.MyFarmerApp.MyFarmer.entity;

import com.MyFarmerApp.MyFarmer.enums.LabourAttendanceStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "DIV_LABOUR_ATTENDANCE",
        uniqueConstraints = @UniqueConstraint(columnNames = {"labour_id", "U_DATE"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabourAttendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "labour_id", nullable = false)
    @JsonIgnore
    private Labour labour;

    @Column(name = "U_DATE", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "U_STATUS", nullable = false, length = 10)
    private LabourAttendanceStatus status;

    @Column(name = "U_REMARKS", length = 255)
    private String remarks;

    @Column(name = "U_CREATEDAT")
    private LocalDate createdAt = LocalDate.now();

    @Column(name = "U_UPDATEDAT")
    private LocalDate updatedAt = LocalDate.now();
}
