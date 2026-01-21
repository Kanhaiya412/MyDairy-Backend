package com.MyFarmerApp.MyFarmer.service;

import com.MyFarmerApp.MyFarmer.dto.AnimalManagementRequest;
import com.MyFarmerApp.MyFarmer.dto.AnimalManagementResponse;
import com.MyFarmerApp.MyFarmer.entity.AnimalManagement;
import com.MyFarmerApp.MyFarmer.entity.CattleEntry;
import com.MyFarmerApp.MyFarmer.enums.AnimalStatus;
import com.MyFarmerApp.MyFarmer.repository.AnimalManagementRepository;
import com.MyFarmerApp.MyFarmer.repository.CattleEntryRepository;
import com.MyFarmerApp.MyFarmer.service.AnimalManagementService;
import com.MyFarmerApp.MyFarmer.service.KafkaProducerService;
import com.MyFarmerApp.MyFarmer.util.EventPayload;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnimalManagementServiceImpl implements AnimalManagementService {

    private final AnimalManagementRepository animalRepo;
    private final CattleEntryRepository cattleRepo;
    private final KafkaProducerService kafkaProducerService;

    public static final String ANIMAL_TOPIC = "animal-events";

    @Override
    public AnimalManagementResponse addRecord(AnimalManagementRequest request) {

        CattleEntry cattle = cattleRepo.findById(request.getCattleId())
                .orElseThrow(() -> new RuntimeException("Cattle not found"));

        AnimalStatus status = request.getStatus() != null
                ? request.getStatus()
                : AnimalStatus.ACTIVE;

        AnimalManagement entry = AnimalManagement.builder()
                .cattleEntry(cattle)
                .animalColor(request.getAnimalColor())
                .birthDate(request.getBirthDate())
                .age(request.getAge())
                .healthStatus(request.getHealthStatus())
                .lastCheckupDate(request.getLastCheckupDate())
                .nextCheckupDate(request.getNextCheckupDate())
                .lastVaccinationDate(request.getLastVaccinationDate())
                .nextVaccinationDate(request.getNextVaccinationDate())
                .lastHeatDate(request.getLastHeatDate())
                .lastBullMeetDate(request.getLastBullMeetDate())
                .lastAIDate(request.getLastAIDate())
                .avgMilkProduction(request.getAvgMilkProduction())
                .remarks(request.getRemarks())
                .status(status)
                .build();

        AnimalManagement saved = animalRepo.save(entry);

        sendKafkaEvent(saved, "ANIMAL_RECORD_ADDED", "Animal management record added");

        return convert(saved);
    }

    @Override
    public AnimalManagementResponse updateRecord(Long id, AnimalManagementRequest request) {

        AnimalManagement existing = animalRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        if (request.getAnimalColor() != null) existing.setAnimalColor(request.getAnimalColor());
        if (request.getBirthDate() != null) existing.setBirthDate(request.getBirthDate());
        if (request.getAge() != null) existing.setAge(request.getAge());
        if (request.getHealthStatus() != null) existing.setHealthStatus(request.getHealthStatus());
        if (request.getLastCheckupDate() != null) existing.setLastCheckupDate(request.getLastCheckupDate());
        if (request.getNextCheckupDate() != null) existing.setNextCheckupDate(request.getNextCheckupDate());
        if (request.getLastVaccinationDate() != null) existing.setLastVaccinationDate(request.getLastVaccinationDate());
        if (request.getNextVaccinationDate() != null) existing.setNextVaccinationDate(request.getNextVaccinationDate());
        if (request.getLastHeatDate() != null) existing.setLastHeatDate(request.getLastHeatDate());
        if (request.getLastBullMeetDate() != null) existing.setLastBullMeetDate(request.getLastBullMeetDate());
        if (request.getLastAIDate() != null) existing.setLastAIDate(request.getLastAIDate());
        if (request.getAvgMilkProduction() != null) existing.setAvgMilkProduction(request.getAvgMilkProduction());
        if (request.getRemarks() != null) existing.setRemarks(request.getRemarks());
        if (request.getStatus() != null) existing.setStatus(request.getStatus());

        existing.setUpdatedAt(LocalDate.now());

        AnimalManagement updated = animalRepo.save(existing);

        sendKafkaEvent(updated, "ANIMAL_RECORD_UPDATED", "Animal management record updated");

        return convert(updated);
    }

    @Override
    public void deleteRecord(Long id) {
        AnimalManagement existing = animalRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        animalRepo.delete(existing);

        sendKafkaEvent(existing, "ANIMAL_RECORD_DELETED", "Animal management record deleted");
    }

    // ===== Kafka Event Sender =====

    private void sendKafkaEvent(AnimalManagement entry, String eventType, String message) {
        try {
            String payload = EventPayload.animalJson(
                    eventType,
                    entry.getCattleEntry().getUser().getUsername(),
                    entry.getCattleEntry().getUser().getRole().name(),
                    true,
                    message,
                    entry.getCattleEntry().getCattleId(),
                    entry.getCattleEntry().getCattleName(),
                    entry.getHealthStatus() != null ? entry.getHealthStatus().name() : "NA",
                    entry.getLastCheckupDate() != null ? entry.getLastCheckupDate().toString() : "NA",
                    entry.getNextCheckupDate() != null ? entry.getNextCheckupDate().toString() : "NA",
                    entry.getLastHeatDate() != null ? entry.getLastHeatDate().toString() : "NA",
                    entry.getLastAIDate() != null ? entry.getLastAIDate().toString() : "NA"
            );

            kafkaProducerService.sendToTopic(ANIMAL_TOPIC, payload);

        } catch (Exception e) {
            System.err.println("Kafka send failed: " + e.getMessage());
        }
    }

    // ===== Convert to Response DTO =====

    private AnimalManagementResponse convert(AnimalManagement a) {

        AnimalManagementResponse r = new AnimalManagementResponse();

        r.setId(a.getId());
        r.setCattleId(a.getCattleEntry().getId());
        r.setCattleName(a.getCattleEntry().getCattleName());
        r.setCattleCode(a.getCattleEntry().getCattleId());

        r.setAnimalColor(a.getAnimalColor());
        r.setBirthDate(a.getBirthDate());
        r.setAge(a.getAge());
        r.setHealthStatus(a.getHealthStatus());
        r.setLastCheckupDate(a.getLastCheckupDate());
        r.setNextCheckupDate(a.getNextCheckupDate());
        r.setLastVaccinationDate(a.getLastVaccinationDate());
        r.setNextVaccinationDate(a.getNextVaccinationDate());
        r.setLastHeatDate(a.getLastHeatDate());
        r.setLastBullMeetDate(a.getLastBullMeetDate());
        r.setLastAIDate(a.getLastAIDate());
        r.setAvgMilkProduction(a.getAvgMilkProduction());
        r.setRemarks(a.getRemarks());
        r.setStatus(a.getStatus());
        r.setCreatedAt(a.getCreatedAt());
        r.setUpdatedAt(a.getUpdatedAt());

        return r;
    }

    @Override
    public List<AnimalManagementResponse> getHistoryByCattle(Long cattleId) {
        CattleEntry cattle = cattleRepo.findById(cattleId)
                .orElseThrow(() -> new RuntimeException("Cattle not found"));

        return animalRepo.findByCattleEntryOrderByCreatedAtDesc(cattle)
                .stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public AnimalManagementResponse getLatestRecord(Long cattleId) {

        CattleEntry cattle = cattleRepo.findById(cattleId)
                .orElseThrow(() -> new RuntimeException("Cattle not found"));

        AnimalManagement latest = animalRepo.findFirstByCattleEntryOrderByCreatedAtDesc(cattle);

        if (latest == null) throw new RuntimeException("No record found");

        return convert(latest);
    }
}
