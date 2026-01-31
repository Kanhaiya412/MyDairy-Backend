// src/main/java/com/MyFarmerApp/MyFarmer/service/SalaryAdvanceService.java
package com.MyFarmerApp.MyFarmer.service;

import com.MyFarmerApp.MyFarmer.dto.SalaryAdvanceRequest;
import com.MyFarmerApp.MyFarmer.entity.Labour;
import com.MyFarmerApp.MyFarmer.entity.LabourSalaryAdvance;
import com.MyFarmerApp.MyFarmer.repository.LabourRepository;
import com.MyFarmerApp.MyFarmer.repository.LabourSalaryAdvanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SalaryAdvanceService {

    private final LabourRepository labourRepository;
    private final LabourSalaryAdvanceRepository advanceRepo;

    // ✅ Create advance (salary se cut ho sakta / nahi bhi)
    public LabourSalaryAdvance addAdvance(SalaryAdvanceRequest req) {

        if (req.getLabourId() == null) {
            throw new RuntimeException("labourId is required");
        }
        if (req.getAmount() == null || req.getAmount() <= 0) {
            throw new RuntimeException("amount must be positive");
        }
        if (req.getDate() == null || req.getDate().isBlank()) {
            throw new RuntimeException("date is required (yyyy-MM-dd)");
        }

        Labour labour = labourRepository.findById(req.getLabourId())
                .orElseThrow(() -> new RuntimeException("Labour not found"));

        LabourSalaryAdvance adv = LabourSalaryAdvance.builder()
                .labour(labour)
                .amount(req.getAmount())
                .date(LocalDate.parse(req.getDate()))
                .remarks(req.getRemarks())
                .status("PENDING")
                .build();

        return advanceRepo.save(adv);
    }

    // ✅ History (month/year optional)
    public List<LabourSalaryAdvance> getHistory(Long labourId, Integer month, Integer year) {

        if (labourId == null) throw new RuntimeException("labourId is required");

        if (month != null && year != null) {
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            // ✅ FIX: status filter removed (show PENDING + SETTLED)
            return advanceRepo.findByLabourIdAndDateBetweenOrderByDateDesc(labourId, start, end);
        }

        return advanceRepo.findByLabourIdOrderByDateDesc(labourId);
    }

    // ✅ Pending only (if you want to settle during salary)
    public List<LabourSalaryAdvance> getPendingForMonth(Long labourId, int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        return advanceRepo.findByLabourIdAndDateBetweenAndStatusOrderByDateDesc(
                labourId, start, end, "PENDING"
        );
    }

    public LabourSalaryAdvance settle(Long id) {
        LabourSalaryAdvance adv = advanceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Advance not found"));

        if ("SETTLED".equalsIgnoreCase(adv.getStatus())) {
            return adv;
        }

        adv.setStatus("SETTLED");
        adv.setSettledDate(LocalDate.now());

        return advanceRepo.save(adv);
    }

    public void settleAll(List<LabourSalaryAdvance> advances) {
        if (advances == null || advances.isEmpty()) return;

        advances.forEach(a -> {
            a.setStatus("SETTLED");
            a.setSettledDate(LocalDate.now());
        });

        advanceRepo.saveAll(advances);
    }
}
