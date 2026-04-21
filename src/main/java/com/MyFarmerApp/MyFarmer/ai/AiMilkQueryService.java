package com.MyFarmerApp.MyFarmer.ai;

import com.MyFarmerApp.MyFarmer.entity.MilkEntry;
import com.MyFarmerApp.MyFarmer.service.MilkEntryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AiMilkQueryService {

    private final MilkEntryService milkEntryService;

    public AiMilkQueryService(MilkEntryService milkEntryService) {
        this.milkEntryService = milkEntryService;
    }

    public String get_milk_by_date(Long userId, String dateStr) {
        log.info("Fetching milk for date: {} for user: {}", dateStr, userId);
        try {
            LocalDate date = LocalDate.parse(dateStr);
            List<MilkEntry> entries =
                    milkEntryService.getEntriesByUserBetween(userId, date, date);

            if (entries.isEmpty()) {
                return "No milk records found for " + dateStr + ".";
            }

            return formatMilkSummary(entries, "Milk report for " + dateStr);
        } catch (Exception e) {
            log.error("Failed to parse date: {}", dateStr, e);
            return "Invalid date format. Please use YYYY-MM-DD.";
        }
    }

    public String get_today_milk(Long userId) {
        return get_milk_by_date(userId, LocalDate.now().toString());
    }

    public String get_last_7_days_milk(Long userId) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(6);
        return get_milk_report(userId, start.toString(), end.toString(), false);
    }

    public String get_monthly_milk(Long userId, Integer month) {
        log.info("Fetching monthly milk for user: {}, month: {}", userId, month);
        
        YearMonth targetMonth;
        if (month != null && month >= 1 && month <= 12) {
            targetMonth = YearMonth.of(LocalDate.now().getYear(), month);
        } else {
            targetMonth = YearMonth.now();
        }

        return get_milk_report(userId, 
                targetMonth.atDay(1).toString(), 
                targetMonth.atEndOfMonth().toString(), 
                false);
    }

    public String get_milk_report(Long userId, String startDateStr, String endDateStr, boolean dayWise) {
        log.info("Fetching milk report from {} to {} (dayWise: {}) for user: {}", 
                startDateStr, endDateStr, dayWise, userId);
        try {
            LocalDate start = LocalDate.parse(startDateStr);
            LocalDate end = LocalDate.parse(endDateStr);

            List<MilkEntry> entries = milkEntryService.getEntriesByUserBetween(userId, start, end);

            if (entries.isEmpty()) {
                return String.format("No milk records found between %s and %s.", startDateStr, endDateStr);
            }

            if (!dayWise) {
                return formatMilkSummary(entries, String.format("Summary from %s to %s", startDateStr, endDateStr));
            }

            // Day-wise breakdown
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Day-wise Milk Report (%s to %s):\n", startDateStr, endDateStr));
            
            entries.stream()
                    .collect(Collectors.groupingBy(MilkEntry::getDate, TreeMap::new, Collectors.toList()))
                    .forEach((date, dailyEntries) -> {
                        double dailyQty = dailyEntries.stream().mapToDouble(MilkEntry::getMilkQuantity).sum();
                        double dailyAmt = dailyEntries.stream().mapToDouble(e -> e.getTotalPayment() != null ? e.getTotalPayment() : 0).sum();
                        sb.append(String.format("- %s: %.2fL | ₹%.2f\n", date, dailyQty, dailyAmt));
                    });

            double totalMilk = entries.stream().mapToDouble(MilkEntry::getMilkQuantity).sum();
            double totalAmount = entries.stream().mapToDouble(e -> e.getTotalPayment() != null ? e.getTotalPayment() : 0).sum();
            sb.append(String.format("\nTOTAL: %.2fL | ₹%.2f", totalMilk, totalAmount));

            return sb.toString();

        } catch (Exception e) {
            log.error("Report execution failed", e);
            return "Unable to generate milk report.";
        }
    }

    private String formatMilkSummary(List<MilkEntry> entries, String title) {
        double totalMilk = entries.stream()
                .mapToDouble(MilkEntry::getMilkQuantity)
                .sum();

        double totalAmount = entries.stream()
                .mapToDouble(e -> e.getTotalPayment() != null ? e.getTotalPayment() : 0)
                .sum();

        return String.format("%s: %.2f liters. Total amount: ₹%.2f", title, totalMilk, totalAmount);
    }
}
