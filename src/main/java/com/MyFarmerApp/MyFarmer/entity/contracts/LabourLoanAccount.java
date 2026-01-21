package com.MyFarmerApp.MyFarmer.entity.contracts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "DIV_LABOUR_LOAN_ACCOUNT")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LabourLoanAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    @JsonIgnore
    private LabourContract contract;
    @Column(name = "U_MONTHLY_INTEREST_RATE")
    private Double monthlyInterestRate;

    @Column(name = "U_OUTSTANDING")
    private Double outstanding = 0.0;

    @Column(name = "U_STATUS")
    private String status = "ACTIVE"; // ACTIVE | CLOSED

    @Column(name = "U_CREATEDAT")
    private LocalDate createdAt = LocalDate.now();


    @OneToMany(mappedBy = "loanAccount")
    @JsonIgnore
    private List<LabourLoanTransaction> transactions;

    @OneToOne(mappedBy = "loanAccount")
    @JsonIgnore
    private LabourLoanSummary summary;

}
