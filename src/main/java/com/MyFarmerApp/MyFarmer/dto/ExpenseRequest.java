package com.MyFarmerApp.MyFarmer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * ✅ DTO for transferring Expense data between frontend and backend.
 * Supports both Add & Update operations in ExpenseManagementController.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseRequest {

    // 👤 Associated user (farmer)
    private Long userId; // required

    // 🐄 Optional: link expense to specific cattle using string code (e.g., "CAT-06")
    private String cattleId; // ✅ Changed from Long → String

    // 🧾 Item details
    private String itemId;
    private String itemCategory;
    private String itemName;
    private String itemQuality;

    // 🔢 Quantity and price
    private Double itemQuantity;
    private Double itemPrice;

    // 🏪 Shop and purchase details
    private String itemShopName;
    private String itemBuyer;
    private String shopOwner;
    private String itemShop;
    private LocalDate purchaseDate;
    private String purchaseDay;

    // 🗒️ Optional metadata
    private String remarks;

    // ⚙️ Status
    private String status;
}
