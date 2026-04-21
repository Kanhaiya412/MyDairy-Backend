package com.MyFarmerApp.MyFarmer.ai.nlu;

import com.MyFarmerApp.MyFarmer.ai.memory.SessionContext;
import com.MyFarmerApp.MyFarmer.ai.models.IntentType;
import com.MyFarmerApp.MyFarmer.ai.models.NluResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ContextResolver {

    /**
     * Resolves the incoming query against the existing session context.
     * Returns a merged SessionContext representing the final state to execute.
     */
    public SessionContext resolveContext(Long userId, NluResult incomingNlu, SessionContext existingContext) {
        
        // If system flags need clarification, return null. The orchestrator will handle it.
        if (Boolean.TRUE.equals(incomingNlu.getNeedsClarification())) {
            return null;
        }

        // Case 1: Fresh Context (User has no active session or explicitly changed topic)
        if (existingContext == null || !Boolean.TRUE.equals(incomingNlu.getIsFollowUp())) {
            log.info("Creating fresh context for user {}", userId);
            return SessionContext.builder()
                    .userId(userId)
                    .lastIntent(incomingNlu.getIntent())
                    .startDate(incomingNlu.getStartDate())
                    .endDate(incomingNlu.getEndDate())
                    .lastMonth(incomingNlu.getMonth())
                    .lastYear(incomingNlu.getYear())
                    .isDayWise(incomingNlu.getIsDayWise())
                    .targetEntity(incomingNlu.getTargetEntity())
                    .comparisonTarget(incomingNlu.getComparisonTarget())
                    .verificationReference(incomingNlu.getVerificationReference())
                    .build();
        }

        // Case 2: Follow-up Merging
        log.info("Merging follow-up query into existing context for user {}", userId);
        
        // The LLM indicated this is a follow up. We keep the old context, but overwrite ANY slots the new NLU provided.
        if (incomingNlu.getIntent() != null && incomingNlu.getIntent() != IntentType.UNKNOWN_UNSUPPORTED) {
            existingContext.setLastIntent(incomingNlu.getIntent());
        }
        if (incomingNlu.getStartDate() != null) {
            existingContext.setStartDate(incomingNlu.getStartDate());
        }
        if (incomingNlu.getEndDate() != null) {
            existingContext.setEndDate(incomingNlu.getEndDate());
        }
        if (incomingNlu.getMonth() != null) {
            existingContext.setLastMonth(incomingNlu.getMonth());
        }
        if (incomingNlu.getYear() != null) {
            existingContext.setLastYear(incomingNlu.getYear());
        }
        if (incomingNlu.getIsDayWise() != null) {
            existingContext.setIsDayWise(incomingNlu.getIsDayWise());
        }
        if (incomingNlu.getTargetEntity() != null) {
            existingContext.setTargetEntity(incomingNlu.getTargetEntity());
        }
        if (incomingNlu.getComparisonTarget() != null) {
            existingContext.setComparisonTarget(incomingNlu.getComparisonTarget());
        }
        if (incomingNlu.getVerificationReference() != null) {
            existingContext.setVerificationReference(incomingNlu.getVerificationReference());
        }

        return existingContext;
    }
}
