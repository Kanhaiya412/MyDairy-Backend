package com.MyFarmerApp.MyFarmer.service;

import com.MyFarmerApp.MyFarmer.dto.CattleEntryRequest;
import com.MyFarmerApp.MyFarmer.entity.CattleEntry;
import com.MyFarmerApp.MyFarmer.entity.User;
import com.MyFarmerApp.MyFarmer.enums.CattleStatus;
import com.MyFarmerApp.MyFarmer.repository.CattleEntryRepository;
import com.MyFarmerApp.MyFarmer.repository.UserRepository;
import com.MyFarmerApp.MyFarmer.util.EventPayload;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class CattleEntryService {

    private final CattleEntryRepository cattleEntryRepository;
    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService;

    public static final String CATTLE_TOPIC = "cattle-events";

    public CattleEntryService(CattleEntryRepository cattleEntryRepository,
                              UserRepository userRepository,
                              KafkaProducerService kafkaProducerService) {
        this.cattleEntryRepository = cattleEntryRepository;
        this.userRepository = userRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    // ============================================================
    // Add new cattle entry
    // ============================================================
    public CattleEntry addCattleEntry(CattleEntryRequest request) {
        // Basic validation
        if (request.getUserId() == null) throw new IllegalArgumentException("userId is required");
        if (request.getCattleId() == null || request.getCattleId().isBlank())
            throw new IllegalArgumentException("cattleId is required");
        if (request.getCattleCategory() == null) throw new IllegalArgumentException("cattleCategory is required");
        if (request.getCattleBreed() == null) throw new IllegalArgumentException("cattleBreed is required");
        if (request.getCattlePurchaseDate() == null) throw new IllegalArgumentException("purchaseDate is required");
        if (request.getTotalCattle() == null) request.setTotalCattle(1);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Ensure unique per user
        if (cattleEntryRepository.existsByCattleIdAndUser(request.getCattleId(), user)) {
            throw new RuntimeException("Cattle ID already exists for this user");
        }

        CattleEntry entry = CattleEntry.builder()
                .user(user)
                .cattleId(request.getCattleId())
                .cattleCategory(request.getCattleCategory())
                .cattleBreed(request.getCattleBreed())
                .gender(request.getGender()) // may be null
                .cattlePurchaseDate(request.getCattlePurchaseDate())
                .cattleDay(request.getCattleDay())
                .cattlePurchaseFrom(request.getCattlePurchaseFrom())
                .cattleName(request.getCattleName())
                .cattlePurchasePrice(request.getCattlePurchasePrice())
                .cattleSoldDate(request.getCattleSoldDate())
                .cattleSoldTo(request.getCattleSoldTo())
                .cattleSoldPrice(request.getCattleSoldPrice())
                .totalCattle(request.getTotalCattle())
                .imageUrl(request.getImageUrl())
                .status(request.getStatus() != null ? request.getStatus() : CattleStatus.ACTIVE)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();

        CattleEntry saved = cattleEntryRepository.save(entry);

        sendKafkaEvent("CATTLE_ENTRY_ADDED", saved, "Cattle entry added successfully");
        return saved;
    }

    // ============================================================
    // Update existing cattle entry (partial updates allowed)
    // ============================================================
    @Transactional
    public CattleEntry updateCattleEntry(Long id, CattleEntryRequest request) {
        CattleEntry existing = cattleEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cattle entry not found"));

        // Only update fields that are provided (null-safe)
        if (request.getCattleCategory() != null) existing.setCattleCategory(request.getCattleCategory());
        if (request.getCattleBreed() != null) existing.setCattleBreed(request.getCattleBreed());
        if (request.getGender() != null) existing.setGender(request.getGender());
        if (request.getCattlePurchaseDate() != null) existing.setCattlePurchaseDate(request.getCattlePurchaseDate());
        if (request.getCattlePurchaseFrom() != null) existing.setCattlePurchaseFrom(request.getCattlePurchaseFrom());
        if (request.getCattleDay() != null) existing.setCattleDay(request.getCattleDay());
        if (request.getCattleName() != null) existing.setCattleName(request.getCattleName());
        if (request.getCattlePurchasePrice() != null) existing.setCattlePurchasePrice(request.getCattlePurchasePrice());
        if (request.getCattleSoldDate() != null) existing.setCattleSoldDate(request.getCattleSoldDate());
        if (request.getCattleSoldTo() != null) existing.setCattleSoldTo(request.getCattleSoldTo());
        if (request.getCattleSoldPrice() != null) existing.setCattleSoldPrice(request.getCattleSoldPrice());
        if (request.getTotalCattle() != null) existing.setTotalCattle(request.getTotalCattle());
        if (request.getImageUrl() != null) existing.setImageUrl(request.getImageUrl());

        // Status handling: sold date implies SOLD
        if (request.getCattleSoldDate() != null) {
            existing.setStatus(CattleStatus.SOLD);
        } else if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }

        existing.setUpdatedAt(LocalDate.now());

        CattleEntry updated = cattleEntryRepository.save(existing);
        sendKafkaEvent("CATTLE_ENTRY_UPDATED", updated, "Cattle entry updated successfully");
        return updated;
    }

    // ============================================================
    // Delete cattle entry
    // ============================================================
    public void deleteCattleEntry(Long id) {
        CattleEntry existing = cattleEntryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cattle entry not found"));
        cattleEntryRepository.delete(existing);
        sendKafkaEvent("CATTLE_ENTRY_DELETED", existing, "Cattle entry deleted successfully");
    }

    // ============================================================
    // Get all cattle by user (newest first)
    // ============================================================
    public List<CattleEntry> getEntriesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return cattleEntryRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // ============================================================
    // Get sold cattle by user
    // ============================================================
    public List<CattleEntry> getSoldCattleByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return cattleEntryRepository.findByUserAndStatus(user, CattleStatus.SOLD);
    }

    // ============================================================
    // Get cattle by cattle code scoped to user
    // ============================================================
    public CattleEntry getCattleByCattleCode(String cattleCode, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return cattleEntryRepository.findByCattleIdAndUser(cattleCode, user)
                .orElseThrow(() -> new RuntimeException("Cattle not found with ID: " + cattleCode));
    }

    // ============================================================
    // Kafka event helper
    // ============================================================
    private void sendKafkaEvent(String eventType, CattleEntry entry, String message) {
        try {
            String payload = EventPayload.cattleJson(
                    eventType,
                    entry.getUser().getUsername(),
                    entry.getUser().getRole().name(),
                    true,
                    message,
                    entry.getCattleId(),
                    entry.getCattleCategory().name(),
                    entry.getCattleBreed().name(),
                    entry.getCattlePurchasePrice(),
                    entry.getCattleName(),
                    entry.getTotalCattle()
            );
            kafkaProducerService.sendToTopic(CATTLE_TOPIC, payload);
        } catch (Exception e) {
            // keep non-blocking - log the error
            System.err.println("⚠️ Failed to send Kafka event: " + e.getMessage());
        }
    }
}
