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
import com.MyFarmerApp.MyFarmer.enums.WageType;
import com.MyFarmerApp.MyFarmer.repository.LabourRepository;
import com.MyFarmerApp.MyFarmer.repository.LabourSalaryAdvanceRepository;
import com.MyFarmerApp.MyFarmer.repository.LabourSalaryRepository;
import com.MyFarmerApp.MyFarmer.repository.contracts.LabourContractRepository;
import com.MyFarmerApp.MyFarmer.repository.contracts.LabourLeavePenaltyRepository;
import com.MyFarmerApp.MyFarmer.repository.contracts.LabourLoanAccountRepository;
import com.MyFarmerApp.MyFarmer.repository.contracts.LabourLoanTransactionRepository;
import com.MyFarmerApp.MyFarmer.repository.projection.LabourDashboardSummary;
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
    private final com.MyFarmerApp.MyFarmer.repository.LabourAttendanceRepository attendanceRepo;

    public LabourDashboardDTO getDashboard(Long userId, Long labourId) {
        // 1. High Performance Summary Fetch
        LabourDashboardSummary summary = labourRepository.getDashboardSummary(labourId);
        
        if (summary == null) throw new RuntimeException("Labour not found");

        // 2. Ownership & Entity Proxy
        Labour labour = labourRepository.findById(labourId).get();
        if (!labour.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to this labour resource");
        }

        // 3. List fetching for timeline & interest
        List<LabourLoanAccount> loanAccounts = loanAccountRepo.findAllByLabourId(labourId);
        List<LabourLoanTransaction> allLoanTxns = new ArrayList<>();
        for (LabourLoanAccount acc : loanAccounts) {
            allLoanTxns.addAll(loanTxnRepo.findByLoanAccountIdOrderByTxnDateDesc(acc.getId()));
        }
        List<LabourSalary> salaries = salaryRepository.findByLabourOrderByYearDescMonthDesc(labour);
        List<LabourLeavePenalty> penalties = penaltyRepo.findByLabourOrderByDateDesc(labour);
        List<LabourSalaryAdvance> advances = advanceRepo.findByLabourIdOrderByDateDesc(labourId);

        // 4. Scalars from Summary
        double sumDisbursed = summary.getTotalDisbursed() != null ? summary.getTotalDisbursed() : 0.0;
        double sumRepaid = summary.getTotalRepaid() != null ? summary.getTotalRepaid() : 0.0;
        double sumSalPaid = summary.getTotalSalaryPaid() != null ? summary.getTotalSalaryPaid() : 0.0;
        double sumPenPaid = summary.getTotalPenaltyPaid() != null ? summary.getTotalPenaltyPaid() : 0.0;
        double sumPenUnpaid = summary.getTotalPenaltyUnpaid() != null ? summary.getTotalPenaltyUnpaid() : 0.0;
        double countPresent = summary.getTotalPresentDays() != null ? summary.getTotalPresentDays() : 0.0;
        double countHalf = summary.getTotalHalfDays() != null ? summary.getTotalHalfDays() : 0.0;
        double countAbsent = summary.getTotalAbsentDays() != null ? summary.getTotalAbsentDays() : 0.0;

        // Interest
        double totalInterest = 0.0;
        LocalDate today = LocalDate.now();
        double monthlyRate = (loanAccounts.isEmpty() || loanAccounts.get(0).getMonthlyInterestRate() == null) 
                             ? 0.02 : loanAccounts.get(0).getMonthlyInterestRate();

        for (LabourLoanTransaction t : allLoanTxns) {
            if ("DISBURSEMENT".equals(t.getType())) {
                int months = Math.max(0, (today.getYear() * 12 + today.getMonthValue()) - (t.getTxnDate().getYear() * 12 + t.getTxnDate().getMonthValue()));
                totalInterest += t.getAmount() * monthlyRate * months;
            }
        }
        double outstandingPrincipal = Math.max(0, sumDisbursed - sumRepaid);

        // Working Days calculation
        int totalWorkingDays = 0;
        if (labour.getWageType() == WageType.MONTHLY || labour.getWageType() == WageType.YEARLY) {
            LocalDate jd = summary.getJoiningDate() != null ? LocalDate.parse(summary.getJoiningDate()) : labour.getCreatedAt();
            LocalDate ed = summary.getEndDate() != null ? LocalDate.parse(summary.getEndDate()) : LocalDate.now();
            long totalCalDays = java.time.temporal.ChronoUnit.DAYS.between(jd, ed) + 1;
            totalWorkingDays = (int) (totalCalDays - countAbsent - (countHalf * 0.5));
        } else {
            totalWorkingDays = (int) (countPresent + (countHalf * 0.5));
        }

        // Timeline construction
        List<LabourEventDTO> timeline = new ArrayList<>();
        if (summary.getActiveContractId() != null) {
            timeline.add(LabourEventDTO.builder()
                    .date(LocalDate.parse(summary.getActiveContractStartDate()))
                    .type("CONTRACT_CREATED")
                    .amount(summary.getActiveContractAmount())
                    .description("Active " + summary.getActiveContractType() + " contract started")
                    .build());
        }

        for (LabourLoanTransaction t : allLoanTxns) {
            timeline.add(LabourEventDTO.builder()
                    .date(t.getTxnDate())
                    .type("LOAN_" + t.getType())
                    .amount(t.getAmount())
                    .description("DISBURSEMENT".equals(t.getType()) ? "Loan / advance given" : "Loan repayment")
                    .build());
        }

        for (LabourSalary s : salaries) {
            if (s.getAmountPaid() != null && s.getAmountPaid() > 0) {
                timeline.add(LabourEventDTO.builder()
                        .date(s.getPaidDate() != null ? s.getPaidDate() : LocalDate.now())
                        .type("SALARY_" + s.getPaymentStatus().name())
                        .amount(s.getAmountPaid())
                        .description("Salary paid for " + YearMonth.of(s.getYear(), s.getMonth()))
                        .build());
            }
        }

        for (LabourLeavePenalty p : penalties) {
            timeline.add(LabourEventDTO.builder()
                    .date(p.getDate())
                    .type("PENALTY_" + p.getStatus())
                    .amount(p.getPenaltyAmount())
                    .description("Penalty: " + (p.getReason() != null ? p.getReason() : ""))
                    .build());
        }

        for (LabourSalaryAdvance a : advances) {
            timeline.add(LabourEventDTO.builder()
                    .date(a.getDate())
                    .type("ADVANCE_" + a.getStatus())
                    .amount(a.getAmount())
                    .description("Salary advance " + ("PENDING".equalsIgnoreCase(a.getStatus()) ? "taken" : "settled"))
                    .build());
        }

        timeline.sort(Comparator.comparing(LabourEventDTO::getDate).reversed().thenComparing(LabourEventDTO::getType));

        // Accrued Wages logic
        double baseRate = (labour.getWageType() == WageType.YEARLY) ? (labour.getYearlySalary() / 365.0) : 
                          (labour.getDailyWage() != null ? labour.getDailyWage() : (labour.getMonthlySalary() / 30.0));
        double accrued = totalWorkingDays * baseRate;
        double pending = Math.max(0, accrued - sumSalPaid);

        // Yearly Penalty Sync
        if (labour.getWageType() == WageType.YEARLY && labour.getAllowedLeaves() != null) {
            int extra = (int) (countAbsent - labour.getAllowedLeaves());
            if (extra > 0) sumPenUnpaid += (extra * baseRate);
        }

        return LabourDashboardDTO.builder()
                .labourId(summary.getLabourId())
                .labourName(summary.getLabourName())
                .mobile(summary.getMobile())
                .photoUrl(summary.getPhotoUrl())
                .status(summary.getStatus())
                .joiningDate(summary.getJoiningDate() != null ? LocalDate.parse(summary.getJoiningDate()) : null)
                .endDate(summary.getEndDate() != null ? LocalDate.parse(summary.getEndDate()) : null)
                .totalWorkingDays(totalWorkingDays)
                .wageType(summary.getWageType())
                .dailyWage(summary.getDailyWage())
                .monthlySalary(summary.getMonthlySalary())
                .yearlySalary(summary.getYearlySalary())
                .allowedLeaves(summary.getAllowedLeaves())
                .activeContractId(summary.getActiveContractId())
                .contractType(summary.getActiveContractType())
                .contractAmount(summary.getActiveContractAmount())
                .contractStartDate(summary.getActiveContractStartDate() != null ? LocalDate.parse(summary.getActiveContractStartDate()) : null)
                .contractEndDate(summary.getActiveContractEndDate() != null ? LocalDate.parse(summary.getActiveContractEndDate()) : null)
                .totalDisbursed(sumDisbursed)
                .totalRepaid(sumRepaid)
                .outstandingPrincipal(outstandingPrincipal)
                .totalInterest(totalInterest)
                .outstandingWithInterest(outstandingPrincipal + totalInterest)
                .totalSalaryPaid(sumSalPaid)
                .totalAccruedSalary(accrued)
                .pendingSalary(pending)
                .totalPenaltyUnpaid(sumPenUnpaid)
                .totalPenaltyPaid(sumPenPaid)
                .timeline(timeline)
                .build();
    }
}
