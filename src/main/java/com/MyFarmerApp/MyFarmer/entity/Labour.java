// src/main/java/com/MyFarmerApp/MyFarmer/entity/Labour.java
package com.MyFarmerApp.MyFarmer.entity;

import com.MyFarmerApp.MyFarmer.enums.LabourStatus;
import com.MyFarmerApp.MyFarmer.enums.WageType;
import com.MyFarmerApp.MyFarmer.entity.contracts.LabourContract;  // ✅ FIXED IMPORT
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "DIV_LABOUR")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Labour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "U_LABOURNAME", nullable = false, length = 100)
    private String labourName;

    @Column(name = "U_MOBILE", length = 15)
    private String mobile;

    @Enumerated(EnumType.STRING)
    @Column(name = "U_WAGE_TYPE", nullable = false)
    private WageType wageType;

    @Column(name = "U_DAILY_WAGE")
    private Double dailyWage;

    @Column(name = "U_MONTHLY_SALARY")
    private Double monthlySalary;

    @Column(name = "U_ADDRESS", length = 255)
    private String address;

    @Column(name = "U_NOTES", length = 255)
    private String notes;

    @Column(name = "U_USE_ATTENDANCE")
    private Boolean useAttendance = true;

    @Column(name = "U_REFERRAL_BY", length = 100)
    private String referralBy;

    @Column(name = "U_JOININGDATE")
    private LocalDate joiningDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "U_STATUS", nullable = false)
    private LabourStatus status = LabourStatus.ACTIVE;

    @Column(name = "U_CREATEDAT")
    private LocalDate createdAt = LocalDate.now();

    @Column(name = "U_UPDATEDAT")
    private LocalDate updatedAt = LocalDate.now();

    @Column(name = "U_ROLE", length = 100)
    private String role;

    @OneToMany(mappedBy = "labour", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<LabourContract> contracts = new ArrayList<>();
}
