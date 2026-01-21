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

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class LabourService {

    private final LabourRepository labourRepository;
    private final LabourAttendanceRepository attendanceRepository;
    private final LabourSalaryRepository salaryRepository;
    private final UserRepository userRepository;
    private final KafkaProducerService kafkaProducerService;

    public LabourService(LabourRepository labourRepository,
                         LabourAttendanceRepository attendanceRepository,
                         LabourSalaryRepository salaryRepository,
                         UserRepository userRepository,
                         KafkaProducerService kafkaProducerService) {
        this.labourRepository = labourRepository;
        this.attendanceRepository = attendanceRepository;
        this.salaryRepository = salaryRepository;
        this.userRepository = userRepository;
        this.kafkaProducerService = kafkaProducerService;
    }

    // ─────────────────────────────────────────────────────────────
    // LABOUR CRUD
    // ─────────────────────────────────────────────────────────────

    public Labour addLabour(User user, LabourRequest req) {

        if (req.getLabourName() == null || req.getLabourName().isBlank()) {
            throw new IllegalArgumentException("labourName is required");
        }

        // WageType
        WageType wageType = WageType.DAILY;
        if (req.getWageType() != null) {
            wageType = WageType.valueOf(req.getWageType().toUpperCase());
        }

        Double dailyWage = req.getDailyWage();
        Double monthlySalary = req.getMonthlySalary();

        if (wageType == WageType.DAILY) {
            if (dailyWage == null || dailyWage <= 0) {
                throw new IllegalArgumentException("dailyWage must be positive for DAILY wageType");
            }
        } else { // MONTHLY
            if (monthlySalary == null || monthlySalary <= 0) {
                throw new IllegalArgumentException("monthlySalary must be positive for MONTHLY wageType");
            }
            // derive effective daily wage if not set
            if (dailyWage == null || dailyWage <= 0) {
                dailyWage = monthlySalary / 30.0;  // simple approximation
            }
        }

        LocalDate joiningDate = null;
        if (req.getJoiningDate() != null && !req.getJoiningDate().isBlank()) {
            joiningDate = LocalDate.parse(req.getJoiningDate()); // expects yyyy-MM-dd
        }

        Labour labour = Labour.builder()
                .user(user)
                .labourName(req.getLabourName().trim())
                .mobile(req.getMobile())
                .wageType(wageType)
                .dailyWage(dailyWage)
                .monthlySalary(monthlySalary)
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

        return labourRepository.save(labour);
    }

    public List<Labour> getLaboursForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return labourRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public Labour updateLabour(Long id, LabourRequest req) {
        Labour existing = labourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Labour not found"));

        if (req.getLabourName() != null) existing.setLabourName(req.getLabourName());
        if (req.getMobile() != null) existing.setMobile(req.getMobile());
        if (req.getAddress() != null) existing.setAddress(req.getAddress());
        if (req.getNotes() != null) existing.setNotes(req.getNotes());
        if (req.getReferralBy() != null) existing.setReferralBy(req.getReferralBy());
        if (req.getUseAttendance() != null) existing.setUseAttendance(req.getUseAttendance());

        if (req.getWageType() != null) {
            existing.setWageType(WageType.valueOf(req.getWageType().toUpperCase()));
        }
        if (req.getDailyWage() != null && req.getDailyWage() > 0) {
            existing.setDailyWage(req.getDailyWage());
        }
        if (req.getMonthlySalary() != null && req.getMonthlySalary() > 0) {
            existing.setMonthlySalary(req.getMonthlySalary());
            // If wageType is MONTHLY and no explicit dailyWage, derive again
            if (existing.getWageType() == WageType.MONTHLY &&
                    (existing.getDailyWage() == null || existing.getDailyWage() <= 0)) {
                existing.setDailyWage(req.getMonthlySalary() / 30.0);
            }
        }

        if (req.getRole() != null) existing.setRole(req.getRole());

        if (req.getJoiningDate() != null && !req.getJoiningDate().isBlank()) {
            existing.setJoiningDate(LocalDate.parse(req.getJoiningDate()));
        }
        if (req.getStatus() != null) existing.setStatus(req.getStatus());

        existing.setUpdatedAt(LocalDate.now());

        return labourRepository.save(existing);
    }

    public void deleteLabour(Long id) {
        Labour existing = labourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Labour not found"));
        labourRepository.delete(existing);
    }

    // ─────────────────────────────────────────────────────────────
    // ATTENDANCE
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public LabourAttendance markAttendance(LabourAttendanceRequest req) {
        if (req.getLabourId() == null) {
            throw new IllegalArgumentException("labourId is required");
        }
        if (req.getDate() == null || req.getDate().isBlank()) {
            throw new IllegalArgumentException("date is required (yyyy-MM-dd)");
        }
        if (req.getStatus() == null) {
            throw new IllegalArgumentException("status is required: PRESENT / ABSENT");
        }

        Labour labour = labourRepository.findById(req.getLabourId())
                .orElseThrow(() -> new RuntimeException("Labour not found"));

        LocalDate date = LocalDate.parse(req.getDate()); // expects yyyy-MM-dd

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

        String payload = EventPayload.labourJson(
                "ATTENDANCE_MARKED",
                labour.getUser().getUsername(),
                labour.getUser().getRole().name(),
                true,
                "Attendance marked",
                labour.getId(),
                labour.getLabourName(),
                labour.getDailyWage(),
                labour.getMobile(),
                labour.getJoiningDate() != null ? labour.getJoiningDate().toString() : null,
                attendance.getStatus() == LabourAttendanceStatus.PRESENT ? 1 : 0,
                null,
                null,
                null,
                null
        );

        kafkaProducerService.sendToTopic(KafkaProducerService.ATTENDANCE_TOPIC, payload);

        return saved;
    }

    public List<LabourAttendance> getAttendanceForMonth(Long labourId, int month, int year) {
        Labour labour = labourRepository.findById(labourId)
                .orElseThrow(() -> new RuntimeException("Labour not found"));

        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        return attendanceRepository.findByLabourAndDateBetween(labour, start, end);
    }

    // ─────────────────────────────────────────────────────────────
    // SALARY GENERATION (using attendance + manual + full month)
    // ─────────────────────────────────────────────────────────────

    @Transactional
    public LabourSalary generateSalary(LabourSalaryGenerateRequest req) {
        if (req.getLabourId() == null) {
            throw new IllegalArgumentException("labourId is required");
        }
        if (req.getMonth() == null || req.getYear() == null) {
            throw new IllegalArgumentException("month and year are required");
        }

        Labour labour = labourRepository.findById(req.getLabourId())
                .orElseThrow(() -> new RuntimeException("Labour not found"));

        int month = req.getMonth();
        int year = req.getYear();

        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        int daysInMonth = ym.lengthOfMonth();

        boolean useAttendance = labour.getUseAttendance() == null || labour.getUseAttendance();

        int presentDays = 0;
        Integer manualDays = req.getManualDays();
        boolean fullMonth = Boolean.TRUE.equals(req.getFullMonth());

        if (useAttendance) {
            List<LabourAttendance> attendanceList =
                    attendanceRepository.findByLabourAndDateBetween(labour, start, end);

            long presentCount = attendanceList.stream()
                    .filter(a -> a.getStatus() == LabourAttendanceStatus.PRESENT)
                    .count();

            presentDays = (int) presentCount;
        }

        int effectiveDays;
        if (useAttendance && presentDays > 0) {
            effectiveDays = presentDays;
            manualDays = null;
        } else if (manualDays != null && manualDays > 0) {
            effectiveDays = manualDays;
        } else if (fullMonth) {
            effectiveDays = daysInMonth;
            manualDays = daysInMonth;
        } else {
            effectiveDays = 0;
        }

        double totalSalary;

        // DAILY or MONTHLY -> always use dailyWage as base rate
        double baseDailyWage = labour.getDailyWage() != null ? labour.getDailyWage() : 0.0;

        if (!useAttendance && labour.getWageType() == WageType.MONTHLY && labour.getMonthlySalary() != null) {
            // fixed monthly salary, no attendance
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
        salary.setPresentDays(useAttendance && presentDays > 0 ? presentDays : null);
        salary.setManualDays(manualDays);
        salary.setTotalSalary(totalSalary);
        salary.setGeneratedDate(LocalDate.now());
        if (salary.getPaymentStatus() == null) {
            salary.setPaymentStatus(LabourPaymentStatus.UNPAID);
        }
        if (salary.getCreatedAt() == null) {
            salary.setCreatedAt(LocalDate.now());
        }
        salary.setUpdatedAt(LocalDate.now());

        LabourSalary savedSalary = salaryRepository.save(salary);

        String payload = EventPayload.labourJson(
                "SALARY_GENERATED",
                labour.getUser().getUsername(),
                labour.getUser().getRole().name(),
                true,
                "Salary generated",
                labour.getId(),
                labour.getLabourName(),
                labour.getDailyWage(),
                labour.getMobile(),
                labour.getJoiningDate() != null ? labour.getJoiningDate().toString() : null,
                savedSalary.getPresentDays(),
                savedSalary.getManualDays(),
                savedSalary.getTotalSalary(),
                savedSalary.getMonth(),
                savedSalary.getYear()
        );

        kafkaProducerService.sendToTopic(KafkaProducerService.SALARY_TOPIC, payload);

        return savedSalary;
    }

    public List<LabourSalary> getSalaryHistory(Long labourId) {
        Labour labour = labourRepository.findById(labourId)
                .orElseThrow(() -> new RuntimeException("Labour not found"));

        return salaryRepository.findByLabourOrderByYearDescMonthDesc(labour);
    }

    public Labour getLabourById(Long id) {
        return labourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Labour not found"));
    }
        @Transactional
    public LabourSalary markSalaryPaid(Long salaryId) {
        LabourSalary salary = salaryRepository.findById(salaryId)
                .orElseThrow(() -> new RuntimeException("Salary record not found"));

        salary.setPaymentStatus(LabourPaymentStatus.PAID);
        salary.setPaidDate(LocalDate.now());
        salary.setUpdatedAt(LocalDate.now());
        LabourSalary updated = salaryRepository.save(salary);

        String payload = EventPayload.labourJson(
                "SALARY_PAID",
                salary.getLabour().getUser().getUsername(),
                salary.getLabour().getUser().getRole().name(),
                true,
                "Salary marked as PAID",
                salary.getLabour().getId(),
                salary.getLabour().getLabourName(),
                salary.getLabour().getDailyWage(),
                salary.getLabour().getMobile(),
                salary.getLabour().getJoiningDate() != null ? salary.getLabour().getJoiningDate().toString() : null,
                salary.getPresentDays(),
                salary.getManualDays(),
                salary.getTotalSalary(),
                salary.getMonth(),
                salary.getYear()
        );

        kafkaProducerService.sendToTopic(KafkaProducerService.SALARY_TOPIC, payload);

        return updated;
    }
}
