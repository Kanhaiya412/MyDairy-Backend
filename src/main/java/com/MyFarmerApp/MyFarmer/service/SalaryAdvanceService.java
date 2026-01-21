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

    // ---------------------------------------------
    // CREATE ADVANCE
    // ---------------------------------------------
    public LabourSalaryAdvance addAdvance(SalaryAdvanceRequest req) {

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

    // ---------------------------------------------
    // HISTORY (MONTH/YEAR optional)
    // ---------------------------------------------
    public List<LabourSalaryAdvance> getHistory(Long labourId, Integer month, Integer year) {

        if (month != null && year != null) {
            LocalDate start = LocalDate.of(year, month, 1);
            LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

            return advanceRepo.findByLabourIdAndDateBetweenAndStatus(labourId, start, end, "PENDING");
        }

        return advanceRepo.findByLabourIdOrderByDateDesc(labourId);
    }

    // ---------------------------------------------
    // GET PENDING ADVANCES FOR SALARY DEDUCTION
    // ---------------------------------------------
    public List<LabourSalaryAdvance> getPendingForMonth(Long labourId, int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        return advanceRepo.findByLabourIdAndDateBetweenAndStatus(
                labourId, start, end, "PENDING"
        );
    }

    // ---------------------------------------------
    // SETTLE ADVANCE
    // ---------------------------------------------
    public LabourSalaryAdvance settle(Long id) {
        LabourSalaryAdvance adv = advanceRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Advance not found"));

        adv.setStatus("SETTLED");
        adv.setSettledDate(LocalDate.now());

        return advanceRepo.save(adv);
    }

    // ---------------------------------------------
    // AUTO SETTLE MULTIPLE ADVANCES (used in salary)
    // ---------------------------------------------
    public void settleAll(List<LabourSalaryAdvance> advances) {
        advances.forEach(a -> {
            a.setStatus("SETTLED");
            a.setSettledDate(LocalDate.now());
        });
        advanceRepo.saveAll(advances);
    }
}
