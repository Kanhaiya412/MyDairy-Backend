package com.MyFarmerApp.MyFarmer.service;

import com.MyFarmerApp.MyFarmer.entity.CattleEntry;
import com.MyFarmerApp.MyFarmer.entity.ExpenseManagement;
import com.MyFarmerApp.MyFarmer.entity.User;
import com.MyFarmerApp.MyFarmer.repository.ExpenseManagementRepository;
import com.MyFarmerApp.MyFarmer.util.EventPayload;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseManagementService {

    private final ExpenseManagementRepository expenseRepository;
    private final KafkaProducerService kafkaProducerService;

    public static final String EXPENSE_TOPIC = "expense-events";

    /**
     * ➕ Create a new expense entry
     */
    public ExpenseManagement createExpense(User user,
                                           CattleEntry cattleEntry,
                                           String itemId,
                                           String itemCategory,
                                           String itemName,
                                           String itemQuality,
                                           Double itemQuantity,
                                           Double itemPrice,
                                           String itemShopName,
                                           String itemBuyer,
                                           String shopOwner,
                                           LocalDate purchaseDate,
                                           String purchaseDay,
                                           String itemShop,
                                           String remarks) {

        String expenseId = "EXP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        double totalCost = (itemPrice != null && itemQuantity != null)
                ? itemPrice * itemQuantity
                : 0.0;

        ExpenseManagement expense = ExpenseManagement.builder()
                .user(user)
                .cattleEntry(cattleEntry)
                .expenseId(expenseId)
                .itemId(itemId)
                .itemCategory(itemCategory)
                .itemName(itemName)
                .itemQuality(itemQuality)
                .itemQuantity(itemQuantity)
                .itemPrice(itemPrice)
                .totalCost(totalCost)
                .itemShopName(itemShopName)
                .itemBuyer(itemBuyer)
                .shopOwner(shopOwner)
                .purchaseDate(purchaseDate)
                .purchaseDay(purchaseDay)
                .itemShop(itemShop)
                .remarks(remarks)
                .status("ACTIVE")
                .build();

        expenseRepository.save(expense);

        // 🧮 Update total expense summary
        Double totalExpenseForUser = expenseRepository.getTotalExpenseByUser(user);
        expense.setTotalExpense(totalExpenseForUser);
        ExpenseManagement savedExpense = expenseRepository.save(expense);

        // ✅ Send Kafka event
        sendKafkaEvent("EXPENSE_ADDED", savedExpense, "Expense entry created successfully");

        return savedExpense;
    }

    /**
     * ✏️ Update existing expense
     */
    public ExpenseManagement updateExpense(Long id, ExpenseManagement updatedExpense) {
        ExpenseManagement existing = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        existing.setItemCategory(updatedExpense.getItemCategory());
        existing.setItemName(updatedExpense.getItemName());
        existing.setItemQuality(updatedExpense.getItemQuality());
        existing.setItemQuantity(updatedExpense.getItemQuantity());
        existing.setItemPrice(updatedExpense.getItemPrice());
        existing.setItemShopName(updatedExpense.getItemShopName());
        existing.setItemBuyer(updatedExpense.getItemBuyer());
        existing.setShopOwner(updatedExpense.getShopOwner());
        existing.setPurchaseDate(updatedExpense.getPurchaseDate());
        existing.setPurchaseDay(updatedExpense.getPurchaseDay());
        existing.setItemShop(updatedExpense.getItemShop());
        existing.setRemarks(updatedExpense.getRemarks());

        double totalCost = (existing.getItemPrice() != null && existing.getItemQuantity() != null)
                ? existing.getItemPrice() * existing.getItemQuantity()
                : 0.0;
        existing.setTotalCost(totalCost);

        ExpenseManagement updated = expenseRepository.save(existing);

        // ✅ Send Kafka event
        sendKafkaEvent("EXPENSE_UPDATED", updated, "Expense entry updated successfully");

        return updated;
    }

    /**
     * ❌ Delete expense (soft delete by status)
     */
    public void deleteExpense(Long id) {
        ExpenseManagement expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found with id: " + id));

        expense.setStatus("ARCHIVED");
        expenseRepository.save(expense);

        // ✅ Send Kafka event
        sendKafkaEvent("EXPENSE_DELETED", expense, "Expense entry archived successfully");
    }

    /**
     * 🔍 Get all expenses
     */
    public List<ExpenseManagement> getAllExpenses() {
        return expenseRepository.findAll();
    }

    /**
     * 🔍 Get expenses by User
     */
    public List<ExpenseManagement> getExpensesByUser(User user) {
        return expenseRepository.findByUser(user);
    }

    /**
     * 🔍 Get expenses by Cattle
     */
    public List<ExpenseManagement> getExpensesByCattle(CattleEntry cattleEntry) {
        return expenseRepository.findByCattleEntry(cattleEntry);
    }

    /**
     * 🔍 Get expense by unique expenseId
     */
    public Optional<ExpenseManagement> getExpenseByExpenseId(String expenseId) {
        return expenseRepository.findByExpenseId(expenseId);
    }

    /**
     * 💰 Get total expense for a specific user
     */
    public Double getTotalExpenseByUser(User user) {
        return expenseRepository.getTotalExpenseByUser(user);
    }

    /**
     * 💰 Get total expense for a specific cattle
     */
    public Double getTotalExpenseByCattle(CattleEntry cattleEntry) {
        return expenseRepository.getTotalExpenseByCattle(cattleEntry);
    }

    /**
     * 💰 Get overall expense across all farmers
     */
    public Double getTotalExpenseOverall() {
        return expenseRepository.getTotalExpenseOverall();
    }



    // ============================================================
    // 📨 PRIVATE HELPER: SEND KAFKA EVENT
    // ============================================================
    private void sendKafkaEvent(String eventType, ExpenseManagement expense, String message) {
        try {
            String payload = EventPayload.expenseJson(
                    eventType,
                    expense.getUser().getUsername(),
                    expense.getUser().getRole().name(),
                    true,
                    message,
                    expense.getExpenseId(),
                    expense.getItemCategory(),
                    expense.getItemName(),
                    expense.getTotalCost(),
                    expense.getItemBuyer(),
                    expense.getPurchaseDay()
            );


            kafkaProducerService.sendToTopic(EXPENSE_TOPIC, payload);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to send Kafka event: " + e.getMessage());
        }
    }
}
