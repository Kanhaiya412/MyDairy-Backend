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
@RequestMapping("/api/milk")
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

    // ========= NEW CATTLE WISE ENDPOINTS ===========

    @GetMapping("/cattle/{cattleId}")
    public ResponseEntity<?> getMilkByCattle(@PathVariable Long cattleId) {
        return ResponseEntity.ok(milkEntryService.getMilkByCattle(cattleId));
    }

    @GetMapping("/cattle/{cattleId}/last/{days}")
    public ResponseEntity<?> getLastNDays(@PathVariable Long cattleId, @PathVariable int days) {
        return ResponseEntity.ok(milkEntryService.getLastNDays(cattleId, days));
    }

    @GetMapping("/cattle/{cattleId}/range")
    public ResponseEntity<?> getCattleRange(
            @PathVariable Long cattleId,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end
    ) {
        return ResponseEntity.ok(milkEntryService.getMilkByCattleRange(cattleId, start, end));
    }

    // ========= EXISTING USER ENDPOINTS ==========

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getEntriesForUser(
            @PathVariable Long userId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        if (month != null && year != null) {
            YearMonth ym = YearMonth.of(year, month);
            return ResponseEntity.ok(milkEntryService.getEntriesByUserBetween(
                    userId, ym.atDay(1), ym.atEndOfMonth()
            ));
        }
        return ResponseEntity.ok(milkEntryService.getEntriesByUser(userId));
    }
}
