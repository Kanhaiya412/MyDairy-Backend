package com.MyFarmerApp.MyFarmer.service;

import com.MyFarmerApp.MyFarmer.dto.MilkEntryRequest;
import com.MyFarmerApp.MyFarmer.entity.CattleEntry;
import com.MyFarmerApp.MyFarmer.entity.MilkEntry;
import com.MyFarmerApp.MyFarmer.entity.User;
import com.MyFarmerApp.MyFarmer.exception.DuplicateMilkEntryException;
import com.MyFarmerApp.MyFarmer.exception.ResourceNotFoundException;
import com.MyFarmerApp.MyFarmer.repository.CattleEntryRepository;
import com.MyFarmerApp.MyFarmer.repository.MilkEntryRepository;
import com.MyFarmerApp.MyFarmer.repository.UserRepository;
import com.MyFarmerApp.MyFarmer.util.EventPayload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class MilkEntryService {

    private final MilkEntryRepository milkEntryRepository;
    private final UserRepository userRepository;
    private final CattleEntryRepository cattleEntryRepository;
    private final KafkaProducerService kafkaProducerService;

    public MilkEntryService(MilkEntryRepository milkEntryRepository,
                            UserRepository userRepository,
                            CattleEntryRepository cattleEntryRepository,
                            KafkaProducerService kafkaProducerService) {
        this.milkEntryRepository = milkEntryRepository;
        this.userRepository = userRepository;
        this.cattleEntryRepository = cattleEntryRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    // ✅ ADD ENTRY (Prevent duplicates)
    @Transactional
    public MilkEntry addMilkEntry(MilkEntryRequest request) {

        if (request.getDate() == null) {
            throw new IllegalArgumentException("Date is required");
        }

        if (request.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Future date not allowed");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // 🐄 optional cattle-wise
        CattleEntry cattleEntry = null;
        if (request.getCattleId() != null) {
            cattleEntry = cattleEntryRepository.findById(request.getCattleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cattle not found"));
        }

        // ✅ DUPLICATE CHECK (General + Cattle wise)
        boolean duplicate;

        if (cattleEntry == null) {
            duplicate = milkEntryRepository.existsByUserAndDateAndShift(
                    user, request.getDate(), request.getShift()
            );
        } else {
            duplicate = milkEntryRepository.existsByUserAndCattleEntryAndDateAndShift(
                    user, cattleEntry, request.getDate(), request.getShift()
            );
        }

        if (duplicate) {
            throw new DuplicateMilkEntryException(
                    "Duplicate Entry ❌ Already exists for this Date & Shift"
            );
        }

        // ✅ Safe total calculation
        double qty = request.getMilkQuantity() != null ? request.getMilkQuantity() : 0.0;
        double fat = request.getFat() != null ? request.getFat() : 0.0;
        double price = request.getFatPrice() != null ? request.getFatPrice() : 0.0;

        double totalPayment = qty * fat * price;

        MilkEntry entry = MilkEntry.builder()
                .user(user)
                .cattleEntry(cattleEntry)
                .date(request.getDate())
                .day(request.getDate().getDayOfWeek().name()) // ✅ MONDAY, TUESDAY...
                .shift(request.getShift())
                .milkQuantity(qty)
                .fat(fat)
                .fatPrice(price)
                .totalPayment(totalPayment)
                .build();

        MilkEntry saved = milkEntryRepository.save(entry);

        // ✅ Kafka event
        String payload = EventPayload.json(
                "MILK_ENTRY_ADDED",
                user.getUsername(),
                user.getRole().name(),
                true,
                "Milk entry added successfully",
                qty,
                fat,
                request.getShift().name(),
                price,
                totalPayment
        );
        kafkaProducerService.sendToTopic(KafkaProducerService.MILK_TOPIC, payload);

        return saved;
    }

    // ✅ UPDATE ENTRY (Edit)
    @Transactional
    public MilkEntry updateMilkEntry(MilkEntryRequest request) {

        if (request.getDate() == null) {
            throw new IllegalArgumentException("Date is required");
        }

        if (request.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Future date not allowed");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        CattleEntry cattleEntry = null;
        if (request.getCattleId() != null) {
            cattleEntry = cattleEntryRepository.findById(request.getCattleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cattle not found"));
        }

        MilkEntry entry;

        // ✅ Find existing entry by (user + date + shift + cattle)
        if (cattleEntry == null) {
            entry = milkEntryRepository.findByUserAndDateAndShift(
                            user, request.getDate(), request.getShift()
                    )
                    .orElseThrow(() -> new ResourceNotFoundException("Milk entry not found for update"));
        } else {
            entry = milkEntryRepository.findByUserAndCattleEntryAndDateAndShift(
                            user, cattleEntry, request.getDate(), request.getShift()
                    )
                    .orElseThrow(() -> new ResourceNotFoundException("Milk entry not found for update"));
        }

        // ✅ Safe update (only values)
        double qty = request.getMilkQuantity() != null ? request.getMilkQuantity() : entry.getMilkQuantity();
        double fat = request.getFat() != null ? request.getFat() : entry.getFat();
        double price = request.getFatPrice() != null ? request.getFatPrice() : entry.getFatPrice();

        double totalPayment = qty * fat * price;

        entry.setMilkQuantity(qty);
        entry.setFat(fat);
        entry.setFatPrice(price);
        entry.setTotalPayment(totalPayment);

        // (Optional) update day again
        entry.setDay(request.getDate().getDayOfWeek().name());

        MilkEntry updated = milkEntryRepository.save(entry);

        // ✅ Kafka event
        String payload = EventPayload.json(
                "MILK_ENTRY_UPDATED",
                user.getUsername(),
                user.getRole().name(),
                true,
                "Milk entry updated successfully",
                qty,
                fat,
                request.getShift().name(),
                price,
                totalPayment
        );
        kafkaProducerService.sendToTopic(KafkaProducerService.MILK_TOPIC, payload);

        return updated;
    }

    // ✅ DELETE ENTRY
    @Transactional
    public void deleteMilkEntry(Long entryId) {
        MilkEntry entry = milkEntryRepository.findById(entryId)
                .orElseThrow(() -> new ResourceNotFoundException("Milk entry not found"));

        milkEntryRepository.delete(entry);

        // (Optional) Kafka event
        String payload = EventPayload.json(
                "MILK_ENTRY_DELETED",
                entry.getUser().getUsername(),
                entry.getUser().getRole().name(),
                true,
                "Milk entry deleted successfully",
                entry.getMilkQuantity(),
                entry.getFat(),
                entry.getShift().name(),
                entry.getFatPrice(),
                entry.getTotalPayment()
        );
        kafkaProducerService.sendToTopic(KafkaProducerService.MILK_TOPIC, payload);
    }

    // ========= CATTLE WISE READ METHODS =========

    public List<MilkEntry> getMilkByCattle(Long cattleId) {
        CattleEntry cattle = cattleEntryRepository.findById(cattleId)
                .orElseThrow(() -> new ResourceNotFoundException("Cattle not found"));

        return milkEntryRepository.findByCattleEntryOrderByDateAsc(cattle);
    }

    public List<MilkEntry> getLastNDays(Long cattleId, int days) {
        CattleEntry cattle = cattleEntryRepository.findById(cattleId)
                .orElseThrow(() -> new ResourceNotFoundException("Cattle not found"));

        LocalDate from = LocalDate.now().minusDays(days);

        return milkEntryRepository.findByCattleEntryAndDateGreaterThanEqualOrderByDateAsc(cattle, from);
    }

    public List<MilkEntry> getMilkByCattleRange(Long cattleId, LocalDate start, LocalDate end) {
        CattleEntry cattle = cattleEntryRepository.findById(cattleId)
                .orElseThrow(() -> new ResourceNotFoundException("Cattle not found"));

        return milkEntryRepository.findByCattleEntryAndDateBetweenOrderByDateAsc(
                cattle, start, end
        );
    }

    // ========= USER READ METHODS =========

    public List<MilkEntry> getEntriesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<MilkEntry> entries = milkEntryRepository.findByUserOrderByDateAsc(user);

        entries.sort(Comparator.comparing(MilkEntry::getDate)
                .thenComparing(MilkEntry::getShift));

        return entries;
    }

    public List<MilkEntry> getEntries(Long userId, Integer month, Integer year) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (month == null || year == null) {
            return milkEntryRepository.findByUserOrderByDateAsc(user);
        }

        return milkEntryRepository.findByUserAndMonth(user, month, year);
    }

    public List<MilkEntry> getEntriesByUserBetween(Long userId, LocalDate start, LocalDate end) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return milkEntryRepository.findByUserAndDateBetweenOrderByDateAsc(user, start, end);
    }
}
