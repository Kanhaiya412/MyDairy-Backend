// src/main/java/com/MyFarmerApp/MyFarmer/repository/LabourSalaryAdvanceRepository.java
package com.MyFarmerApp.MyFarmer.repository;

import com.MyFarmerApp.MyFarmer.entity.LabourSalaryAdvance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LabourSalaryAdvanceRepository extends JpaRepository<LabourSalaryAdvance, Long> {

    List<LabourSalaryAdvance> findByLabourIdOrderByDateDesc(Long labourId);

    // ✅ FIX: month/year filter me status filter remove
    List<LabourSalaryAdvance> findByLabourIdAndDateBetweenOrderByDateDesc(
            Long labourId,
            LocalDate start,
            LocalDate end
    );

    // ✅ Optional: only pending (for salary/loan deduction screen if needed)
    List<LabourSalaryAdvance> findByLabourIdAndDateBetweenAndStatusOrderByDateDesc(
            Long labourId,
            LocalDate start,
            LocalDate end,
            String status
    );
}
