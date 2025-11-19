package com.MyFarmerApp.MyFarmer.service;

import com.MyFarmerApp.MyFarmer.dto.MilkEntryRequest;
import com.MyFarmerApp.MyFarmer.entity.MilkEntry;
import com.MyFarmerApp.MyFarmer.entity.User;
import com.MyFarmerApp.MyFarmer.enums.Shift;
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
    private final KafkaProducerService kafkaProducerService;

    public MilkEntryService(MilkEntryRepository milkEntryRepository,
                            UserRepository userRepository,
                            KafkaProducerService kafkaProducerService) {
        this.milkEntryRepository = milkEntryRepository;
        this.userRepository = userRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    @Transactional
    public MilkEntry addMilkEntry(MilkEntryRequest request) {
        // basic validations (DTO should have validated most)
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Future date not allowed");
        }
        if (request.getMilkQuantity() <= 0) {
            throw new IllegalArgumentException("Milk quantity must be greater than zero");
        }
        if (request.getFat() <= 0 || request.getFat() > 20) {
            throw new IllegalArgumentException("Fat value out of range");
        }
        if (request.getFatPrice() <= 0) {
            throw new IllegalArgumentException("Fat price must be > 0");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // check duplicate (same user, date, shift)
        if (milkEntryRepository.existsByUserAndDateAndShift(user, request.getDate(), request.getShift())) {
            throw new RuntimeException("Entry already exists for this date and shift");
        }

        // compute server-side day string (e.g., MONDAY)
        String day = request.getDate().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();

        // compute totalPayment on server (single source of truth)
        double totalPayment = request.getMilkQuantity() * request.getFat() * request.getFatPrice();

        MilkEntry entry = MilkEntry.builder()
                .user(user)
                .day(day)
                .date(request.getDate())
                .shift(request.getShift())
                .milkQuantity(request.getMilkQuantity())
                .fat(request.getFat())
                .fatPrice(request.getFatPrice())
                .totalPayment(totalPayment)
                .build();

        MilkEntry saved = milkEntryRepository.save(entry);

        // produce kafka event
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

    // Return sorted entries (ascending by date) for a user
    public List<MilkEntry> getEntriesByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<MilkEntry> entries = milkEntryRepository.findByUserOrderByDateAsc(user);
        // ensure stable ordering also by shift if needed (MORNING then EVENING)
        entries.sort(Comparator.comparing(MilkEntry::getDate).thenComparing(MilkEntry::getShift));
        return entries;
    }



    public List<MilkEntry> getEntries(Long userId, Integer month, Integer year) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // If month & year not provided → return all records sorted
        if (month == null || year == null) {
            return milkEntryRepository.findByUserOrderByDateAsc(user);
        }

        // return filtered month data sorted
        return milkEntryRepository.findByUserAndMonth(user, month, year);
    }

    public List<MilkEntry> getEntriesByUserBetween(Long userId, LocalDate start, LocalDate end) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return milkEntryRepository.findByUserAndDateBetweenOrderByDateAsc(user, start, end);
    }
}
