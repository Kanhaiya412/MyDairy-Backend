package com.MyFarmerApp.MyFarmer.entity.contracts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "DIV_LABOUR_LOAN_TXN")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LabourLoanTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_account_id", nullable = false)
    @JsonIgnore
    private LabourLoanAccount loanAccount;

    @Column(name = "U_TXN_DATE", nullable = false)
    private LocalDate txnDate;

    @Column(name = "U_TYPE", nullable = false, length = 20)
    private String type; // DISBURSEMENT | REPAYMENT | INTEREST

    @Column(name = "U_AMOUNT", nullable = false)
    private Double amount;

    @Column(name = "U_REASON", length = 255)
    private String reason;

    @Column(name = "U_NOTES", length = 255)
    private String notes;

    @Column(name = "U_CREATEDAT")
    private LocalDate createdAt = LocalDate.now();
}
