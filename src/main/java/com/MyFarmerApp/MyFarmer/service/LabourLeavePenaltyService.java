package com.MyFarmerApp.MyFarmer.service;

import com.MyFarmerApp.MyFarmer.dto.contracts.CreatePenaltyRequest;
import com.MyFarmerApp.MyFarmer.dto.contracts.MarkPenaltyPaidRequest;
import com.MyFarmerApp.MyFarmer.entity.Labour;
import com.MyFarmerApp.MyFarmer.entity.contracts.LabourLeavePenalty;
import com.MyFarmerApp.MyFarmer.repository.LabourRepository;
import com.MyFarmerApp.MyFarmer.repository.contracts.LabourLeavePenaltyRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class LabourLeavePenaltyService {

    private final LabourRepository labourRepository;
    private final LabourLeavePenaltyRepository penaltyRepo;

    public LabourLeavePenaltyService(
            LabourRepository labourRepository,
            LabourLeavePenaltyRepository penaltyRepo
    ) {
        this.labourRepository = labourRepository;
        this.penaltyRepo = penaltyRepo;
    }

    @Transactional
    public LabourLeavePenalty createPenalty(CreatePenaltyRequest req) {

        Labour labour = labourRepository.findById(req.getLabourId())
                .orElseThrow(() -> new RuntimeException("Labour not found"));

        LabourLeavePenalty penalty = LabourLeavePenalty.builder()
                .labour(labour)
                .date(LocalDate.parse(req.getDate()))
                .extraLeaves(req.getExtraLeaves())
                .penaltyAmount(req.getPenaltyAmount())
                .reason(req.getReason())
                .status("UNPAID")
                .createdAt(LocalDate.now())
                .build();

        return penaltyRepo.save(penalty);
    }

    public List<LabourLeavePenalty> getPenaltyHistory(Long labourId) {
        Labour labour = labourRepository.findById(labourId)
                .orElseThrow(() -> new RuntimeException("Labour not found"));
        return penaltyRepo.findByLabourOrderByDateDesc(labour);
    }

    @Transactional
    public LabourLeavePenalty markPenaltyPaid(Long penaltyId, MarkPenaltyPaidRequest req) {
        LabourLeavePenalty penalty = penaltyRepo.findById(penaltyId)
                .orElseThrow(() -> new RuntimeException("Penalty not found"));

        penalty.setStatus("PAID");
        penalty.setPaidDate(LocalDate.now());

        return penaltyRepo.save(penalty);
    }
}
