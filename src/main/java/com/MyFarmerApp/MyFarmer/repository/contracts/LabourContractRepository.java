package com.MyFarmerApp.MyFarmer.repository.contracts;

import com.MyFarmerApp.MyFarmer.entity.Labour;
import com.MyFarmerApp.MyFarmer.entity.contracts.LabourContract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LabourContractRepository extends JpaRepository<LabourContract, Long> {
    List<LabourContract> findByLabourAndActive(Labour labour, Boolean active);

    List<LabourContract> findByLabourOrderByStartDateDesc(Labour labour);
}
