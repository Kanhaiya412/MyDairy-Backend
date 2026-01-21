package com.MyFarmerApp.MyFarmer.entity.contracts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "DIV_LABOUR_LOAN_SUMMARY")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LabourLoanSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_account_id")
    @JsonIgnore
    private LabourLoanAccount loanAccount;

    private Double totalDisbursed = 0.0;
    private Double totalRepaid = 0.0;
    private Double totalInterest = 0.0;
    private Double outstandingAmount = 0.0;
}
