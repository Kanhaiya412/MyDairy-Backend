package com.MyFarmerApp.MyFarmer.repository.contracts;

import com.MyFarmerApp.MyFarmer.entity.contracts.LabourContract;
import com.MyFarmerApp.MyFarmer.entity.contracts.LabourLoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabourLoanAccountRepository extends JpaRepository<LabourLoanAccount, Long> {
    LabourLoanAccount findByContractId(Long contractId);
    LabourLoanAccount findByLabourId(Long labourId);

    // ✅ Returns ALL loan accounts for a labour (used in dashboard to include non-contract udhar)
    List<LabourLoanAccount> findAllByLabourId(Long labourId);

    List<LabourLoanAccount> findByContractIn(List<LabourContract> contracts);
}
