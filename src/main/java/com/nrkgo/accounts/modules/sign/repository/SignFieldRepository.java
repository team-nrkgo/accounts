package com.nrkgo.accounts.modules.sign.repository;

import com.nrkgo.accounts.modules.sign.model.SignField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SignFieldRepository extends JpaRepository<SignField, Long> {
    List<SignField> findByEnvelopeId(Long envelopeId);
    List<SignField> findByRecipientId(Long recipientId);
}
