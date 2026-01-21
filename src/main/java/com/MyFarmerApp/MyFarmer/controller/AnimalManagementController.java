package com.MyFarmerApp.MyFarmer.controller;

import com.MyFarmerApp.MyFarmer.dto.AnimalManagementRequest;
import com.MyFarmerApp.MyFarmer.service.AnimalManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/animal")
@RequiredArgsConstructor
public class AnimalManagementController {

    private final AnimalManagementService service;

    @PostMapping("/add")
    public ResponseEntity<?> add(@Valid @RequestBody AnimalManagementRequest request) {
        return ResponseEntity.ok(service.addRecord(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody AnimalManagementRequest request) {
        return ResponseEntity.ok(service.updateRecord(id, request));
    }

    @GetMapping("/cattle/{cattleId}")
    public ResponseEntity<?> getHistory(@PathVariable Long cattleId) {
        return ResponseEntity.ok(service.getHistoryByCattle(cattleId));
    }

    @GetMapping("/cattle/{cattleId}/latest")
    public ResponseEntity<?> getLatest(@PathVariable Long cattleId) {
        return ResponseEntity.ok(service.getLatestRecord(cattleId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.deleteRecord(id);
        return ResponseEntity.ok("Animal management record deleted");
    }
}
