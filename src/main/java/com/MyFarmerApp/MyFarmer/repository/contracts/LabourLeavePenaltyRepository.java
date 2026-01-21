package com.MyFarmerApp.MyFarmer.repository.contracts;

import com.MyFarmerApp.MyFarmer.entity.Labour;
import com.MyFarmerApp.MyFarmer.entity.contracts.LabourLeavePenalty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LabourLeavePenaltyRepository extends JpaRepository<LabourLeavePenalty, Long> {

    List<LabourLeavePenalty> findByLabourOrderByDateDesc(Labour labour);

    List<LabourLeavePenalty> findByLabourAndDateBetween(
            Labour labour,
            LocalDate start,
            LocalDate end
    );
}
