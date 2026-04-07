// src/main/java/com/MyFarmerApp/MyFarmer/service/contracts/ContractService.java
package com.MyFarmerApp.MyFarmer.service.contracts;

import com.MyFarmerApp.MyFarmer.dto.contracts.*;
import com.MyFarmerApp.MyFarmer.entity.Labour;
import com.MyFarmerApp.MyFarmer.entity.contracts.*;
import com.MyFarmerApp.MyFarmer.repository.LabourRepository;
import com.MyFarmerApp.MyFarmer.repository.contracts.*;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ContractService {

    private final LabourRepository labourRepository;
    private final LabourContractRepository contractRepo;
    private final LabourLeavePenaltyRepository penaltyRepo;
    private final LabourLoanAccountRepository loanAccountRepo;
    private final LabourLoanTransactionRepository loanTxnRepo;
    private final LabourLoanSummaryRepository summaryRepo;

    public ContractService(
            LabourRepository labourRepository,
            LabourContractRepository contractRepo,
            LabourLeavePenaltyRepository penaltyRepo,
            LabourLoanAccountRepository loanAccountRepo,
            LabourLoanTransactionRepository loanTxnRepo,
            LabourLoanSummaryRepository summaryRepo
    ) {
        this.labourRepository = labourRepository;
        this.contractRepo = contractRepo;
        this.penaltyRepo = penaltyRepo;
        this.loanAccountRepo = loanAccountRepo;
        this.loanTxnRepo = loanTxnRepo;
        this.summaryRepo = summaryRepo;
    }

    // ------------------------------------------------------------------
    // 1) CREATE CONTRACT
    // ------------------------------------------------------------------

    @Transactional
    public LabourContract createContract(CreateContractRequest req) {

        if (req.getLabourId() == null) throw new RuntimeException("labourId is required");
        if (req.getContractType() == null || req.getContractType().isBlank())
            throw new RuntimeException("contractType is required");
        if (req.getContractAmount() == null || req.getContractAmount() <= 0)
            throw new RuntimeException("contractAmount must be positive");
        if (req.getStartDate() == null || req.getStartDate().isBlank())
            throw new RuntimeException("startDate is required");
        if (req.getEndDate() == null || req.getEndDate().isBlank())
            throw new RuntimeException("endDate is required");

        Labour labour = labourRepository.findById(req.getLabourId())
                .orElseThrow(() -> new RuntimeException("Labour not found"));

        Integer allowedLeaves = (req.getAllowedLeaves() != null) ? req.getAllowedLeaves() : 21;
        Double monthlyInterestRate = (req.getMonthlyInterestRate() != null) ? req.getMonthlyInterestRate() : 0.02;

        LabourContract contract = LabourContract.builder()
                .labour(labour)
                .contractType(req.getContractType().toUpperCase())
                .contractAmount(req.getContractAmount())
                .startDate(LocalDate.parse(req.getStartDate()))
                .endDate(LocalDate.parse(req.getEndDate()))
                .allowedLeaves(allowedLeaves)
                .monthlyInterestRate(monthlyInterestRate)
                .build();

        LabourContract savedContract = contractRepo.save(contract);

        // ✅ Create Loan Account only if not exists
        LabourLoanAccount existing = loanAccountRepo.findByContractId(savedContract.getId());
        if (existing == null) {
            LabourLoanAccount account = LabourLoanAccount.builder()
                    .contract(savedContract)
                    .monthlyInterestRate(savedContract.getMonthlyInterestRate())
                    .outstanding(0.0)
                    .status("ACTIVE")
                    .build();

            LabourLoanAccount savedAccount = loanAccountRepo.save(account);

            // ✅ Create Loan Summary
            LabourLoanSummary summary = LabourLoanSummary.builder()
                    .loanAccount(savedAccount)
                    .totalDisbursed(0.0)
                    .totalRepaid(0.0)
                    .totalInterest(0.0)
                    .outstandingAmount(0.0)
                    .build();

            summaryRepo.save(summary);
        }

        return savedContract;
    }

    // ------------------------------------------------------------------
    // 2) PENALTY
    // ------------------------------------------------------------------

    public LabourLeavePenalty createPenalty(CreatePenaltyRequest req) {

        if (req.getLabourId() == null) throw new RuntimeException("labourId is required");
        if (req.getDate() == null || req.getDate().isBlank()) throw new RuntimeException("date is required");
        if (req.getExtraLeaves() == null || req.getExtraLeaves() <= 0)
            throw new RuntimeException("extraLeaves must be positive");
        if (req.getPenaltyAmount() == null || req.getPenaltyAmount() <= 0)
            throw new RuntimeException("penaltyAmount must be positive");

        Labour labour = labourRepository.findById(req.getLabourId())
                .orElseThrow(() -> new RuntimeException("Labour not found"));

        LabourLeavePenalty penalty = LabourLeavePenalty.builder()
                .labour(labour)
                .date(LocalDate.parse(req.getDate()))
                .extraLeaves(req.getExtraLeaves())
                .penaltyAmount(req.getPenaltyAmount())
                .reason(req.getReason())
                .status("UNPAID")
                .build();

        return penaltyRepo.save(penalty);
    }

    public List<LabourLeavePenalty> getPenalties(Long labourId) {
        Labour labour = labourRepository.findById(labourId)
                .orElseThrow(() -> new RuntimeException("Labour not found"));

        return penaltyRepo.findByLabourOrderByDateDesc(labour);
    }

    public List<LabourContract> getContractHistory(Long labourId) {
        Labour labour = labourRepository.findById(labourId)
                .orElseThrow(() -> new RuntimeException("Labour not found"));

        return contractRepo.findByLabourOrderByStartDateDesc(labour);
    }

    // ------------------------------------------------------------------
    // CLOSE CONTRACT
    // ------------------------------------------------------------------

    @Transactional
    public LabourContract closeContract(Long id) {
        LabourContract c = contractRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        c.setActive(false);
        c.setEndDate(LocalDate.now());
        return contractRepo.save(c);
    }

    // ------------------------------------------------------------------
    // MARK PENALTY PAID
    // ------------------------------------------------------------------

    @Transactional
    public LabourLeavePenalty markPenaltyPaid(Long penaltyId, MarkPenaltyPaidRequest req) {

        LabourLeavePenalty p = penaltyRepo.findById(penaltyId)
                .orElseThrow(() -> new RuntimeException("Penalty not found"));

        p.setStatus("PAID");
        p.setPaidDate(LocalDate.now());
        return penaltyRepo.save(p);
    }    // ------------------------------------------------------------------
    // LOAN MANAGEMENT (BY LABOUR ID)
    // ------------------------------------------------------------------

    @Transactional
    public LabourLoanAccount getOrCreateLoanAccount(Long labourId) {
        LabourLoanAccount account = loanAccountRepo.findByLabourId(labourId);
        if (account == null) {
            Labour labour = labourRepository.findById(labourId)
                    .orElseThrow(() -> new RuntimeException("Labour not found"));

            account = LabourLoanAccount.builder()
                    .labour(labour)
                    .monthlyInterestRate(0.02) // 2% Default simple interest
                    .outstanding(0.0)
                    .status("ACTIVE")
                    .createdAt(LocalDate.now())
                    .build();
            account = loanAccountRepo.save(account);

            LabourLoanSummary summary = LabourLoanSummary.builder()
                    .loanAccount(account)
                    .totalDisbursed(0.0)
                    .totalRepaid(0.0)
                    .totalInterest(0.0)
                    .outstandingAmount(0.0)
                    .build();
            summaryRepo.save(summary);
        }
        return account;
    }

    @Transactional
    public LabourLoanTransaction recordLoanDisbursementByLabour(Long labourId, LoanDisbursementRequest req) {
        LabourLoanAccount account = getOrCreateLoanAccount(labourId);
        double amount = req.getAmount();

        account.setOutstanding(account.getOutstanding() + amount);
        loanAccountRepo.save(account);

        LabourLoanSummary summary = summaryRepo.findByLoanAccountId(account.getId());
        summary.setTotalDisbursed(summary.getTotalDisbursed() + amount);
        summary.setOutstandingAmount(account.getOutstanding());
        summaryRepo.save(summary);

        return loanTxnRepo.save(LabourLoanTransaction.builder()
                .loanAccount(account)
                .txnDate(LocalDate.parse(req.getDate()))
                .type("DISBURSEMENT")
                .amount(amount)
                .reason(req.getReason())
                .notes(req.getNotes())
                .createdAt(LocalDate.now())
                .build());
    }

    @Transactional
    public LabourLoanTransaction recordLoanRepaymentByLabour(Long labourId, LoanRepaymentRequest req) {
        LabourLoanAccount account = getOrCreateLoanAccount(labourId);
        double amount = req.getAmount();

        account.setOutstanding(Math.max(0.0, account.getOutstanding() - amount));
        loanAccountRepo.save(account);

        LabourLoanSummary summary = summaryRepo.findByLoanAccountId(account.getId());
        summary.setTotalRepaid(summary.getTotalRepaid() + amount);
        summary.setOutstandingAmount(account.getOutstanding());
        summaryRepo.save(summary);

        return loanTxnRepo.save(LabourLoanTransaction.builder()
                .loanAccount(account)
                .txnDate(LocalDate.parse(req.getDate()))
                .type("REPAYMENT")
                .amount(amount)
                .notes(req.getNotes())
                .createdAt(LocalDate.now())
                .build());
    }

    public LabourLoanAccount getLoanAccountByLabour(Long labourId) {
        return loanAccountRepo.findByLabourId(labourId);
    }

    public List<LabourLoanTransaction> getLoanTransactions(Long accountId) {
        return loanTxnRepo.findByLoanAccountIdOrderByTxnDateDesc(accountId);
    }

    public LabourLoanSummary getLoanSummary(Long accountId) {
        return summaryRepo.findByLoanAccountId(accountId);
    }
}
