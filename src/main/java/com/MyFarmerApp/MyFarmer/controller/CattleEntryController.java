package com.MyFarmerApp.MyFarmer.controller;

import com.MyFarmerApp.MyFarmer.dto.CattleEntryRequest;
import com.MyFarmerApp.MyFarmer.entity.CattleEntry;
import com.MyFarmerApp.MyFarmer.enums.CattleStatus;
import com.MyFarmerApp.MyFarmer.service.CattleEntryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cattle")
@CrossOrigin(origins = "*") // ✅ Allow mobile app access (React Native)
public class CattleEntryController {

    private final CattleEntryService cattleEntryService;

    public CattleEntryController(CattleEntryService cattleEntryService) {
        this.cattleEntryService = cattleEntryService;
    }

    // ➕ Add new cattle entry
    @PostMapping("/add")
    public ResponseEntity<?> addCattleEntry(@RequestBody CattleEntryRequest request) {
        try {
            if (request.getStatus() == null) request.setStatus(CattleStatus.ACTIVE);
            CattleEntry savedEntry = cattleEntryService.addCattleEntry(request);
            return ResponseEntity.ok(savedEntry);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 🔍 Get all cattle by user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CattleEntry>> getCattleEntriesByUser(@PathVariable Long userId) {
        List<CattleEntry> entries = cattleEntryService.getEntriesByUser(userId);
        return ResponseEntity.ok(entries);
    }

    // 🔍 Get all sold cattle by user
    @GetMapping("/user/{userId}/sold")
    public ResponseEntity<List<CattleEntry>> getSoldCattleEntriesByUser(@PathVariable Long userId) {
        List<CattleEntry> entries = cattleEntryService.getSoldCattleByUser(userId);
        return ResponseEntity.ok(entries);
    }

    // 🔍 Get cattle filtered by category
    @GetMapping("/user/{userId}/category/{category}")
    public ResponseEntity<List<CattleEntry>> getCattleEntriesByCategory(
            @PathVariable Long userId,
            @PathVariable String category) {

        List<CattleEntry> allEntries = cattleEntryService.getEntriesByUser(userId);
        List<CattleEntry> filtered = allEntries.stream()
                .filter(e -> e.getCattleCategory().name().equalsIgnoreCase(category))
                .toList();

        return ResponseEntity.ok(filtered);
    }

    // ✏️ Update cattle
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCattle(@PathVariable Long id, @RequestBody CattleEntryRequest request) {
        try {
            if (request.getCattleSoldDate() != null) {
                request.setStatus(CattleStatus.SOLD);
            } else if (request.getStatus() == null) {
                request.setStatus(CattleStatus.ACTIVE);
            }
            CattleEntry updated = cattleEntryService.updateCattleEntry(id, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ❌ Delete cattle
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCattle(@PathVariable Long id) {
        cattleEntryService.deleteCattleEntry(id);
        return ResponseEntity.ok("Cattle entry deleted successfully");
    }

    // 🔍 Get cattle by cattleCode for a specific user
    @GetMapping("/user/{userId}/code/{cattleCode}")
    public ResponseEntity<?> getCattleByCode(@PathVariable Long userId, @PathVariable String cattleCode) {
        try {
            CattleEntry cattle = cattleEntryService.getCattleByCattleCode(cattleCode, userId);
            return ResponseEntity.ok(cattle);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
