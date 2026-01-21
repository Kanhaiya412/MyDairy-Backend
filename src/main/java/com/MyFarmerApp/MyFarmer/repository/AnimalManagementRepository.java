package com.MyFarmerApp.MyFarmer.repository;

import com.MyFarmerApp.MyFarmer.entity.AnimalManagement;
import com.MyFarmerApp.MyFarmer.entity.CattleEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnimalManagementRepository extends JpaRepository<AnimalManagement, Long> {

    List<AnimalManagement> findByCattleEntryOrderByCreatedAtDesc(CattleEntry cattleEntry);

    AnimalManagement findFirstByCattleEntryOrderByCreatedAtDesc(CattleEntry cattleEntry);
}
