package com.MyFarmerApp.MyFarmer.ai;

import com.MyFarmerApp.MyFarmer.entity.MilkEntry;
import com.MyFarmerApp.MyFarmer.enums.AiIntent;
import com.MyFarmerApp.MyFarmer.service.MilkEntryService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class AiMilkQueryService {

    private final MilkEntryService milkEntryService;

    public AiMilkQueryService(MilkEntryService milkEntryService) {
        this.milkEntryService = milkEntryService;
    }

    public String resolve(AiIntent intent, Long userId, Integer month) {

        return switch (intent) {

            case TODAY_MILK -> todayMilk(userId);

            case LAST_7_DAYS_MILK -> last7DaysMilk(userId);

            case MONTHLY_MILK -> monthlyMilk(userId);

            default -> "I couldn't understand your request yet. Please ask about milk data.";
        };
    }

    // ---------------- TODAY ----------------
    private String todayMilk(Long userId) {

        LocalDate today = LocalDate.now();

        List<MilkEntry> entries =
                milkEntryService.getEntriesByUserBetween(userId, today, today);

        if (entries.isEmpty()) {
            return "No milk entry found for today.";
        }

        double totalMilk = entries.stream()
                .mapToDouble(MilkEntry::getMilkQuantity)
                .sum();

        double totalAmount = entries.stream()
                .mapToDouble(e -> e.getTotalPayment() != null ? e.getTotalPayment() : 0)
                .sum();

        return "Today's milk production is "
                + totalMilk + " liters. Total amount: ₹" + totalAmount;
    }

    // ---------------- LAST 7 DAYS ----------------
    private String last7DaysMilk(Long userId) {

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);

        List<MilkEntry> entries =
                milkEntryService.getEntriesByUserBetween(userId, start, end);

        if (entries.isEmpty()) {
            return "No milk records found for the last 7 days.";
        }

        double totalMilk = entries.stream()
                .mapToDouble(MilkEntry::getMilkQuantity)
                .sum();

        return "Milk produced in the last 7 days is "
                + totalMilk + " liters.";
    }

    // ---------------- MONTHLY ----------------
    private String monthlyMilk(Long userId) {

        YearMonth currentMonth = YearMonth.now();

        List<MilkEntry> entries =
                milkEntryService.getEntriesByUserBetween(
                        userId,
                        currentMonth.atDay(1),
                        currentMonth.atEndOfMonth()
                );

        if (entries.isEmpty()) {
            return "No milk records found for this month.";
        }

        double totalMilk = entries.stream()
                .mapToDouble(MilkEntry::getMilkQuantity)
                .sum();

        double totalAmount = entries.stream()
                .mapToDouble(e -> e.getTotalPayment() != null ? e.getTotalPayment() : 0)
                .sum();

        return "This month's total milk is "
                + totalMilk + " liters with earnings ₹" + totalAmount;
    }
}
