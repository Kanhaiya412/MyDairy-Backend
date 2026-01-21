package com.MyFarmerApp.MyFarmer.dto;

import com.MyFarmerApp.MyFarmer.enums.CattleBreed;
import com.MyFarmerApp.MyFarmer.enums.CattleCategory;
import com.MyFarmerApp.MyFarmer.enums.CattleStatus;
import com.MyFarmerApp.MyFarmer.enums.CattleGender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for creating/updating cattle. Server handles timestamps.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CattleEntryRequest {

    private Long userId;

    // Identification
    private String cattleId;          // Unique code (required on create)
    private String cattleName;
    private CattleCategory cattleCategory;
    private CattleBreed cattleBreed;
    private CattleGender gender;      // NEW

    // Purchase details
    private LocalDate cattlePurchaseDate;
    private String cattleDay;
    private String cattlePurchaseFrom;
    private Double cattlePurchasePrice;

    // Sale details (optional)
    private LocalDate cattleSoldDate;
    private String cattleSoldTo;
    private Double cattleSoldPrice;

    // Count / metadata
    private Integer totalCattle;

    // NEW: image link (optional)
    private String imageUrl;

    // Optional status override
    private CattleStatus status;
}
