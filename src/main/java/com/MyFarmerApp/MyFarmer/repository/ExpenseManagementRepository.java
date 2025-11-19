package com.MyFarmerApp.MyFarmer.repository;

import com.MyFarmerApp.MyFarmer.entity.CattleEntry;
import com.MyFarmerApp.MyFarmer.entity.ExpenseManagement;
import com.MyFarmerApp.MyFarmer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ExpenseManagementRepository extends JpaRepository<ExpenseManagement, Long> {

    /**
     * 🔍 Find expenses by User (farmer)
     */
    List<ExpenseManagement> findByUser(User user);

    /**
     * 🔍 Find expenses related to a specific cattle
     */
    List<ExpenseManagement> findByCattleEntry(CattleEntry cattleEntry);

    /**
     * 🔍 Find expense by its unique expenseId
     */
    Optional<ExpenseManagement> findByExpenseId(String expenseId);

    /**
     * 🔍 Find expenses by item category (Feed, Medicine, etc.)
     */
    List<ExpenseManagement> findByItemCategoryIgnoreCase(String itemCategory);

    /**
     * 🔍 Search expenses by partial item name (case-insensitive)
     */
    @Query("SELECT e FROM ExpenseManagement e WHERE LOWER(e.itemName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<ExpenseManagement> searchByItemName(@Param("name") String name);

    /**
     * 💰 Get total expense (sum of totalCost) for a specific user
     */
    @Query("SELECT COALESCE(SUM(e.totalCost), 0) FROM ExpenseManagement e WHERE e.user = :user")
    Double getTotalExpenseByUser(@Param("user") User user);

    /**
     * 💰 Get total expense for a specific cattle
     */
    @Query("SELECT COALESCE(SUM(e.totalCost), 0) FROM ExpenseManagement e WHERE e.cattleEntry = :cattleEntry")
    Double getTotalExpenseByCattle(@Param("cattleEntry") CattleEntry cattleEntry);

    /**
     * 💰 Get total expense across all users (for dashboard summary)
     */
    @Query("SELECT COALESCE(SUM(e.totalCost), 0) FROM ExpenseManagement e")
    Double getTotalExpenseOverall();
}
