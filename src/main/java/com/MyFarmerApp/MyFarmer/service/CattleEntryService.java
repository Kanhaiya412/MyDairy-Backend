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

import java.util.List;

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
    // ➕ ADD NEW CATTLE ENTRY
    // ============================================================
    public CattleEntry addCattleEntry(CattleEntryRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Ensure cattleId is unique per user
        if (cattleEntryRepository.existsByCattleIdAndUser(request.getCattleId(), user)) {
            throw new RuntimeException("Cattle ID already exists for this user");
        }

        CattleEntry entry = CattleEntry.builder()
                .user(user)
                .cattleId(request.getCattleId())
                .cattleCategory(request.getCattleCategory())
                .cattleBreed(request.getCattleBreed())
                .cattlePurchaseDate(request.getCattlePurchaseDate())
                .cattleDay(request.getCattleDay())
                .cattlePurchaseFrom(request.getCattlePurchaseFrom())
                .cattlename(request.getCattleName())
                .cattlePurchasePrice(request.getCattlePurchasePrice())
                .cattleSoldDate(request.getCattleSoldDate())
                .cattleSoldTo(request.getCattleSoldTo())
                .cattleSoldPrice(request.getCattleSoldPrice())
                .totalCattle(request.getTotalCattle())
                .status(request.getStatus() != null ? request.getStatus() : CattleStatus.ACTIVE)
                .build();

        CattleEntry saved = cattleEntryRepository.save(entry);

        sendKafkaEvent("CATTLE_ENTRY_ADDED", saved, "Cattle entry added successfully");
        return saved;
    }

    // ============================================================
    // ✏️ UPDATE EXISTING CATTLE ENTRY (NULL-SAFE)
    // ============================================================
    @Transactional
    public CattleEntry updateCattleEntry(Long cattleId, CattleEntryRequest request) {
        CattleEntry existing = cattleEntryRepository.findById(cattleId)
                .orElseThrow(() -> new RuntimeException("Cattle entry not found"));

        if (request.getCattleCategory() != null)
            existing.setCattleCategory(request.getCattleCategory());
        if (request.getCattleBreed() != null)
            existing.setCattleBreed(request.getCattleBreed());
        if (request.getCattlePurchaseDate() != null)
            existing.setCattlePurchaseDate(request.getCattlePurchaseDate());
        if (request.getCattlePurchaseFrom() != null)
            existing.setCattlePurchaseFrom(request.getCattlePurchaseFrom());
        if (request.getCattleDay() != null)
            existing.setCattleDay(request.getCattleDay());
        if (request.getCattleName() != null)
            existing.setCattlename(request.getCattleName());
        if (request.getCattlePurchasePrice() != null)
            existing.setCattlePurchasePrice(request.getCattlePurchasePrice());
        if (request.getCattleSoldDate() != null)
            existing.setCattleSoldDate(request.getCattleSoldDate());
        if (request.getCattleSoldTo() != null)
            existing.setCattleSoldTo(request.getCattleSoldTo());
        if (request.getCattleSoldPrice() != null)
            existing.setCattleSoldPrice(request.getCattleSoldPrice());
        if (request.getTotalCattle() != null)
            existing.setTotalCattle(request.getTotalCattle());

        // ✅ Status handling
        if (request.getCattleSoldDate() != null) {
            existing.setStatus(CattleStatus.SOLD);
        } else if (request.getStatus() != null) {
            existing.setStatus(request.getStatus());
        }

        CattleEntry updated = cattleEntryRepository.save(existing);
        sendKafkaEvent("CATTLE_ENTRY_UPDATED", updated, "Cattle entry updated successfully");
        return updated;
    }

    // ============================================================
    // ❌ DELETE CATTLE ENTRY
    // ============================================================
    public void deleteCattleEntry(Long cattleId) {
        CattleEntry existing = cattleEntryRepository.findById(cattleId)
                .orElseThrow(() -> new RuntimeException("Cattle entry not found"));

        cattleEntryRepository.delete(existing);
        sendKafkaEvent("CATTLE_ENTRY_DELETED", existing, "Cattle entry deleted successfully");
    }

    // ============================================================
    // 🔍 GET ALL CATTLE BY USER
    // ============================================================
    public List<CattleEntry> getEntriesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return cattleEntryRepository.findByUser(user);
    }

    // ============================================================
    // 🔍 GET ALL SOLD CATTLE BY USER
    // ============================================================
    public List<CattleEntry> getSoldCattleByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return cattleEntryRepository.findByUserAndStatus(user, CattleStatus.SOLD);
    }

    // ============================================================
    // 🔍 GET CATTLE BY CODE — SCOPED TO USER
    // ============================================================
    public CattleEntry getCattleByCattleCode(String cattleCode, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return cattleEntryRepository.findByCattleIdAndUser(cattleCode, user)
                .orElseThrow(() -> new RuntimeException("Cattle not found with ID: " + cattleCode));
    }

    // ============================================================
    // 📨 SEND KAFKA EVENT
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
                    entry.getCattlename(),
                    entry.getTotalCattle()
            );
            kafkaProducerService.sendToTopic(CATTLE_TOPIC, payload);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to send Kafka event: " + e.getMessage());
        }
    }
}
