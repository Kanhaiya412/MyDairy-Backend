// src/main/java/com/MyFarmerApp/MyFarmer/service/LabourDashboardService.java
package com.MyFarmerApp.MyFarmer.service;

import com.MyFarmerApp.MyFarmer.dto.LabourDashboardDTO;
import com.MyFarmerApp.MyFarmer.dto.LabourEventDTO;
import com.MyFarmerApp.MyFarmer.entity.Labour;
import com.MyFarmerApp.MyFarmer.entity.LabourSalary;
import com.MyFarmerApp.MyFarmer.entity.LabourSalaryAdvance;
import com.MyFarmerApp.MyFarmer.entity.contracts.LabourContract;
import com.MyFarmerApp.MyFarmer.entity.contracts.LabourLeavePenalty;
import com.MyFarmerApp.MyFarmer.entity.contracts.LabourLoanAccount;
import com.MyFarmerApp.MyFarmer.entity.contracts.LabourLoanTransaction;
import com.MyFarmerApp.MyFarmer.enums.LabourPaymentStatus;
import com.MyFarmerApp.MyFarmer.repository.LabourRepository;
import com.MyFarmerApp.MyFarmer.repository.LabourSalaryAdvanceRepository;
import com.MyFarmerApp.MyFarmer.repository.LabourSalaryRepository;
import com.MyFarmerApp.MyFarmer.repository.contracts.LabourContractRepository;
import com.MyFarmerApp.MyFarmer.repository.contracts.LabourLeavePenaltyRepository;
import com.MyFarmerApp.MyFarmer.repository.contracts.LabourLoanAccountRepository;
import com.MyFarmerApp.MyFarmer.repository.contracts.LabourLoanTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LabourDashboardService {

    private final LabourRepository labourRepository;
    private final LabourContractRepository contractRepo;
    private final LabourLoanAccountRepository loanAccountRepo;
    private final LabourLoanTransactionRepository loanTxnRepo;
    private final LabourSalaryRepository salaryRepository;
    private final LabourLeavePenaltyRepository penaltyRepo;
    private final LabourSalaryAdvanceRepository advanceRepo;

    public LabourDashboardDTO getDashboard(Long labourId) {

        Labour labour = labourRepository.findById(labourId)
                .orElseThrow(() -> new RuntimeException("Labour not found"));

        // ─────────────────────────────────────────
        // CONTRACTS
        // ─────────────────────────────────────────
        List<LabourContract> contracts = contractRepo.findByLabourOrderByStartDateDesc(labour);

        LabourContract activeContract = contracts.stream()
                .filter(LabourContract::getActive)
                .findFirst()
                .orElse(null);

        Long activeContractId = activeContract != null ? activeContract.getId() : null;
        String contractType = activeContract != null ? activeContract.getContractType() : null;
        Double contractAmount = activeContract != null ? activeContract.getContractAmount() : null;
        LocalDate contractStart = activeContract != null ? activeContract.getStartDate() : null;
        LocalDate contractEnd = activeContract != null ? activeContract.getEndDate() : null;

        // ─────────────────────────────────────────
        // LOAN + INTEREST (EXTRA BORROWED MONEY)
        // ─────────────────────────────────────────
        double totalDisbursed = 0.0;
        double totalRepaid = 0.0;
        double totalInterest = 0.0;
        double outstandingPrincipal = 0.0;

        List<LabourLoanAccount> loanAccounts = contracts.isEmpty()
                ? List.of()
                : loanAccountRepo.findByContractIn(contracts);

        List<LabourLoanTransaction> allLoanTxns = new ArrayList<>();
        for (LabourLoanAccount acc : loanAccounts) {
            List<LabourLoanTransaction> txns =
                    loanTxnRepo.findByLoanAccountIdOrderByTxnDateDesc(acc.getId());
            allLoanTxns.addAll(txns);
        }

        if (!allLoanTxns.isEmpty()) {
            // Principal summary (total disbursed / repaid / outstanding)
            for (LabourLoanTransaction t : allLoanTxns) {
                switch (t.getType()) {
                    case "DISBURSEMENT" -> {
                        totalDisbursed += t.getAmount();
                        outstandingPrincipal += t.getAmount();
                    }
                    case "REPAYMENT" -> {
                        totalRepaid += t.getAmount();
                        outstandingPrincipal -= t.getAmount();
                    }
                    case "INTEREST" -> {
                        // if you ever store interest as TXN, you can decide to handle it here
                    }
                    default -> {
                    }
                }
            }

            if (outstandingPrincipal < 0) {
                outstandingPrincipal = 0.0;
            }

            // Simple interest per disbursement:
            // For each DISBURSEMENT:
            // interest_i = principal_i * monthlyRate * months_since_disbursement
            LocalDate today = LocalDate.now();
            double monthlyRate = 0.02; // default 2%

            if (!loanAccounts.isEmpty() && loanAccounts.get(0).getMonthlyInterestRate() != null) {
                monthlyRate = loanAccounts.get(0).getMonthlyInterestRate();
            }

            totalInterest = 0.0;

            for (LabourLoanTransaction t : allLoanTxns) {
                if ("DISBURSEMENT".equals(t.getType())) {

                    double principal = t.getAmount();
                    LocalDate disbDate = t.getTxnDate();

                    int startYm = disbDate.getYear() * 12 + disbDate.getMonthValue();
                    int endYm = today.getYear() * 12 + today.getMonthValue();
                    int months = Math.max(0, endYm - startYm);

                    totalInterest += principal * monthlyRate * months;
                }
            }
        }

        double outstandingWithInterest = outstandingPrincipal + totalInterest;

        // ─────────────────────────────────────────
        // SALARY SUMMARY (total PAID)
        // ─────────────────────────────────────────
        List<LabourSalary> salaries = salaryRepository.findByLabourOrderByYearDescMonthDesc(labour);
        double totalSalaryPaid = salaries.stream()
                .filter(s -> s.getPaymentStatus() == LabourPaymentStatus.PAID)
                .mapToDouble(s -> s.getTotalSalary() != null ? s.getTotalSalary() : 0.0)
                .sum();

        // ─────────────────────────────────────────
        // PENALTY SUMMARY
        // ─────────────────────────────────────────
        List<LabourLeavePenalty> penalties = penaltyRepo.findByLabourOrderByDateDesc(labour);

        double totalPenaltyUnpaid = penalties.stream()
                .filter(p -> "UNPAID".equalsIgnoreCase(p.getStatus()))
                .mapToDouble(p -> p.getPenaltyAmount() != null ? p.getPenaltyAmount() : 0.0)
                .sum();

        double totalPenaltyPaid = penalties.stream()
                .filter(p -> "PAID".equalsIgnoreCase(p.getStatus()))
                .mapToDouble(p -> p.getPenaltyAmount() != null ? p.getPenaltyAmount() : 0.0)
                .sum();

        // ─────────────────────────────────────────
        // SALARY ADVANCES (optional in timeline)
        // ─────────────────────────────────────────
        List<LabourSalaryAdvance> advances =
                advanceRepo.findByLabourIdOrderByDateDesc(labourId);

        // ─────────────────────────────────────────
        // BUILD TIMELINE
        // ─────────────────────────────────────────
        List<LabourEventDTO> timeline = new ArrayList<>();

        // contract event — ONLY active contract (Q1 = A)
        if (activeContract != null) {
            timeline.add(LabourEventDTO.builder()
                    .date(activeContract.getStartDate())
                    .type("CONTRACT_CREATED")
                    .amount(activeContract.getContractAmount())
                    .description("Active " + activeContract.getContractType() + " contract started")
                    .build());
        }

        // loan txns
        for (LabourLoanTransaction t : allLoanTxns) {
            String desc;
            if ("DISBURSEMENT".equals(t.getType())) {
                desc = "Loan / advance given";
            } else if ("REPAYMENT".equals(t.getType())) {
                desc = "Loan repayment";
            } else {
                desc = "Loan txn: " + t.getType();
            }

            timeline.add(LabourEventDTO.builder()
                    .date(t.getTxnDate())
                    .type("LOAN_" + t.getType())       // LOAN_DISBURSEMENT / LOAN_REPAYMENT
                    .amount(t.getAmount())
                    .description(desc)
                    .build());
        }

        // salary paid events
        for (LabourSalary s : salaries) {
            if (s.getPaymentStatus() == LabourPaymentStatus.PAID && s.getPaidDate() != null) {
                YearMonth ym = YearMonth.of(s.getYear(), s.getMonth());
                timeline.add(LabourEventDTO.builder()
                        .date(s.getPaidDate())
                        .type("SALARY_PAID")
                        .amount(s.getTotalSalary())
                        .description("Salary paid for " + ym)
                        .build());
            }
        }

        // penalty events
        for (LabourLeavePenalty p : penalties) {
            String desc = "Penalty: " + (p.getReason() != null ? p.getReason() : "");
            timeline.add(LabourEventDTO.builder()
                    .date(p.getDate())
                    .type("PENALTY_" + p.getStatus())   // PENALTY_PAID / PENALTY_UNPAID
                    .amount(p.getPenaltyAmount())
                    .description(desc)
                    .build());
        }

        // advances in timeline (Q2 = A — keep visible)
        for (LabourSalaryAdvance a : advances) {
            String desc = "Salary advance " +
                    ("PENDING".equalsIgnoreCase(a.getStatus()) ? "taken" : "settled");
            timeline.add(LabourEventDTO.builder()
                    .date(a.getDate())
                    .type("ADVANCE_" + a.getStatus())   // ADVANCE_PENDING / ADVANCE_SETTLED
                    .amount(a.getAmount())
                    .description(desc)
                    .build());
        }

        // sort by date DESC, then by type for stable order
        timeline.sort(
                Comparator.comparing(LabourEventDTO::getDate)
                        .reversed()
                        .thenComparing(LabourEventDTO::getType)
        );

        // ─────────────────────────────────────────
        // BUILD FINAL DTO
        // ─────────────────────────────────────────
        return LabourDashboardDTO.builder()
                .labourId(labour.getId())
                .labourName(labour.getLabourName())
                .mobile(labour.getMobile())
                .wageType(labour.getWageType() != null ? labour.getWageType().name() : null)
                .dailyWage(labour.getDailyWage())
                .monthlySalary(labour.getMonthlySalary())
                .activeContractId(activeContractId)
                .contractType(contractType)
                .contractAmount(contractAmount)
                .contractStartDate(contractStart)
                .contractEndDate(contractEnd)
                .totalDisbursed(totalDisbursed)
                .totalRepaid(totalRepaid)
                .outstandingPrincipal(outstandingPrincipal)
                .totalInterest(totalInterest)
                .outstandingWithInterest(outstandingWithInterest)
                .totalSalaryPaid(totalSalaryPaid)
                .totalPenaltyUnpaid(totalPenaltyUnpaid)
                .totalPenaltyPaid(totalPenaltyPaid)
                .timeline(timeline)
                .build();
    }
}
