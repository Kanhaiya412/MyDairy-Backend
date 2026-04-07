// src/main/java/com/MyFarmerApp/MyFarmer/service/LabourService.java
package com.MyFarmerApp.MyFarmer.service;

import com.MyFarmerApp.MyFarmer.dto.LabourAttendanceRequest;
import com.MyFarmerApp.MyFarmer.dto.LabourRequest;
import com.MyFarmerApp.MyFarmer.dto.LabourSalaryGenerateRequest;
import com.MyFarmerApp.MyFarmer.entity.Labour;
import com.MyFarmerApp.MyFarmer.entity.LabourAttendance;
import com.MyFarmerApp.MyFarmer.entity.LabourSalary;
import com.MyFarmerApp.MyFarmer.entity.User;
import com.MyFarmerApp.MyFarmer.enums.LabourAttendanceStatus;
import com.MyFarmerApp.MyFarmer.enums.LabourPaymentStatus;
import com.MyFarmerApp.MyFarmer.enums.LabourStatus;
import com.MyFarmerApp.MyFarmer.enums.WageType;
import com.MyFarmerApp.MyFarmer.repository.LabourAttendanceRepository;
import com.MyFarmerApp.MyFarmer.repository.LabourRepository;
import com.MyFarmerApp.MyFarmer.repository.LabourSalaryRepository;
import com.MyFarmerApp.MyFarmer.repository.UserRepository;
import com.MyFarmerApp.MyFarmer.util.EventPayload;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@Service
public class LabourService {

