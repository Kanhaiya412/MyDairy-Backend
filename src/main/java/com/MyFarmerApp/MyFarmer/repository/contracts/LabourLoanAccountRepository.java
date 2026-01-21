package com.MyFarmerApp.MyFarmer.repository.contracts;

import com.MyFarmerApp.MyFarmer.entity.contracts.LabourContract;
import com.MyFarmerApp.MyFarmer.entity.contracts.LabourLoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabourLoanAccountRepository extends JpaRepository<LabourLoanAccount, Long> {
    LabourLoanAccount findByContractId(Long contractId);

    List<LabourLoanAccount> findByContractIn(List<LabourContract> contracts);
}
