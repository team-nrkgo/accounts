package com.nrkgo.accounts.modules.plans.repository;

import com.nrkgo.accounts.modules.plans.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByProductCodeAndStatus(Integer productCode, Integer status);

    List<Plan> findByProductCode(Integer productCode);

    // Used by AdminPlanController to find a specific plan by name
    Optional<Plan> findByProductCodeAndPlanNameAndStatus(Integer productCode, String planName, Integer status);
}
