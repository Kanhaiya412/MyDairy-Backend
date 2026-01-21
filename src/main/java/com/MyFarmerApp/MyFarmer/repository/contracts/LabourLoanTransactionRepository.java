package com.MyFarmerApp.MyFarmer.repository.contracts;

import com.MyFarmerApp.MyFarmer.entity.contracts.LabourLoanTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabourLoanTransactionRepository extends JpaRepository<LabourLoanTransaction, Long> {
    List<LabourLoanTransaction> findByLoanAccountIdOrderByTxnDateDesc(Long loanAccountId);
}
