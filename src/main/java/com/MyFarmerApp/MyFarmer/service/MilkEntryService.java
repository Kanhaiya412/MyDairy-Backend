package com.MyFarmerApp.MyFarmer.service;

import com.MyFarmerApp.MyFarmer.dto.MilkEntryRequest;
import com.MyFarmerApp.MyFarmer.entity.CattleEntry;
import com.MyFarmerApp.MyFarmer.entity.MilkEntry;
import com.MyFarmerApp.MyFarmer.entity.User;
import com.MyFarmerApp.MyFarmer.enums.Shift;
import com.MyFarmerApp.MyFarmer.repository.CattleEntryRepository;
import com.MyFarmerApp.MyFarmer.repository.MilkEntryRepository;
import com.MyFarmerApp.MyFarmer.repository.UserRepository;
import com.MyFarmerApp.MyFarmer.util.EventPayload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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

    @Transactional
    public MilkEntry addMilkEntry(MilkEntryRequest request) {

        if (request.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Future date not allowed");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 🐄 If cattle-wise milk entry
        CattleEntry cattleEntry = null;
        if (request.getCattleId() != null) {
            cattleEntry = cattleEntryRepository.findById(request.getCattleId())
                    .orElseThrow(() -> new RuntimeException("Cattle not found"));
        }

        // Duplicate check only for USER wise entries (not cattle wise)
        if (cattleEntry == null && milkEntryRepository.existsByUserAndDateAndShift(
                user, request.getDate(), request.getShift())) {
            throw new RuntimeException("Entry already exists for this user for date & shift");
        }

        double totalPayment = request.getMilkQuantity() * request.getFatPrice() * request.getFat();

        MilkEntry entry = MilkEntry.builder()
                .user(user)
                .cattleEntry(cattleEntry)  // NEW
                .date(request.getDate())
                .day(request.getDate().getDayOfWeek().toString())
                .shift(request.getShift())
                .milkQuantity(request.getMilkQuantity())
                .fat(request.getFat())
                .fatPrice(request.getFatPrice())
                .totalPayment(totalPayment)
                .build();

        MilkEntry saved = milkEntryRepository.save(entry);

        // Send Kafka event (kept same)
        String payload = EventPayload.json(
                "MILK_ENTRY_ADDED",
                user.getUsername(),
                user.getRole().name(),
                true,
                "Milk entry added successfully",
                request.getMilkQuantity(),
                request.getFat(),
                request.getShift().name(),
                request.getFatPrice(),
                totalPayment
        );
        kafkaProducerService.sendToTopic(KafkaProducerService.MILK_TOPIC, payload);

        return saved;
    }

    // ====== NEW METHODS ======

    public List<MilkEntry> getMilkByCattle(Long cattleId) {
        CattleEntry cattle = cattleEntryRepository.findById(cattleId)
                .orElseThrow(() -> new RuntimeException("Cattle not found"));

        return milkEntryRepository.findByCattleEntryOrderByDateAsc(cattle);
    }

    public List<MilkEntry> getLastNDays(Long cattleId, int days) {
        CattleEntry cattle = cattleEntryRepository.findById(cattleId)
                .orElseThrow(() -> new RuntimeException("Cattle not found"));

        LocalDate from = LocalDate.now().minusDays(days);

        return milkEntryRepository.findByCattleEntryAndDateGreaterThanEqualOrderByDateAsc(cattle, from);
    }

    public List<MilkEntry> getMilkByCattleRange(Long cattleId, LocalDate start, LocalDate end) {
        CattleEntry cattle = cattleEntryRepository.findById(cattleId)
                .orElseThrow(() -> new RuntimeException("Cattle not found"));

        return milkEntryRepository.findByCattleEntryAndDateBetweenOrderByDateAsc(
                cattle, start, end
        );
    }

    // ===== OLD CODE PRESERVED FOR USER =====

    public List<MilkEntry> getEntriesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<MilkEntry> entries = milkEntryRepository.findByUserOrderByDateAsc(user);

        entries.sort(Comparator.comparing(MilkEntry::getDate)
                .thenComparing(MilkEntry::getShift));

        return entries;
    }

    public List<MilkEntry> getEntries(Long userId, Integer month, Integer year) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (month == null || year == null) {
            return milkEntryRepository.findByUserOrderByDateAsc(user);
        }

        return milkEntryRepository.findByUserAndMonth(user, month, year);
    }

    public List<MilkEntry> getEntriesByUserBetween(Long userId, LocalDate start, LocalDate end) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return milkEntryRepository.findByUserAndDateBetweenOrderByDateAsc(user, start, end);
    }
}