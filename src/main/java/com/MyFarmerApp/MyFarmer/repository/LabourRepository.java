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

    List<Labour> findByUserAndStatus(User user, LabourStatus status);

    List<Labour> findByUserOrderByCreatedAtDesc(User user);
}
