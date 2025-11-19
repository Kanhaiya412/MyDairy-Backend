package com.MyFarmerApp.MyFarmer.dto;

import com.MyFarmerApp.MyFarmer.enums.Shift;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class MilkEntryRequest {

    @NotNull
    private Long userId;

    // remove sending 'day' from client; it will be computed on server
    private String day;

    @NotNull
    private LocalDate date;

    @NotNull
    private Shift shift;

    @NotNull
    @DecimalMin(value = "0.01", message = "Milk quantity must be > 0")
    private Double milkQuantity;

    @NotNull
    @DecimalMin(value = "0.1", message = "Fat must be >= 0.1")
    @DecimalMax(value = "20.0", message = "Fat unrealistic")
    private Double fat;

    @NotNull
    @DecimalMin(value = "0.01", message = "Fat price must be > 0")
    private Double fatPrice;
}
