package com.MyFarmerApp.MyFarmer.controller;

import com.MyFarmerApp.MyFarmer.dto.ApiResponse;
import com.MyFarmerApp.MyFarmer.dto.ExpenseRequest;
import com.MyFarmerApp.MyFarmer.entity.CattleEntry;
import com.MyFarmerApp.MyFarmer.entity.ExpenseManagement;
import com.MyFarmerApp.MyFarmer.entity.User;
import com.MyFarmerApp.MyFarmer.service.AuthService;
import com.MyFarmerApp.MyFarmer.service.CattleEntryService;
import com.MyFarmerApp.MyFarmer.service.ExpenseManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExpenseManagementController {

    private final ExpenseManagementService expenseService;
    private final AuthService authService;
    private final CattleEntryService cattleService;

    // ✅ ADD NEW EXPENSE (using username from JWT)
    @PostMapping("/add")
    public ResponseEntity<ApiResponse<ExpenseManagement>> addExpense(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ExpenseRequest request) {
        try {
            // ✅ Extract username from JWT token
            var userInfo = authService.getUserFromToken(authHeader);
            String username = (String) userInfo.get("username");
            Long userId = (Long) userInfo.get("userId");

            User user = authService.getUserById(userId);
            CattleEntry cattle = null;

            if (request.getCattleId() != null && !request.getCattleId().isEmpty()) {
                cattle = cattleService.getCattleByCattleCode(request.getCattleId(), userId);
            }

            ExpenseManagement expense = expenseService.createExpense(
                    user,
                    cattle,
                    request.getItemId(),
                    request.getItemCategory(),
                    request.getItemName(),
                    request.getItemQuality(),
                    request.getItemQuantity(),
                    request.getItemPrice(),
                    request.getItemShopName(),
                    request.getItemBuyer(),
                    request.getShopOwner(),
                    request.getPurchaseDate() != null ? request.getPurchaseDate() : LocalDate.now(),
                    request.getPurchaseDay(),
                    request.getItemShop(),
                    request.getRemarks()
            );

            return ResponseEntity.ok(new ApiResponse<>(true, "Expense created successfully", expense));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Error: " + e.getMessage(), null));
        }
    }

    // ✅ UPDATE EXPENSE
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExpenseManagement>> updateExpense(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody ExpenseRequest request) {
        try {
            authService.getUserFromToken(authHeader); // ✅ Just validate token
            ExpenseManagement updated = expenseService.updateExpense(id, convertToEntity(request));
            return ResponseEntity.ok(new ApiResponse<>(true, "Expense updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    // ✅ DELETE EXPENSE
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteExpense(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader) {
        try {
            authService.getUserFromToken(authHeader); // ✅ validate token
            expenseService.deleteExpense(id);
            return ResponseEntity.ok(new ApiResponse<>(true, "Expense archived successfully", "DELETED"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    // ✅ GET ALL EXPENSES (for current logged-in user)
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ExpenseManagement>>> getAllExpenses(
            @RequestHeader("Authorization") String authHeader) {
        try {
            var userInfo = authService.getUserFromToken(authHeader);
            Long userId = (Long) userInfo.get("userId");
            User user = authService.getUserById(userId);

            List<ExpenseManagement> userExpenses = expenseService.getExpensesByUser(user);
            return ResponseEntity.ok(new ApiResponse<>(true, "User expenses fetched successfully", userExpenses));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    // ✅ GET EXPENSE BY EXPENSE ID
    @GetMapping("/find/{expenseId}")
    public ResponseEntity<ApiResponse<ExpenseManagement>> getExpenseByExpenseId(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String expenseId) {
        try {
            authService.getUserFromToken(authHeader); // ✅ validate token
            Optional<ExpenseManagement> expense = expenseService.getExpenseByExpenseId(expenseId);
            return expense
                    .map(value -> ResponseEntity.ok(new ApiResponse<>(true, "Expense found", value)))
                    .orElseGet(() -> ResponseEntity.badRequest()
                            .body(new ApiResponse<>(false, "Expense not found", null)));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, e.getMessage(), null));
        }
    }

    // ✅ Convert DTO → Entity (for update)
    private ExpenseManagement convertToEntity(ExpenseRequest request) {
        return ExpenseManagement.builder()
                .itemId(request.getItemId())
                .itemCategory(request.getItemCategory())
                .itemName(request.getItemName())
                .itemQuality(request.getItemQuality())
                .itemQuantity(request.getItemQuantity())
                .itemPrice(request.getItemPrice())
                .itemShopName(request.getItemShopName())
                .itemBuyer(request.getItemBuyer())
                .shopOwner(request.getShopOwner())
                .purchaseDate(request.getPurchaseDate())
                .purchaseDay(request.getPurchaseDay())
                .itemShop(request.getItemShop())
                .remarks(request.getRemarks())
                .status(request.getStatus())
                .build();
    }
}
