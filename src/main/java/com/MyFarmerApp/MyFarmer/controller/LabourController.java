// src/main/java/com/MyFarmerApp/MyFarmer/controller/LabourController.java
package com.MyFarmerApp.MyFarmer.controller;

import com.MyFarmerApp.MyFarmer.dto.*;
import com.MyFarmerApp.MyFarmer.entity.Labour;
import com.MyFarmerApp.MyFarmer.entity.LabourAttendance;
import com.MyFarmerApp.MyFarmer.entity.LabourSalary;
import com.MyFarmerApp.MyFarmer.entity.User;
import com.MyFarmerApp.MyFarmer.mapper.LabourMapper;
import com.MyFarmerApp.MyFarmer.service.AuthService;
import com.MyFarmerApp.MyFarmer.service.LabourDashboardService;
import com.MyFarmerApp.MyFarmer.service.LabourService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/labour")
@CrossOrigin(origins = "*")
public class LabourController {

    private final LabourService labourService;
    private final AuthService authService;
    private final LabourDashboardService labourDashboardService;

    public LabourController(
            LabourService labourService,
            AuthService authService,
            LabourDashboardService labourDashboardService
    ) {
        this.labourService = labourService;
        this.authService = authService;
        this.labourDashboardService = labourDashboardService;
    }

    private Long extractUserId(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            throw new RuntimeException("Authorization header missing");
        }
        Object idObj = authService.getUserFromToken(authHeader).get("userId");
        return ((Number) idObj).longValue();
    }

    // ───────────────────────────── LABOUR ──────────────────────────────────

    @PostMapping("/add")
    public ResponseEntity<?> addLabour(
            @RequestBody LabourRequest req,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            Long userId = extractUserId(authHeader);
            User user = authService.getUserById(userId);

            Labour saved = labourService.addLabour(user, req);
            return ResponseEntity.ok(LabourMapper.toDTO(saved));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyLabours(
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            Long userId = extractUserId(authHeader);

            List<LabourResponseDTO> list = labourService.getLaboursForUser(userId)
                    .stream()
                    .map(LabourMapper::toDTO)
                    .toList();

            return ResponseEntity.ok(list);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateLabour(
            @PathVariable Long id,
            @RequestBody LabourRequest req
    ) {
        try {
            Labour updated = labourService.updateLabour(id, req);
            return ResponseEntity.ok(LabourMapper.toDTO(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLabour(@PathVariable Long id) {
        try {
            labourService.deleteLabour(id);
            return ResponseEntity.ok("Labour deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLabourById(@PathVariable Long id) {
        try {
            Labour labour = labourService.getLabourById(id);
            return ResponseEntity.ok(LabourMapper.toDTO(labour));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ─────────────────────────── ATTENDANCE ─────────────────────────────────

    @PostMapping("/attendance/mark")
    public ResponseEntity<?> markAttendance(@RequestBody LabourAttendanceRequest req) {
        try {
            LabourAttendance att = labourService.markAttendance(req);
            return ResponseEntity.ok(att);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/attendance/{labourId}")
    public ResponseEntity<?> getAttendanceForMonth(
            @PathVariable Long labourId,
            @RequestParam int month,
            @RequestParam int year
    ) {
        try {
            List<LabourAttendance> list = labourService.getAttendanceForMonth(labourId, month, year);
            return ResponseEntity.ok(list);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ───────────────────────────── SALARY ───────────────────────────────────

    @PostMapping("/salary/generate")
    public ResponseEntity<?> generateSalary(@RequestBody LabourSalaryGenerateRequest req) {
        try {
            LabourSalary salary = labourService.generateSalary(req);
            return ResponseEntity.ok(salary);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/salary/{labourId}")
    public ResponseEntity<?> getSalaryHistory(@PathVariable Long labourId) {
        try {
            List<LabourSalary> list = labourService.getSalaryHistory(labourId);
            return ResponseEntity.ok(list);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/salary/{salaryId}/paid")
    public ResponseEntity<?> markSalaryPaid(@PathVariable Long salaryId) {
        try {
            LabourSalary upd = labourService.markSalaryPaid(salaryId);
            return ResponseEntity.ok(upd);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ───────────────────────────── DASHBOARD ────────────────────────────────

    @GetMapping("/{labourId}/dashboard")
    public ResponseEntity<?> getLabourDashboard(@PathVariable Long labourId) {
        LabourDashboardDTO dto = labourDashboardService.getDashboard(labourId);
        return ResponseEntity.ok(dto);
    }
}
