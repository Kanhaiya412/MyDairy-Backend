package com.MyFarmerApp.MyFarmer.repository;

import com.MyFarmerApp.MyFarmer.entity.CattleEntry;
import com.MyFarmerApp.MyFarmer.entity.MilkEntry;
import com.MyFarmerApp.MyFarmer.entity.User;
import com.MyFarmerApp.MyFarmer.enums.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MilkEntryRepository extends JpaRepository<MilkEntry, Long> {

    // ======== OLD ========
    List<MilkEntry> findByUser(User user);

    List<MilkEntry> findByUserOrderByDateAsc(User user);

    boolean existsByUserAndDateAndShift(User user, LocalDate date, Shift shift);

    @Query("SELECT m FROM MilkEntry m " +
            "WHERE m.user = :user AND MONTH(m.date) = :month AND YEAR(m.date) = :year " +
            "ORDER BY m.date ASC")
    List<MilkEntry> findByUserAndMonth(
            @Param("user") User user,
            @Param("month") int month,
            @Param("year") int year
    );

    List<MilkEntry> findByUserAndDateBetweenOrderByDateAsc(
            User user,
            LocalDate start,
            LocalDate end
    );

    // ======== NEW (CATTLE WISE) ========

    List<MilkEntry> findByCattleEntryOrderByDateAsc(CattleEntry cattleEntry);

    List<MilkEntry> findByCattleEntryAndDateGreaterThanEqualOrderByDateAsc(
            CattleEntry cattleEntry,
            LocalDate fromDate
    );

    List<MilkEntry> findByCattleEntryAndDateBetweenOrderByDateAsc(
            CattleEntry cattleEntry,
            LocalDate start,
            LocalDate end
    );

    // ✅ duplicate check for cattle wise entry
    boolean existsByUserAndCattleEntryAndDateAndShift(
            User user,
            CattleEntry cattleEntry,
            LocalDate date,
            Shift shift
    );

    // ✅ find existing entry for edit/update (general entry)
    Optional<MilkEntry> findByUserAndDateAndShift(User user, LocalDate date, Shift shift);

    // ✅ find existing entry for edit/update (cattle wise)
    Optional<MilkEntry> findByUserAndCattleEntryAndDateAndShift(
            User user,
            CattleEntry cattleEntry,
            LocalDate date,
            Shift shift
    );
}
