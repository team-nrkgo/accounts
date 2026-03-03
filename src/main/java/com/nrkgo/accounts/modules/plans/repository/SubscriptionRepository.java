package com.nrkgo.accounts.modules.plans.repository;

import com.nrkgo.accounts.modules.plans.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    // Get the current active subscription for an org on a specific product
    Optional<Subscription> findFirstByOrgIdAndProductCodeAndStatusOrderByCreatedTimeDesc(
            Long orgId, Integer productCode, Integer status);

    // Full subscription history for an org on a specific product (admin/audit use)
    List<Subscription> findByOrgIdAndProductCodeOrderByCreatedTimeDesc(
            Long orgId, Integer productCode);

    // All subscriptions for an org across all products
    List<Subscription> findByOrgIdOrderByCreatedTimeDesc(Long orgId);
}
