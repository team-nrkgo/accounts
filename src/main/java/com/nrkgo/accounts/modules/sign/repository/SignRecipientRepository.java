package com.nrkgo.accounts.modules.sign.repository;

import com.nrkgo.accounts.modules.sign.model.SignRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SignRecipientRepository extends JpaRepository<SignRecipient, Long> {
    List<SignRecipient> findByEnvelopeIdOrderBySigningOrderAsc(Long envelopeId);
    Optional<SignRecipient> findByAccessToken(String accessToken);
}
