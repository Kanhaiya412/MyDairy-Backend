// src/main/java/com/MyFarmerApp/MyFarmer/repository/LabourRepository.java
package com.MyFarmerApp.MyFarmer.repository;

import com.MyFarmerApp.MyFarmer.entity.Labour;
import com.MyFarmerApp.MyFarmer.entity.User;
import com.MyFarmerApp.MyFarmer.enums.LabourStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LabourRepository extends JpaRepository<Labour, Long> {

    List<Labour> findByUserIdAndStatus(Long userId, LabourStatus status);

    List<Labour> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Labour> findByUserIdAndStatusNotOrderByCreatedAtDesc(Long userId, LabourStatus status);

    @org.springframework.data.jpa.repository.Query(nativeQuery = true, value = 
        "SELECT l.id as labourId, l.u_labourname as labourName, l.u_mobile as mobile, " +
        "l.u_photo_url as photoUrl, l.u_status as status, l.u_joiningdate as joiningDate, l.u_enddate as endDate, " +
        "l.u_wage_type as wageType, l.u_daily_wage as dailyWage, l.u_monthly_salary as monthlySalary, " +
        "l.u_yearly_salary as yearlySalary, l.u_allowed_leaves as allowedLeaves, " +
        
        "lc.id as activeContractId, lc.u_contract_type as activeContractType, " +
        "lc.u_contract_amount as activeContractAmount, lc.u_start_date as activeContractStartDate, " +
        "lc.u_end_date as activeContractEndDate, " +
        
        "(SELECT SUM(u_amount) FROM div_labour_loan_txn llt " +
        " JOIN div_labour_loan_account lla ON llt.loan_account_id = lla.id " +
        " WHERE lla.labour_id = l.id AND llt.u_type = 'DISBURSEMENT') as totalDisbursed, " +
        
        "(SELECT SUM(u_amount) FROM div_labour_loan_txn llt " +
        " JOIN div_labour_loan_account lla ON llt.loan_account_id = lla.id " +
        " WHERE lla.labour_id = l.id AND llt.u_type = 'REPAYMENT') as totalRepaid, " +
        
        "(SELECT SUM(u_amount_paid) FROM div_labour_salary WHERE labour_id = l.id) as totalSalaryPaid, " +
        
        "(SELECT SUM(u_amount) FROM div_labour_leave_penalty WHERE labour_id = l.id AND u_status = 'PAID') as totalPenaltyPaid, " +
        
        "(SELECT SUM(u_amount) FROM div_labour_leave_penalty WHERE labour_id = l.id AND u_status = 'UNPAID') as totalPenaltyUnpaid, " +
        
        "(SELECT COUNT(*) FROM div_labour_attendance WHERE labour_id = l.id AND u_status = 'PRESENT') as totalPresentDays, " +
        
        "(SELECT COUNT(*) FROM div_labour_attendance WHERE labour_id = l.id AND u_status = 'ABSENT') as totalAbsentDays, " +
        
        "(SELECT COUNT(*) FROM div_labour_attendance WHERE labour_id = l.id AND u_status = 'HALF_DAY') as totalHalfDays " +
        
        "FROM div_labour l " +
        "LEFT JOIN div_labour_contract lc ON l.id = lc.labour_id AND lc.u_active = 1 " +
        "WHERE l.id = :labourId")
    com.MyFarmerApp.MyFarmer.repository.projection.LabourDashboardSummary getDashboardSummary(Long labourId);
}
