package com.MyFarmerApp.MyFarmer.repository;

import com.MyFarmerApp.MyFarmer.entity.Labour;
import com.MyFarmerApp.MyFarmer.entity.LabourSalary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LabourSalaryRepository extends JpaRepository<LabourSalary, Long> {

    Optional<LabourSalary> findByLabourAndMonthAndYear(Labour labour, int month, int year);

    Optional<LabourSalary> findByLabourIdAndMonthAndYear(Long labourId, int month, int year);

    List<LabourSalary> findByLabourOrderByYearDescMonthDesc(Labour labour);

    List<LabourSalary> findByLabourOrderByYearAscMonthAsc(Labour labour);

    List<LabourSalary> findByLabourIdOrderByYearDescMonthDesc(Long labourId);

    List<LabourSalary> findByLabourIdOrderByYearAscMonthAsc(Long labourId);

}
