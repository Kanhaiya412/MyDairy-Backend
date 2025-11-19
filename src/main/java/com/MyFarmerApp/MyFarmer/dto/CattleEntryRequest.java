package com.MyFarmerApp.MyFarmer.dto;

import com.MyFarmerApp.MyFarmer.enums.CattleCategory;
import com.MyFarmerApp.MyFarmer.enums.CattleBreed;
import com.MyFarmerApp.MyFarmer.enums.CattleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * ✅ DTO for transferring cattle entry data between frontend and backend.
 * Used for both Add & Update operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CattleEntryRequest {

    // 🧑‍🌾 Associated user (farmer) ID
    private Long userId;

    // 🐮 Basic cattle details
    private String cattleId;                         // Unique Cattle Code (e.g., CAT-001)
    private String cattleName;                       // Display name (e.g., "Laxmi")
    private CattleCategory cattleCategory;            // COW / BUFFALO
    private CattleBreed cattleBreed;                  // GIR / JAFRAWADI / etc.
    private Integer totalCattle;                      // Total number of cattle (optional)

    // 📅 Purchase details
    private LocalDate cattlePurchaseDate;             // Purchase date
    private String cattleDay;                         // Auto-filled day (Monday, etc.)
    private String cattlePurchaseFrom;                // Seller/source
    private Double cattlePurchasePrice;               // Purchase price in ₹

    // 💰 Sale details (optional)
    private LocalDate cattleSoldDate;                 // Sale date
    private String cattleSoldTo;                      // Buyer name
    private Double cattleSoldPrice;                   // Sale price

    // ⚙️ Status of the cattle (ACTIVE / SOLD / DEAD / etc.)
    private CattleStatus status;
}
