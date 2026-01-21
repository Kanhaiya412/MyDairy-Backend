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

        Labour labour = labourRepository.findById(req.getLabourId())
                .orElseThrow(() -> new RuntimeException("Labour not found"));

        Integer allowedLeaves = (req.getAllowedLeaves() != null)
                ? req.getAllowedLeaves()
                : 21;

        Double monthlyInterestRate = (req.getMonthlyInterestRate() != null)
                ? req.getMonthlyInterestRate()
                : 0.02;

        LabourContract contract = LabourContract.builder()
                .labour(labour)
                .contractType(req.getContractType())
                .contractAmount(req.getContractAmount())
                .startDate(LocalDate.parse(req.getStartDate()))
                .endDate(LocalDate.parse(req.getEndDate()))
                .allowedLeaves(allowedLeaves)
                .monthlyInterestRate(monthlyInterestRate)
                // advancePaid, active, createdAt get defaults from @Builder.Default
                .build();

        LabourContract savedContract = contractRepo.save(contract);

        // Create Loan Account
        LabourLoanAccount account = LabourLoanAccount.builder()
                .contract(savedContract)
                .monthlyInterestRate(savedContract.getMonthlyInterestRate())
                .outstanding(0.0)
                .status("ACTIVE")
                .build();

        loanAccountRepo.save(account);

        // Create Loan Summary
        LabourLoanSummary summary = LabourLoanSummary.builder()
                .loanAccount(account)
                .totalDisbursed(0.0)
                .totalRepaid(0.0)
                .totalInterest(0.0)
                .outstandingAmount(0.0)
                .build();

        summaryRepo.save(summary);

        return savedContract;
    }

    // ------------------------------------------------------------------
    // 2) PENALTY
    // ------------------------------------------------------------------

    public LabourLeavePenalty createPenalty(CreatePenaltyRequest req) {

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
    }

    // ------------------------------------------------------------------
    // 3) LOAN DISBURSEMENT
    // ------------------------------------------------------------------

    @Transactional
    public LabourLoanTransaction recordLoanDisbursement(Long contractId, LoanDisbursementRequest req) {

        LabourLoanAccount account = loanAccountRepo.findByContractId(contractId);
        if (account == null) {
            throw new RuntimeException("Loan account not found for contract");
        }

        double amount = req.getAmount();

        // 1) Update outstanding
        account.setOutstanding(account.getOutstanding() + amount);
        loanAccountRepo.save(account);

        // 2) Fix NULL summary
        LabourLoanSummary summary = summaryRepo.findByLoanAccountId(account.getId());
        if (summary == null) {
            summary = LabourLoanSummary.builder()
                    .loanAccount(account)
                    .totalDisbursed(0.0)
                    .totalRepaid(0.0)
                    .totalInterest(0.0)
                    .outstandingAmount(account.getOutstanding())
                    .build();
        }

        summary.setTotalDisbursed(summary.getTotalDisbursed() + amount);
        summary.setOutstandingAmount(account.getOutstanding());
        summaryRepo.save(summary);

        // 3) Create transaction entry
        LabourLoanTransaction txn = LabourLoanTransaction.builder()
                .loanAccount(account)
                .txnDate(LocalDate.parse(req.getDate()))
                .type("DISBURSEMENT")
                .amount(amount)
                .reason(req.getReason())
                .notes(req.getNotes())
                .build();

        return loanTxnRepo.save(txn);
    }

    // ------------------------------------------------------------------
    // 4) LOAN REPAYMENT
    // ------------------------------------------------------------------

    @Transactional
    public LabourLoanTransaction recordLoanRepayment(Long contractId, LoanRepaymentRequest req) {

        LabourLoanAccount account = loanAccountRepo.findByContractId(contractId);

        double amount = req.getAmount();

        account.setOutstanding(account.getOutstanding() - amount);
        if (account.getOutstanding() <= 0) {
            account.setStatus("CLOSED");
        }
        loanAccountRepo.save(account);

        LabourLoanSummary summary = summaryRepo.findByLoanAccountId(account.getId());
        summary.setTotalRepaid(summary.getTotalRepaid() + amount);
        summary.setOutstandingAmount(account.getOutstanding());
        summaryRepo.save(summary);

        LabourLoanTransaction txn = LabourLoanTransaction.builder()
                .loanAccount(account)
                .txnDate(LocalDate.parse(req.getDate()))
                .type("REPAYMENT")
                .amount(amount)
                .notes(req.getNotes())
                .build();

        return loanTxnRepo.save(txn);
    }

    // ------------------------------------------------------------------
    // 5) GET LOAN ACCOUNT + TXNS
    // ------------------------------------------------------------------

    public LabourLoanAccount getLoanAccount(Long contractId) {
        return loanAccountRepo.findByContractId(contractId);
    }

    public List<LabourLoanTransaction> getLoanTransactions(Long accountId) {
        return loanTxnRepo.findByLoanAccountIdOrderByTxnDateDesc(accountId);
    }

    public LabourLoanSummary getLoanSummary(Long accountId) {
        return summaryRepo.findByLoanAccountId(accountId);
    }

}
