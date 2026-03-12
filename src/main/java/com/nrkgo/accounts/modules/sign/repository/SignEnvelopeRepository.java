package com.nrkgo.accounts.modules.sign.repository;

import com.nrkgo.accounts.modules.sign.model.SignEnvelope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SignEnvelopeRepository extends JpaRepository<SignEnvelope, Long> {
    Optional<SignEnvelope> findByTrackingId(String trackingId);
}
