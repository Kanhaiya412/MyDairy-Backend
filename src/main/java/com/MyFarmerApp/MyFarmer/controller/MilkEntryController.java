package com.MyFarmerApp.MyFarmer.controller;

import com.MyFarmerApp.MyFarmer.dto.MilkEntryRequest;
import com.MyFarmerApp.MyFarmer.entity.MilkEntry;
import com.MyFarmerApp.MyFarmer.service.MilkEntryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/milk")   // ✅ FIXED: Added /api prefix
public class MilkEntryController {

    private final MilkEntryService milkEntryService;

    public MilkEntryController(MilkEntryService milkEntryService) {
        this.milkEntryService = milkEntryService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addEntry(@Valid @RequestBody MilkEntryRequest request) {
        MilkEntry saved = milkEntryService.addMilkEntry(request);
        return ResponseEntity.ok(saved);
    }

    /**
     * GET /api/milk/user/{userId}?month=11&year=2025
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getEntriesForUser(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        List<MilkEntry> result;

        if (month != null && year != null) {
            YearMonth ym = YearMonth.of(year, month);
            LocalDate start = ym.atDay(1);
            LocalDate end = ym.atEndOfMonth();
            result = milkEntryService.getEntriesByUserBetween(userId, start, end);
        } else {
            result = milkEntryService.getEntriesByUser(userId);
        }

        return ResponseEntity.ok(result);
    }
}
