package com.nrkgo.accounts.modules.plans.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nrkgo.accounts.modules.plans.repository.PlanRepository;
import com.nrkgo.accounts.modules.plans.repository.SubscriptionRepository;
import org.springframework.stereotype.Component;

/**
 * Factory that returns the correct PlanService implementation for a given
 * product code.
 *
 * HOW TO ADD A NEW PRODUCT:
 * 1. Add a new constant in ProductCodes.java
 * 2. Create MyProductPlanService extends DefaultPlanService
 * 3. Add a case in the switch below
 */
@Component
public class PlanServiceFactory {

    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;

    public PlanServiceFactory(PlanRepository planRepository,
            SubscriptionRepository subscriptionRepository,
            ObjectMapper objectMapper) {
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.objectMapper = objectMapper;
    }

    public PlanService getInstance(int productCode) {
        switch (productCode) {
            case ProductCodes.SNAP_STEPS: {
                DefaultPlanService svc = new DefaultPlanService(
                        planRepository, subscriptionRepository, objectMapper);
                svc.productCode = ProductCodes.SNAP_STEPS;
                return svc;
            }
            case ProductCodes.ECHO: {
                DefaultPlanService svc = new DefaultPlanService(
                        planRepository, subscriptionRepository, objectMapper);
                svc.productCode = ProductCodes.ECHO;
                return svc;
            }

            default:
                throw new IllegalArgumentException("Unknown product code: " + productCode);
        }
    }
}
