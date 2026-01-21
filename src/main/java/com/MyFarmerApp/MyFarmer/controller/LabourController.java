// src/main/java/com/MyFarmerApp/MyFarmer/controller/LabourController.java
package com.MyFarmerApp.MyFarmer.controller;

import com.MyFarmerApp.MyFarmer.dto.LabourAttendanceRequest;
import com.MyFarmerApp.MyFarmer.dto.LabourDashboardDTO;
import com.MyFarmerApp.MyFarmer.dto.LabourRequest;
import com.MyFarmerApp.MyFarmer.dto.LabourSalaryGenerateRequest;
import com.MyFarmerApp.MyFarmer.entity.Labour;
import com.MyFarmerApp.MyFarmer.entity.LabourAttendance;
import com.MyFarmerApp.MyFarmer.entity.LabourSalary;
import com.MyFarmerApp.MyFarmer.entity.User;
import com.MyFarmerApp.MyFarmer.service.AuthService;
import com.MyFarmerApp.MyFarmer.service.LabourDashboardService;
import com.MyFarmerApp.MyFarmer.service.LabourService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/labour")
@CrossOrigin(origins = "*")
public class LabourController {

    @Autowired
    private LabourService labourService;

    @Autowired
    private AuthService authService;

    @Autowired
    private LabourDashboardService labourDashboardService;


    // ───────────────────────────── LABOUR ──────────────────────────────────

    @PostMapping("/add")
    public ResponseEntity<?> addLabour(
            @RequestBody LabourRequest req,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            Long userId = ((Number) authService.getUserFromToken(authHeader).get("userId")).longValue();
            User user = authService.getUserById(userId);

            Labour saved = labourService.addLabour(user, req);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getLaboursForUser(@PathVariable Long userId) {
        try {
            List<Labour> list = labourService.getLaboursForUser(userId);
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
            return ResponseEntity.ok(updated);
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

    @GetMapping("/{id}")
    public ResponseEntity<?> getLabourById(@PathVariable Long id) {
        try {
            Labour labour = labourService.getLabourById(id);
            return ResponseEntity.ok(labour);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // endpoint:
    @GetMapping("/{labourId}/dashboard")
    public ResponseEntity<LabourDashboardDTO> getLabourDashboard(@PathVariable Long labourId) {
        LabourDashboardDTO dto = labourDashboardService.getDashboard(labourId);
        return ResponseEntity.ok(dto);
    }


}
