package com.MyFarmerApp.MyFarmer.repository;

import com.MyFarmerApp.MyFarmer.entity.CattleEntry;
import com.MyFarmerApp.MyFarmer.entity.User;
import com.MyFarmerApp.MyFarmer.enums.CattleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CattleEntryRepository extends JpaRepository<CattleEntry, Long> {

    List<CattleEntry> findByUser(User user);

    List<CattleEntry> findByUserAndStatus(User user, CattleStatus status);

    // ✅ Scoped lookups — unique cattle per user
    Optional<CattleEntry> findByCattleIdAndUser(String cattleId, User user);

    boolean existsByCattleIdAndUser(String cattleId, User user);
}
