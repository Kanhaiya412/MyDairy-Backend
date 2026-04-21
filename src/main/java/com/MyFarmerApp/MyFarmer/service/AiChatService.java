package com.MyFarmerApp.MyFarmer.service;

import com.MyFarmerApp.MyFarmer.ai.AiMilkQueryService;
import com.MyFarmerApp.MyFarmer.ai.response.ResponseOrchestrator;
import com.MyFarmerApp.MyFarmer.ai.memory.ContextStore;
import com.MyFarmerApp.MyFarmer.ai.memory.SessionContext;
import com.MyFarmerApp.MyFarmer.ai.models.NluResult;
import com.MyFarmerApp.MyFarmer.ai.nlu.ContextResolver;
import com.MyFarmerApp.MyFarmer.ai.nlu.NluEngine;
import com.MyFarmerApp.MyFarmer.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiChatService {

    private final AiMilkQueryService milkQueryService;
    private final UserRepository userRepository;
    
    // Core AI Engines
    private final NluEngine nluEngine;
    private final ResponseOrchestrator responseOrchestrator;
    private final ContextStore contextStore;
    private final ContextResolver contextResolver;

    public AiChatService(
            AiMilkQueryService milkQueryService,
            UserRepository userRepository,
            NluEngine nluEngine,
            ResponseOrchestrator responseOrchestrator,
            ContextStore contextStore,
            ContextResolver contextResolver
    ) {
        this.milkQueryService = milkQueryService;
        this.userRepository = userRepository;
        this.nluEngine = nluEngine;
        this.responseOrchestrator = responseOrchestrator;
        this.contextStore = contextStore;
        this.contextResolver = contextResolver;
    }

    public String askGroq(String userMessage) {
        log.info("AI Chat Request (Original): {}", userMessage);
        Long userId = extractUserIdFromSecurity();
        
        try {
            SessionContext activeContext = contextStore.getContext(userId);
            NluResult nluResult = nluEngine.parseIntentAndEntities(userMessage, activeContext);
            log.info("Parsed NLU Intent: {} (FollowUp: {})", nluResult.getIntent(), nluResult.getIsFollowUp());

            if (Boolean.TRUE.equals(nluResult.getNeedsClarification())) {
                return nluResult.getClarificationMessage() != null ? 
                       nluResult.getClarificationMessage() : 
                       "Aap exactly kiski detail chahte hain? Thoda clear batayein.";
            }

            SessionContext resolvedContext = contextResolver.resolveContext(userId, nluResult, activeContext);
            if (resolvedContext == null) {
                return "Main samajh nahi paya. Kripya apna sawal naye sire se puchein.";
            }

            contextStore.saveContext(resolvedContext);

            return switch (resolvedContext.getLastIntent()) {
                case TODAY_MILK, DATE_REPORT, MONTHLY_REPORT, YEARLY_REPORT, CUSTOM_RANGE_REPORT, DAY_WISE_REPORT, COMPARE_PREVIOUS, ANALYTICS_QUERY -> 
                    handleMilkProductionQuery(userId, userMessage, resolvedContext, nluResult.getDetectedLanguage());
                case CATTLE_REPORT -> 
                    handleCattleQuery(userId, userMessage, resolvedContext, nluResult.getDetectedLanguage());
                case PAYMENT_REPORT, EMPLOYEE_REPORT -> 
                    handleLabourQuery(userId, userMessage, resolvedContext, nluResult.getDetectedLanguage());
                case GLOBAL_AI_QUERY -> 
                    responseOrchestrator.orchestrateGlobal(userMessage);
                
                case VERIFY_PREVIOUS_RESPONSE, CONFIRMATION_QUERY -> 
                    "Ji, maine apne system records ke mutabik wahi jankari di hai jo save ki gayi hai.";
                case DETAILS_REQUEST, FOLLOW_UP_QUERY -> 
                    handleMilkProductionQuery(userId, userMessage, resolvedContext, nluResult.getDetectedLanguage());
                
                case UNKNOWN, UNKNOWN_UNSUPPORTED -> 
                    "Main sirf dairy farm account (milk records, cattle health, staff reports) mein help kar sakta hu. Please apna sawaal specifically puchein.";
                default -> 
                    "Main is account query ko properly samajh nahi paya. Kripya naya sawaal puchein.";
            };

        } catch (Exception e) {
            log.error("Enterprise AI Chat engine failure", e);
            return "System mein abhi thodi issue aayi hai. Please kuch minutes baad try karein.";
        }
    }

    private String handleMilkProductionQuery(Long userId, String originalQuery, SessionContext context, String language) {
        String dbData = null;
        boolean isDayWise = Boolean.TRUE.equals(context.getIsDayWise());
        
        if (context.getStartDate() == null && context.getLastYear() != null) {
            int year = context.getLastYear();
            if (context.getLastMonth() != null) {
                int month = context.getLastMonth();
                context.setStartDate(java.time.LocalDate.of(year, month, 1));
                context.setEndDate(java.time.LocalDate.of(year, month, java.time.YearMonth.of(year, month).lengthOfMonth()));
            } else {
                context.setStartDate(java.time.LocalDate.of(year, 1, 1));
                context.setEndDate(java.time.LocalDate.of(year, 12, 31));
            }
        }
        
        if (context.getStartDate() != null && context.getEndDate() != null) {
            log.info("Fetching DB records from {} to {} (DayWise: {})", context.getStartDate(), context.getEndDate(), isDayWise);
            dbData = milkQueryService.get_milk_report(userId,
                    context.getStartDate().toString(),
                    context.getEndDate().toString(),
                    isDayWise);
        }
        
        // Pass to Orchestrator (it handles null/missing logic securely)
        return responseOrchestrator.orchestrateResponse(
            context.getLastIntent().name(), 
            dbData, 
            originalQuery, 
            language
        );
    }
    
    private String handleCattleQuery(Long userId, String originalQuery, SessionContext context, String language) {
        return responseOrchestrator.orchestrateResponse("CATTLE_REPORT", null, originalQuery, language);
    }

    private String handleLabourQuery(Long userId, String originalQuery, SessionContext context, String language) {
        return responseOrchestrator.orchestrateResponse("EMPLOYEE_REPORT", null, originalQuery, language);
    }

    private Long extractUserIdFromSecurity() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null
                    && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getName())) {

                return userRepository.findByUsername(auth.getName())
                        .map(user -> user.getId())
                        .orElse(1L);
            }

            return 1L;

        } catch (Exception e) {
            log.warn("Failed to extract userId, using default user", e);
            return 1L;
        }
    }
}
