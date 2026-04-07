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

    private Long extractUserId() {
        Object principal = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof com.MyFarmerApp.MyFarmer.config.MyFarmerUserDetails) {
            return ((com.MyFarmerApp.MyFarmer.config.MyFarmerUserDetails) principal).getUserId();
        }
        throw new SecurityException("Unauthorized or Invalid Session");
    }

    // ───────────────────────────── LABOUR ──────────────────────────────────

    @PostMapping("/add")
    public ResponseEntity<?> addLabour(
            @RequestBody LabourRequest req    ) {
        try {
            Long userId = extractUserId();
            Labour saved = labourService.addLabour(userId, req);
            return ResponseEntity.ok(LabourMapper.toDTO(saved));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    @PostMapping("/upload-photo")
    public ResponseEntity<?> uploadPhoto(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file    ) {
        try {
            Long userId = extractUserId();
            String photoUrl = labourService.uploadPhoto(userId, file);
            return ResponseEntity.ok(java.util.Collections.singletonMap("photoUrl", photoUrl));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyLabours(
    ) {
        try {
            Long userId = extractUserId();

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
            @RequestBody LabourRequest req    ) {
        try {
            Long userId = extractUserId();
            Labour updated = labourService.updateLabour(userId, id, req);
            return ResponseEntity.ok(LabourMapper.toDTO(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLabour(
            @PathVariable Long id    ) {
        try {
            Long userId = extractUserId();
            labourService.deleteLabour(userId, id);
            return ResponseEntity.ok("Labour deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getLabourById(
            @PathVariable Long id    ) {
        try {
            Long userId = extractUserId();
            Labour labour = labourService.getLabourById(userId, id);
            return ResponseEntity.ok(LabourMapper.toDTO(labour));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ─────────────────────────── ATTENDANCE ─────────────────────────────────

    @PostMapping("/attendance/mark")
    public ResponseEntity<?> markAttendance(
            @RequestBody LabourAttendanceRequest req    ) {
        try {
            Long userId = extractUserId();
            LabourAttendance att = labourService.markAttendance(userId, req);
            return ResponseEntity.ok(att);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/attendance/batch")
    public ResponseEntity<?> markBatchAttendance(
            @RequestBody com.MyFarmerApp.MyFarmer.dto.LabourBatchAttendanceRequest req    ) {
        try {
            Long userId = extractUserId();
            List<LabourAttendance> list = labourService.markBatchAttendance(userId, req);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/attendance/{labourId}")
    public ResponseEntity<?> getAttendanceForMonth(
            @PathVariable Long labourId,
            @RequestParam int month,
            @RequestParam int year    ) {
        try {
            Long userId = extractUserId();
            List<LabourAttendance> list = labourService.getAttendanceForMonth(userId, labourId, month, year);
            return ResponseEntity.ok(list);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ───────────────────────────── SALARY ───────────────────────────────────

    @PostMapping("/salary/generate")
    public ResponseEntity<?> generateSalary(
            @RequestBody LabourSalaryGenerateRequest req    ) {
        try {
            Long userId = extractUserId();
            LabourSalary salary = labourService.generateSalary(userId, req);
            return ResponseEntity.ok(salary);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/salary/{labourId}")
    public ResponseEntity<?> getSalaryHistory(
            @PathVariable Long labourId    ) {
        try {
            Long userId = extractUserId();
            List<LabourSalary> list = labourService.getSalaryHistory(userId, labourId);
            return ResponseEntity.ok(list);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/salary/{salaryId}/paid")
    public ResponseEntity<?> markSalaryPaid(
            @PathVariable Long salaryId,
            @RequestParam Double amount    ) {
        try {
            Long userId = extractUserId();
            LabourSalary upd = labourService.markSalaryPaid(userId, salaryId, amount);
            return ResponseEntity.ok(upd);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/bulk-pay")
    public ResponseEntity<?> payBulk(
            @RequestParam Long labourId,
            @RequestParam Double amount    ) {
        try {
            Long userId = extractUserId();
            labourService.payLumpsumSalary(userId, labourId, amount);
            return ResponseEntity.ok("Bulk payment processed and allocated successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ───────────────────────────── DASHBOARD ────────────────────────────────

    @GetMapping("/{labourId}/dashboard")
    public ResponseEntity<?> getLabourDashboard(
            @PathVariable Long labourId    ) {
        Long userId = extractUserId();
        LabourDashboardDTO dto = labourDashboardService.getDashboard(userId, labourId);
        return ResponseEntity.ok(dto);
    }
}
