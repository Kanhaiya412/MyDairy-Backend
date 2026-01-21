package com.MyFarmerApp.MyFarmer.controller;

import com.MyFarmerApp.MyFarmer.dto.SalaryAdvanceRequest;
import com.MyFarmerApp.MyFarmer.dto.SettleAdvanceRequest;
import com.MyFarmerApp.MyFarmer.entity.LabourSalaryAdvance;
import com.MyFarmerApp.MyFarmer.service.SalaryAdvanceService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SalaryAdvanceController {

    private final SalaryAdvanceService advanceService;

    @PostMapping("/create")
    public LabourSalaryAdvance create(@RequestBody SalaryAdvanceRequest req) {
        return advanceService.addAdvance(req);
    }

    @GetMapping("/history/{labourId}")
    public List<LabourSalaryAdvance> history(
            @PathVariable Long labourId,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year
    ) {
        return advanceService.getHistory(labourId, month, year);
    }

    @GetMapping("/pending/{labourId}")
    public List<LabourSalaryAdvance> pending(
            @PathVariable Long labourId,
            @RequestParam Integer month,
            @RequestParam Integer year
    ) {
        return advanceService.getPendingForMonth(labourId, month, year);
    }

    @PostMapping("/{id}/settle")
    public LabourSalaryAdvance settle(@PathVariable Long id,
                                      @RequestBody(required = false) SettleAdvanceRequest req) {
        return advanceService.settle(id);
    }
}
