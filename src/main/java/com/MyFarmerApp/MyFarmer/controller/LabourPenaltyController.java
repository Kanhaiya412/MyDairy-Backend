package com.MyFarmerApp.MyFarmer.controller;

import com.MyFarmerApp.MyFarmer.dto.contracts.CreatePenaltyRequest;
import com.MyFarmerApp.MyFarmer.dto.contracts.MarkPenaltyPaidRequest;
import com.MyFarmerApp.MyFarmer.entity.contracts.LabourLeavePenalty;
import com.MyFarmerApp.MyFarmer.service.LabourLeavePenaltyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/labour/penalty")
@CrossOrigin(origins = "*")
public class LabourPenaltyController {

    private final LabourLeavePenaltyService penaltyService;

    public LabourPenaltyController(LabourLeavePenaltyService penaltyService) {
        this.penaltyService = penaltyService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addPenalty(@RequestBody CreatePenaltyRequest req) {
        return ResponseEntity.ok(penaltyService.createPenalty(req));
    }

    @GetMapping("/{labourId}")
    public ResponseEntity<?> list(@PathVariable Long labourId) {
        return ResponseEntity.ok(penaltyService.getPenaltyHistory(labourId));
    }

    @PostMapping("/{penaltyId}/paid")
    public ResponseEntity<?> markPaid(
            @PathVariable Long penaltyId,
            @RequestBody MarkPenaltyPaidRequest req
    ) {
        return ResponseEntity.ok(penaltyService.markPenaltyPaid(penaltyId, req));
    }
}