    private final LabourRepository labourRepository;
    private final LabourAttendanceRepository attendanceRepository;
    private final LabourSalaryRepository salaryRepository;
    private final UserRepository userRepository;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    public LabourService(
            LabourRepository labourRepository,
            LabourAttendanceRepository attendanceRepository,
            LabourSalaryRepository salaryRepository,
            UserRepository userRepository,
            org.springframework.context.ApplicationEventPublisher eventPublisher
    ) {
        this.labourRepository = labourRepository;
        this.attendanceRepository = attendanceRepository;
        this.salaryRepository = salaryRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    // ─────────────────────────────────────────────────────────────
    // SECURITY HELPER
    // ─────────────────────────────────────────────────────────────
    private Labour getLabourAndVerifyOwnership(Long labourId, Long userId) {
        Labour labour = labourRepository.findById(labourId)
                .orElseThrow(() -> new RuntimeException("Labour not found"));
        if (!labour.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to this labour resource");
        }
        return labour;
    }

    // ─────────────────────────────────────────────────────────────
    // LABOUR CRUD
    // ─────────────────────────────────────────────────────────────

    public Labour addLabour(Long userId, LabourRequest req) {
        if (userId == null) throw new IllegalArgumentException("userId is required");
        if (req.getLabourName() == null || req.getLabourName().isBlank()) {
            throw new IllegalArgumentException("labourName is required");
        }

        // Get Proxy for User (Zero DB Hit if only ID is used)
        User user = userRepository.getReferenceById(userId);

        // WageType
        WageType wageType = WageType.DAILY;
        if (req.getWageType() != null && !req.getWageType().isBlank()) {
            wageType = WageType.valueOf(req.getWageType().toUpperCase());
        }

        Double dailyWage = req.getDailyWage();
        Double monthlySalary = req.getMonthlySalary();
        Double yearlySalary = req.getYearlySalary();
        Integer allowedLeaves = req.getAllowedLeaves();

        // ✅ validate wage
        if (wageType == WageType.DAILY) {
            if (dailyWage == null || dailyWage <= 0) {
                throw new IllegalArgumentException("dailyWage must be positive for DAILY wageType");
            }
        } else if (wageType == WageType.MONTHLY) {
            if (monthlySalary == null || monthlySalary <= 0) {
                throw new IllegalArgumentException("monthlySalary must be positive for MONTHLY wageType");
            }
            if (dailyWage == null || dailyWage <= 0) {
                dailyWage = monthlySalary / 30.0;
            }
        } else if (wageType == WageType.YEARLY) {
            if (yearlySalary == null || yearlySalary <= 0) {
                throw new IllegalArgumentException("yearlySalary must be positive for YEARLY wageType");
            }
            // For yearly, we derive a daily rate strictly for penalty/leave calculations
            if (dailyWage == null || dailyWage <= 0) {
                dailyWage = yearlySalary / 365.0;
            }
        }

        LocalDate joiningDate = null;
        if (req.getJoiningDate() != null && !req.getJoiningDate().isBlank()) {
            joiningDate = LocalDate.parse(req.getJoiningDate());
        }

        Labour labour = Labour.builder()
                .user(user)
                .labourName(req.getLabourName().trim())
                .mobile(req.getMobile())
                .photoUrl(req.getPhotoUrl())
                .wageType(wageType)
                .dailyWage(dailyWage)
                .monthlySalary(monthlySalary)
                .yearlySalary(yearlySalary)
                .allowedLeaves(allowedLeaves != null ? allowedLeaves : 0)
                .address(req.getAddress())
                .notes(req.getNotes())
                .useAttendance(req.getUseAttendance() != null ? req.getUseAttendance() : Boolean.TRUE)
                .referralBy(req.getReferralBy())
                .role(req.getRole() != null ? req.getRole() : "LABOUR")
                .joiningDate(joiningDate)
                .status(req.getStatus() != null ? req.getStatus() : LabourStatus.ACTIVE)
                .createdAt(LocalDate.now())
                .updatedAt(LocalDate.now())
                .build();

        Labour saved = labourRepository.save(labour);

        // ✅ If YEARLY, generate the Master Salary Record for settlement
        if (wageType == WageType.YEARLY) {
            LabourSalary yearlyRecord = LabourSalary.builder()
                    .labour(saved)
                    .month(1) // Anchor to start of year
                    .year(joiningDate != null ? joiningDate.getYear() : LocalDate.now().getYear())
                    .totalSalary(yearlySalary)
                    .amountPaid(0.0)
                    .paymentStatus(LabourPaymentStatus.UNPAID)
                    .generatedDate(LocalDate.now())
                    .createdAt(LocalDate.now())
                    .updatedAt(LocalDate.now())
                    .build();
            salaryRepository.save(yearlyRecord);
        }

        return saved;
    }

    public List<Labour> getLaboursForUser(Long userId) {
        return labourRepository.findByUserIdAndStatusNotOrderByCreatedAtDesc(userId, LabourStatus.INACTIVE);
    }

    @Transactional
    public Labour updateLabour(Long userId, Long id, LabourRequest req) {
        Labour existing = getLabourAndVerifyOwnership(id, userId);

        if (req.getLabourName() != null) existing.setLabourName(req.getLabourName());
        if (req.getMobile() != null) existing.setMobile(req.getMobile());
        if (req.getPhotoUrl() != null) existing.setPhotoUrl(req.getPhotoUrl());
        if (req.getAddress() != null) existing.setAddress(req.getAddress());
        if (req.getNotes() != null) existing.setNotes(req.getNotes());
        if (req.getReferralBy() != null) existing.setReferralBy(req.getReferralBy());
        if (req.getUseAttendance() != null) existing.setUseAttendance(req.getUseAttendance());

        if (req.getWageType() != null && !req.getWageType().isBlank()) {
            existing.setWageType(WageType.valueOf(req.getWageType().toUpperCase()));
        }

        if (req.getDailyWage() != null && req.getDailyWage() > 0) {
            existing.setDailyWage(req.getDailyWage());
        }

        if (req.getMonthlySalary() != null && req.getMonthlySalary() > 0) {
            existing.setMonthlySalary(req.getMonthlySalary());
        }
        
        if (req.getYearlySalary() != null && req.getYearlySalary() > 0) {
            existing.setYearlySalary(req.getYearlySalary());
        }

        if (req.getAllowedLeaves() != null) {
            existing.setAllowedLeaves(req.getAllowedLeaves());
        }

        // Re-derive daily wage if missing/changed type
        if (existing.getWageType() == WageType.MONTHLY && (existing.getDailyWage() == null || existing.getDailyWage() <= 0)) {
            if (existing.getMonthlySalary() != null) existing.setDailyWage(existing.getMonthlySalary() / 30.0);
        } else if (existing.getWageType() == WageType.YEARLY && (existing.getDailyWage() == null || existing.getDailyWage() <= 0)) {
            if (existing.getYearlySalary() != null) existing.setDailyWage(existing.getYearlySalary() / 365.0);
        }

        if (req.getRole() != null) existing.setRole(req.getRole());

        if (req.getJoiningDate() != null && !req.getJoiningDate().isBlank()) {
            existing.setJoiningDate(LocalDate.parse(req.getJoiningDate()));
        }

        if (req.getStatus() != null) {
            // ✅ AUTO-DEACTIVATION TRACKING
            if (req.getStatus() == LabourStatus.INACTIVE && existing.getStatus() == LabourStatus.ACTIVE) {
                existing.setEndDate(LocalDate.now());
            } else if (req.getStatus() == LabourStatus.ACTIVE && existing.getStatus() == LabourStatus.INACTIVE) {
                existing.setEndDate(null); // Reactivated
            }
            existing.setStatus(req.getStatus());
        }

        existing.setUpdatedAt(LocalDate.now());

        return labourRepository.save(existing);
    }

    public void deleteLabour(Long userId, Long id) {
        Labour existing = getLabourAndVerifyOwnership(id, userId);
        // Soft delete mechanism, protecting data history
        existing.setStatus(LabourStatus.INACTIVE);
        existing.setUpdatedAt(LocalDate.now());
        labourRepository.save(existing);
    }

    // ─────────────────────────────────────────────────────────────
    // ATTENDANCE
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public LabourAttendance markAttendance(Long userId, LabourAttendanceRequest req) {

        if (req.getLabourId() == null) {
            throw new IllegalArgumentException("labourId is required");
        }
        if (req.getDate() == null || req.getDate().isBlank()) {
            throw new IllegalArgumentException("date is required (yyyy-MM-dd)");
        }
        if (req.getStatus() == null || req.getStatus().isBlank()) {
            throw new IllegalArgumentException("status is required: PRESENT / ABSENT");
        }

        Labour labour = getLabourAndVerifyOwnership(req.getLabourId(), userId);

        LocalDate date = LocalDate.parse(req.getDate());

        LabourAttendanceStatus status =
                LabourAttendanceStatus.valueOf(req.getStatus().toUpperCase());

        LabourAttendance existing = attendanceRepository
                .findByLabourAndDate(labour, date)
                .orElse(null);

        LabourAttendance attendance = existing != null ? existing : new LabourAttendance();

        attendance.setLabour(labour);
        attendance.setDate(date);
        attendance.setStatus(status);
        attendance.setRemarks(req.getRemarks());

        if (attendance.getCreatedAt() == null) {
            attendance.setCreatedAt(LocalDate.now());
        }
        attendance.setUpdatedAt(LocalDate.now());

        LabourAttendance saved = attendanceRepository.save(attendance);

        // ✅ Publish internal domain event (Async listener handles Kafka)
        eventPublisher.publishEvent(com.MyFarmerApp.MyFarmer.event.LabourEvent.builder()
                .eventType("ATTENDANCE_MARKED")
                .topic(KafkaProducerService.ATTENDANCE_TOPIC)
                .username(labour.getUser().getUsername())
                .role(labour.getUser().getRole().name())
                .success(true)
                .message("Attendance marked")
                .labourId(labour.getId())
                .labourName(labour.getLabourName())
                .dailyWage(labour.getDailyWage())
                .mobile(labour.getMobile())
                .date(attendance.getDate().toString())
                .presentDays(attendance.getStatus() == LabourAttendanceStatus.PRESENT ? 1 : 0)
                .build());

        return saved;
    }

    public List<LabourAttendance> getAttendanceForMonth(Long userId, Long labourId, int month, int year) {
        Labour labour = getLabourAndVerifyOwnership(labourId, userId);

        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        return attendanceRepository.findByLabourAndDateBetween(labour, start, end);
    }

    @Transactional
    public List<LabourAttendance> markBatchAttendance(Long userId, com.MyFarmerApp.MyFarmer.dto.LabourBatchAttendanceRequest req) {
        LocalDate date = LocalDate.parse(req.getDate());
        List<LabourAttendance> results = new java.util.ArrayList<>();

        for (com.MyFarmerApp.MyFarmer.dto.LabourBatchAttendanceRequest.LabourAttendanceEntry entry : req.getEntries()) {
            Labour labour = getLabourAndVerifyOwnership(entry.getLabourId(), userId);
            
            LabourAttendance attendance = attendanceRepository.findByLabourAndDate(labour, date)
                    .orElse(new LabourAttendance());

            attendance.setLabour(labour);
            attendance.setDate(date);
            attendance.setStatus(LabourAttendanceStatus.valueOf(entry.getStatus().toUpperCase()));
            attendance.setRemarks(entry.getRemarks());
            attendance.setShift(entry.getShift() != null ? entry.getShift() : "DAY");
            attendance.setWorkHours(entry.getWorkHours() != null ? entry.getWorkHours() : 8.0);
            
            if (attendance.getCreatedAt() == null) attendance.setCreatedAt(LocalDate.now());
            attendance.setUpdatedAt(LocalDate.now());

            results.add(attendanceRepository.save(attendance));
        }
        return results;
    }

    // ─────────────────────────────────────────────────────────────
    // SALARY GENERATION
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public LabourSalary generateSalary(Long userId, LabourSalaryGenerateRequest req) {

        if (req.getLabourId() == null) {
            throw new IllegalArgumentException("labourId is required");
        }
        if (req.getMonth() == null || req.getYear() == null) {
            throw new IllegalArgumentException("month and year are required");
        }

        Labour labour = getLabourAndVerifyOwnership(req.getLabourId(), userId);

        int month = req.getMonth();
        int year = req.getYear();

        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        int daysInMonth = ym.lengthOfMonth();

        // ✅ ENFORCE CALENDAR BOUNDARIES
        LocalDate joiningDate = labour.getJoiningDate() != null ? labour.getJoiningDate() : labour.getCreatedAt();
        // ✅ SPECIAL CASE: YEARLY workers don't follow monthly calendars
        if (labour.getWageType() == WageType.YEARLY) {
            return salaryRepository.findByLabourIdAndMonthAndYear(labour.getId(), 1, joiningDate.getYear())
                    .orElseGet(() -> {
                        LabourSalary s = LabourSalary.builder()
                                .labour(labour)
                                .month(1)
                                .year(joiningDate.getYear())
                                .totalSalary(labour.getYearlySalary())
                                .amountPaid(0.0)
                                .paymentStatus(LabourPaymentStatus.UNPAID)
                                .generatedDate(LocalDate.now())
                                .createdAt(LocalDate.now())
                                .updatedAt(LocalDate.now())
                                .build();
                        return salaryRepository.save(s);
                    });
        }
        
        if (joiningDate != null && ym.isBefore(YearMonth.from(joiningDate))) {
            throw new IllegalArgumentException("Cannot generate salary for a month before the joining date (" + joiningDate + ").");
        }
        if (labour.getEndDate() != null && ym.isAfter(YearMonth.from(labour.getEndDate()))) {
            throw new IllegalArgumentException("Cannot generate salary for a month after the deactivation date (" + labour.getEndDate() + ").");
        }

        LocalDate calcStart = start;
        if (joiningDate != null && joiningDate.isAfter(start)) {
            calcStart = joiningDate;
        }

        LocalDate calcEnd = end;
        if (labour.getEndDate() != null && labour.getEndDate().isBefore(end)) {
            calcEnd = labour.getEndDate();
        }

        boolean useAttendance = labour.getUseAttendance() == null || labour.getUseAttendance();

        int presentDays = 0;
        Integer manualDays = req.getManualDays();
        boolean fullMonth = Boolean.TRUE.equals(req.getFullMonth());
        double totalSalary = 0.0;
        double baseDailyWage = labour.getDailyWage() != null ? labour.getDailyWage() : 0.0;

        // ✅ AUTO-ATTENDANCE LOGIC
        if (useAttendance) {
            List<LabourAttendance> attendanceList =
                    attendanceRepository.findByLabourAndDateBetween(labour, calcStart, calcEnd);

            if (labour.getWageType() == WageType.MONTHLY) {
                long totalAvailableDays = java.time.temporal.ChronoUnit.DAYS.between(calcStart, calcEnd) + 1;
                if (totalAvailableDays < 0) totalAvailableDays = 0;

                int absentDays = (int) attendanceList.stream()
                        .filter(a -> a.getStatus() == LabourAttendanceStatus.ABSENT)
                        .count();
                double halfDays = attendanceList.stream()
                        .filter(a -> a.getStatus() == LabourAttendanceStatus.HALF_DAY)
                        .count();

                presentDays = (int) (totalAvailableDays - absentDays);
                totalSalary = (totalAvailableDays - absentDays - (halfDays * 0.5)) * baseDailyWage;
                
            } else {
                // DAILY: only counts explicit marks
                double fullDays = attendanceList.stream()
                        .filter(a -> a.getStatus() == LabourAttendanceStatus.PRESENT || 
                                    a.getStatus() == LabourAttendanceStatus.PAID_LEAVE || 
                                    a.getStatus() == LabourAttendanceStatus.OFF_DAY)
                        .count();
                
                double halfDays = attendanceList.stream()
                        .filter(a -> a.getStatus() == LabourAttendanceStatus.HALF_DAY)
                        .count();

                presentDays = (int) (fullDays + (halfDays * 0.5));
                totalSalary = (fullDays + (halfDays * 0.5)) * baseDailyWage;
            }
        }

        // ✅ FIXED LOGIC: useAttendance true => always use presentDays (even 0)
        int effectiveDays;
        if (useAttendance) {
            effectiveDays = presentDays;
            manualDays = null; // ignore manual if attendance is ON
        } else if (manualDays != null && manualDays > 0) {
            effectiveDays = manualDays;
        } else if (fullMonth) {
            effectiveDays = daysInMonth;
            manualDays = daysInMonth;
        } else {
            effectiveDays = 0;
        }

        // ✅ MONTHLY + attendance OFF => fixed salary
        if (!useAttendance && labour.getWageType() == WageType.MONTHLY && labour.getMonthlySalary() != null) {
            totalSalary = labour.getMonthlySalary();
            presentDays = 0;
            manualDays = null;
        } else {
            totalSalary = effectiveDays * baseDailyWage;
        }

        LabourSalary salary = salaryRepository
                .findByLabourAndMonthAndYear(labour, month, year)
                .orElse(LabourSalary.builder().build());

        salary.setLabour(labour);
        salary.setMonth(month);
        salary.setYear(year);
        salary.setPresentDays(useAttendance ? presentDays : null);
        salary.setManualDays(manualDays);
        salary.setTotalSalary(totalSalary);
        salary.setGeneratedDate(LocalDate.now());

        if (salary.getAmountPaid() == null) {
            salary.setAmountPaid(0.0);
        }
        if (salary.getPaymentStatus() == null) {
            salary.setPaymentStatus(LabourPaymentStatus.UNPAID);
        }
        if (salary.getCreatedAt() == null) {
            salary.setCreatedAt(LocalDate.now());
        }
        salary.setUpdatedAt(LocalDate.now());

        LabourSalary savedSalary = salaryRepository.save(salary);

        // ✅ Publish domain event for Async Kafka dispatch
        eventPublisher.publishEvent(com.MyFarmerApp.MyFarmer.event.LabourEvent.builder()
                .eventType("SALARY_GENERATED")
                .topic(KafkaProducerService.SALARY_TOPIC)
                .username(labour.getUser().getUsername())
                .role(labour.getUser().getRole().name())
                .success(true)
                .message("Salary generated")
                .labourId(labour.getId())
                .labourName(labour.getLabourName())
                .dailyWage(labour.getDailyWage())
                .mobile(labour.getMobile())
                .presentDays(savedSalary.getPresentDays())
                .manualDays(savedSalary.getManualDays())
                .amount(savedSalary.getTotalSalary())
                .month(savedSalary.getMonth())
                .year(savedSalary.getYear())
                .build());

        return savedSalary;
    }

    @Transactional
    public void payLumpsumSalary(Long userId, Long labourId, Double amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Labour labour = getLabourAndVerifyOwnership(labourId, userId);
        
        // 1. Fetch pending salaries (Oldest First) - using optimized ID-based query
        List<LabourSalary> pendingSalaries = salaryRepository.findByLabourIdOrderByYearAscMonthAsc(labourId)
                .stream()
                .filter(s -> s.getPaymentStatus() != LabourPaymentStatus.PAID)
                .toList();

        // 2. Auto-generate for missing months if needed
        if (pendingSalaries.isEmpty() && (labour.getWageType() == WageType.MONTHLY || labour.getWageType() == WageType.DAILY)) {
            LocalDate joinDate = labour.getJoiningDate() != null ? labour.getJoiningDate() : labour.getCreatedAt();
            java.time.YearMonth currentYm = java.time.YearMonth.from(joinDate);
            java.time.YearMonth endYm = java.time.YearMonth.now();
            
            while (!currentYm.isAfter(endYm)) {
                if (salaryRepository.findByLabourIdAndMonthAndYear(labourId, currentYm.getMonthValue(), currentYm.getYear()).isEmpty()) {
                    LabourSalaryGenerateRequest autoReq = new LabourSalaryGenerateRequest();
                    autoReq.setLabourId(labourId);
                    autoReq.setMonth(currentYm.getMonthValue());
                    autoReq.setYear(currentYm.getYear());
                    autoReq.setFullMonth(true);
                    this.generateSalary(userId, autoReq);
                }
                currentYm = currentYm.plusMonths(1);
            }
            pendingSalaries = salaryRepository.findByLabourIdOrderByYearAscMonthAsc(labourId)
                    .stream()
                    .filter(s -> s.getPaymentStatus() != LabourPaymentStatus.PAID)
                    .toList();
        }

        if (pendingSalaries.isEmpty()) {
            throw new RuntimeException("No calculated statement found to settle. Please 'Generate Monthly Statement' for this month first.");
        }

        double remainingAmount = amount;
        for (LabourSalary salary : pendingSalaries) {
            if (remainingAmount <= 0) break;
            double currentPaid = (salary.getAmountPaid() != null ? salary.getAmountPaid() : 0.0);
            double dueForThisMonth = salary.getTotalSalary() - currentPaid;
            if (dueForThisMonth <= 0) continue; 
            double paymentForThisMonth = Math.min(remainingAmount, dueForThisMonth);
            double newAmountPaid = currentPaid + paymentForThisMonth;
            salary.setAmountPaid(newAmountPaid);
            if (newAmountPaid >= salary.getTotalSalary()) {
                salary.setPaymentStatus(LabourPaymentStatus.PAID);
                salary.setPaidDate(LocalDate.now());
            } else {
                salary.setPaymentStatus(LabourPaymentStatus.PARTIAL);
            }
            salary.setUpdatedAt(LocalDate.now());
            salaryRepository.save(salary);
            remainingAmount -= paymentForThisMonth;
        }

        // ✅ Async Event dispatch
        eventPublisher.publishEvent(com.MyFarmerApp.MyFarmer.event.LabourEvent.builder()
                .eventType("SALARY_LUMPSUM_PAID")
                .topic(KafkaProducerService.SALARY_TOPIC)
                .username(labour.getUser().getUsername())
                .role(labour.getUser().getRole().name())
                .success(true)
                .message("Lumpsum payment processed")
                .labourId(labour.getId())
                .labourName(labour.getLabourName())
                .amount(amount)
                .build());
    }

    @Transactional
    public LabourSalary markSalaryPaid(Long userId, Long salaryId, Double amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        LabourSalary salary = salaryRepository.findById(salaryId)
                .orElseThrow(() -> new RuntimeException("Salary record not found"));
        
        if (!salary.getLabour().getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized access to this labour resource");
        }

        double currentPaid = (salary.getAmountPaid() != null ? salary.getAmountPaid() : 0.0);
        double newAmountPaid = currentPaid + amount;
        
        if (newAmountPaid > salary.getTotalSalary()) {
            newAmountPaid = salary.getTotalSalary();
        }

        salary.setAmountPaid(newAmountPaid);

        if (newAmountPaid >= salary.getTotalSalary()) {
            salary.setPaymentStatus(LabourPaymentStatus.PAID);
            salary.setPaidDate(LocalDate.now());
        } else {
            salary.setPaymentStatus(LabourPaymentStatus.PARTIAL);
        }
        
        salary.setUpdatedAt(LocalDate.now());
        LabourSalary updated = salaryRepository.save(salary);

        // ✅ Async Event dispatch
        eventPublisher.publishEvent(com.MyFarmerApp.MyFarmer.event.LabourEvent.builder()
                .eventType("SALARY_PAID")
                .topic(KafkaProducerService.SALARY_TOPIC)
                .username(salary.getLabour().getUser().getUsername())
                .role(salary.getLabour().getUser().getRole().name())
                .success(true)
                .message("Salary marked as " + salary.getPaymentStatus().name())
                .labourId(salary.getLabour().getId())
                .labourName(salary.getLabour().getLabourName())
                .dailyWage(salary.getLabour().getDailyWage())
                .mobile(salary.getLabour().getMobile())
                .amount(amount)
                .build());

        return updated;
    }

    public List<LabourSalary> getSalaryHistory(Long userId, Long labourId) {
        Labour labour = getLabourAndVerifyOwnership(labourId, userId);
        return salaryRepository.findByLabourOrderByYearDescMonthDesc(labour);
    }

    public Labour getLabourById(Long userId, Long id) {
        return getLabourAndVerifyOwnership(id, userId);
    }

    // ─────────────────────────────────────────────────────────────
    // PHOTO UPLOAD
    // ─────────────────────────────────────────────────────────────
    public String uploadPhoto(Long userId, MultipartFile file) {
        userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        
        try {
            String uploadDir = "uploads/labour/";
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            
            // ✅ Use Files.copy instead of transferTo for safe absolute/relative path stream copying
            java.nio.file.Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            return "/uploads/labour/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not store the photo. Error: " + e.getMessage());
        }
    }
}
