
package com.MyFarmerApp.MyFarmer.controller.contracts;

import com.MyFarmerApp.MyFarmer.dto.LoanSummaryResponse;
import com.MyFarmerApp.MyFarmer.dto.contracts.*;
import com.MyFarmerApp.MyFarmer.entity.contracts.LabourLoanAccount;
import com.MyFarmerApp.MyFarmer.entity.contracts.LabourLoanSummary;
import com.MyFarmerApp.MyFarmer.service.contracts.ContractService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    // 3) LOAN : DISBURSEMENT
    // ---------------------------------------------------------
    @PostMapping("/{contractId}/loan/disburse")
    public ResponseEntity<?> disburseLoan(
            @PathVariable Long contractId,
            @RequestBody LoanDisbursementRequest req
    ) {
        return ResponseEntity.ok(contractService.recordLoanDisbursement(contractId, req));
    }

    // ---------------------------------------------------------
    // 4) LOAN : REPAYMENT
    // ---------------------------------------------------------
    @PostMapping("/{contractId}/loan/repay")
    public ResponseEntity<?> repayLoan(
            @PathVariable Long contractId,
            @RequestBody LoanRepaymentRequest req
    ) {
        return ResponseEntity.ok(contractService.recordLoanRepayment(contractId, req));
    }

    // ---------------------------------------------------------
    // 5) GET LOAN ACCOUNT + TRANSACTIONS
    // ---------------------------------------------------------
    @GetMapping("/{contractId}/loan")
    public ResponseEntity<?> getLoanAccount(@PathVariable Long contractId) {
        return ResponseEntity.ok(contractService.getLoanAccount(contractId));
    }

    @GetMapping("/loan/{accountId}/transactions")
    public ResponseEntity<?> loanTransactions(@PathVariable Long accountId) {
        return ResponseEntity.ok(contractService.getLoanTransactions(accountId));
    }

    // ✅ FIXED: summary NULL safe
    @GetMapping("/{contractId}/loan/summary")
    public ResponseEntity<?> loanSummary(@PathVariable Long contractId) {

        LabourLoanAccount account = contractService.getLoanAccount(contractId);
        if (account == null) {
            return ResponseEntity.ok(null);
        }

        LabourLoanSummary summary = contractService.getLoanSummary(account.getId());
        if (summary == null) {
            // ✅ return zero summary instead of crash
            LoanSummaryResponse resp = LoanSummaryResponse.builder()
                    .accountId(account.getId())
                    .totalDisbursed(0.0)
                    .totalRepaid(0.0)
                    .totalInterest(0.0)
                    .outstandingAmount(account.getOutstanding() != null ? account.getOutstanding() : 0.0)
                    .build();
            return ResponseEntity.ok(resp);
        }

        LoanSummaryResponse resp = LoanSummaryResponse.builder()
                .accountId(account.getId())
                .totalDisbursed(summary.getTotalDisbursed())
                .totalRepaid(summary.getTotalRepaid())
                .totalInterest(summary.getTotalInterest())
                .outstandingAmount(summary.getOutstandingAmount())
                .build();

        return ResponseEntity.ok(resp);
    }
}
