// src/main/java/com/MyFarmerApp/MyFarmer/repository/LabourAttendanceRepository.java
package com.MyFarmerApp.MyFarmer.repository;

import com.MyFarmerApp.MyFarmer.entity.Labour;
import com.MyFarmerApp.MyFarmer.entity.LabourAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LabourAttendanceRepository extends JpaRepository<LabourAttendance, Long> {

    Optional<LabourAttendance> findByLabourAndDate(Labour labour, LocalDate date);

    List<LabourAttendance> findByLabourAndDateBetween(
            Labour labour,
            LocalDate start,
            LocalDate end
    );
}
