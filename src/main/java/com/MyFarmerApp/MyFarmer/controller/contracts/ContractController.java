package com.MyFarmerApp.MyFarmer.controller.contracts;

import com.MyFarmerApp.MyFarmer.dto.LoanSummaryResponse;
import com.MyFarmerApp.MyFarmer.dto.contracts.*;
import com.MyFarmerApp.MyFarmer.entity.contracts.LabourLoanAccount;
import com.MyFarmerApp.MyFarmer.service.contracts.ContractService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/contract")
@CrossOrigin(origins = "*")
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    // ---------------------------------------------------------
    // 1) CREATE CONTRACT
    // ---------------------------------------------------------
    @PostMapping("/create")
    public ResponseEntity<?> createContract(@RequestBody CreateContractRequest req) {
        return ResponseEntity.ok(contractService.createContract(req));
    }

    @GetMapping("/history/{labourId}")
    public ResponseEntity<?> history(@PathVariable Long labourId) {
        return ResponseEntity.ok(contractService.getContractHistory(labourId));
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<?> close(@PathVariable Long id) {
        return ResponseEntity.ok(contractService.closeContract(id));
    }

    // ---------------------------------------------------------
    // 2) PENALTY
    // ---------------------------------------------------------
    @PostMapping("/penalty/create")
    public ResponseEntity<?> createPenalty(@RequestBody CreatePenaltyRequest req) {
        return ResponseEntity.ok(contractService.createPenalty(req));
    }

    @GetMapping("/penalty/{labourId}")
    public ResponseEntity<?> getPenalties(@PathVariable Long labourId) {
        return ResponseEntity.ok(contractService.getPenalties(labourId));
    }

    @PostMapping("/penalty/{penaltyId}/mark-paid")
    public ResponseEntity<?> markPenaltyPaid(
            @PathVariable Long penaltyId,
            @RequestBody MarkPenaltyPaidRequest req
    ) {
        return ResponseEntity.ok(contractService.markPenaltyPaid(penaltyId, req));
    }

    // ---------------------------------------------------------
    // 3) LOAN : DISBURSEMENT (BY LABOUR ID)
    // ---------------------------------------------------------
    @PostMapping("/loan/disburse/{labourId}")
    public ResponseEntity<?> disburseLoan(
            @PathVariable Long labourId,
            @RequestBody LoanDisbursementRequest req
    ) {
        return ResponseEntity.ok(contractService.recordLoanDisbursementByLabour(labourId, req));
    }

    // ---------------------------------------------------------
    // 4) LOAN : REPAYMENT (BY LABOUR ID)
    // ---------------------------------------------------------
    @PostMapping("/loan/repay/{labourId}")
    public ResponseEntity<?> repayLoan(
            @PathVariable Long labourId,
            @RequestBody LoanRepaymentRequest req
    ) {
        return ResponseEntity.ok(contractService.recordLoanRepaymentByLabour(labourId, req));
    }

    // ---------------------------------------------------------
    // 5) GET LOAN ACCOUNT + TRANSACTIONS
    // ---------------------------------------------------------
    @GetMapping("/loan/account/{labourId}")
    public ResponseEntity<?> getLoanAccount(@PathVariable Long labourId) {
        return ResponseEntity.ok(contractService.getOrCreateLoanAccount(labourId));
    }

    @GetMapping("/loan/{accountId}/transactions")
    public ResponseEntity<?> loanTransactions(@PathVariable Long accountId) {
        return ResponseEntity.ok(contractService.getLoanTransactions(accountId));
    }

    @GetMapping("/loan/summary/{labourId}")
    public ResponseEntity<?> loanSummary(@PathVariable Long labourId) {

        LabourLoanAccount account = contractService.getOrCreateLoanAccount(labourId);
        List<com.MyFarmerApp.MyFarmer.entity.contracts.LabourLoanTransaction> txns =
                contractService.getLoanTransactions(account.getId());

        double totalDisbursed = 0.0;
        double totalRepaid = 0.0;
        double totalInterest = 0.0;
        double outstandingPrincipal = 0.0;

        // Step 1: sum up principal
        for (var t : txns) {
            if ("DISBURSEMENT".equals(t.getType())) {
                totalDisbursed += t.getAmount();
                outstandingPrincipal += t.getAmount();
            } else if ("REPAYMENT".equals(t.getType())) {
                totalRepaid += t.getAmount();
                outstandingPrincipal -= t.getAmount();
            }
        }
        if (outstandingPrincipal < 0) outstandingPrincipal = 0.0;

        // Step 2: calculate simple interest per disbursement
        // interest = principal × monthlyRate × months_since_disbursement
        double monthlyRate = account.getMonthlyInterestRate() != null
                ? account.getMonthlyInterestRate()
                : 0.02; // default 2% per month

        java.time.LocalDate today = java.time.LocalDate.now();
        for (var t : txns) {
            if ("DISBURSEMENT".equals(t.getType())) {
                java.time.LocalDate disbDate = t.getTxnDate();
                int startYm = disbDate.getYear() * 12 + disbDate.getMonthValue();
                int endYm   = today.getYear()    * 12 + today.getMonthValue();
                int months  = Math.max(0, endYm - startYm);
                totalInterest += t.getAmount() * monthlyRate * months;
            }
        }

        double outstandingWithInterest = outstandingPrincipal + totalInterest;

        return ResponseEntity.ok(LoanSummaryResponse.builder()
                .accountId(account.getId())
                .totalDisbursed(totalDisbursed)
                .totalRepaid(totalRepaid)
                .totalInterest(Math.round(totalInterest * 100.0) / 100.0)
                .outstandingAmount(Math.round(outstandingWithInterest * 100.0) / 100.0)
                .build());
    }
}
