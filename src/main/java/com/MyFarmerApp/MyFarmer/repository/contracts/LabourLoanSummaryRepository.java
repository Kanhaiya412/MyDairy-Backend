package com.MyFarmerApp.MyFarmer.repository.contracts;

import com.MyFarmerApp.MyFarmer.entity.contracts.LabourLoanSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabourLoanSummaryRepository extends JpaRepository<LabourLoanSummary, Long> {
    LabourLoanSummary findByLoanAccountId(Long loanAccountId);
}
