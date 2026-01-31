// src/main/java/com/MyFarmerApp/MyFarmer/controller/SalaryAdvanceController.java
package com.MyFarmerApp.MyFarmer.controller;

import com.MyFarmerApp.MyFarmer.dto.SalaryAdvanceRequest;
import com.MyFarmerApp.MyFarmer.dto.SettleAdvanceRequest;
import com.MyFarmerApp.MyFarmer.entity.LabourSalaryAdvance;
import com.MyFarmerApp.MyFarmer.service.SalaryAdvanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SalaryAdvanceController {

    private final SalaryAdvanceService advanceService;

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody SalaryAdvanceRequest req) {
        try {
            LabourSalaryAdvance created = advanceService.addAdvance(req);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/history/{labourId}")
    public ResponseEntity<?> history(
            @PathVariable Long labourId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        try {
            List<LabourSalaryAdvance> list = advanceService.getHistory(labourId, month, year);
            return ResponseEntity.ok(list);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/pending/{labourId}")
    public ResponseEntity<?> pending(
            @PathVariable Long labourId,
            @RequestParam Integer month,
            @RequestParam Integer year
    ) {
        try {
            List<LabourSalaryAdvance> list = advanceService.getPendingForMonth(labourId, month, year);
            return ResponseEntity.ok(list);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/settle")
    public ResponseEntity<?> settle(
            @PathVariable Long id,
            @RequestBody(required = false) SettleAdvanceRequest req
    ) {
        try {
            LabourSalaryAdvance settled = advanceService.settle(id);
            return ResponseEntity.ok(settled);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
