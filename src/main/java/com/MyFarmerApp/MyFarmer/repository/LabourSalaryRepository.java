package com.MyFarmerApp.MyFarmer.repository;

import com.MyFarmerApp.MyFarmer.entity.Labour;
import com.MyFarmerApp.MyFarmer.entity.LabourSalary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LabourSalaryRepository extends JpaRepository<LabourSalary, Long> {

    Optional<LabourSalary> findByLabourAndMonthAndYear(Labour labour, int month, int year);

    List<LabourSalary> findByLabourOrderByYearDescMonthDesc(Labour labour);

}
