package com.MyFarmerApp.MyFarmer.repository;

import com.MyFarmerApp.MyFarmer.entity.MilkEntry;
import com.MyFarmerApp.MyFarmer.entity.User;
import com.MyFarmerApp.MyFarmer.enums.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface MilkEntryRepository extends JpaRepository<MilkEntry, Long> {

    // Existing
    List<MilkEntry> findByUser(User user);

    // Sort all entries ASC
    List<MilkEntry> findByUserOrderByDateAsc(User user);

    // Check duplicate
    boolean existsByUserAndDateAndShift(User user, LocalDate date, Shift shift);

    // Month filter
    @Query("SELECT m FROM MilkEntry m " +
            "WHERE m.user = :user AND MONTH(m.date) = :month AND YEAR(m.date) = :year " +
            "ORDER BY m.date ASC")
    List<MilkEntry> findByUserAndMonth(
            @Param("user") User user,
            @Param("month") int month,
            @Param("year") int year
    );

    // Date range filter (useful for sprint ranges)
    List<MilkEntry> findByUserAndDateBetweenOrderByDateAsc(
            User user,
            LocalDate start,
            LocalDate end
    );
}
