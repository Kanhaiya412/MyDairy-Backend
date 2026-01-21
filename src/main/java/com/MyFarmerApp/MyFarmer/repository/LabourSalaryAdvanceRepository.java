package com.MyFarmerApp.MyFarmer.repository;

import com.MyFarmerApp.MyFarmer.entity.LabourSalaryAdvance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LabourSalaryAdvanceRepository extends JpaRepository<LabourSalaryAdvance, Long> {

    List<LabourSalaryAdvance> findByLabourIdOrderByDateDesc(Long labourId);

    List<LabourSalaryAdvance> findByLabourIdAndDateBetweenAndStatus(
            Long labourId,
            LocalDate start,
            LocalDate end,
            String status
    );
}
