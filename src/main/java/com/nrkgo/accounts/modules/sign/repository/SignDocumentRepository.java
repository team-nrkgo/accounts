package com.nrkgo.accounts.modules.sign.repository;

import com.nrkgo.accounts.modules.sign.model.SignDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SignDocumentRepository extends JpaRepository<SignDocument, Long> {
    List<SignDocument> findByEnvelopeIdOrderByOrderIndexAsc(Long envelopeId);
}
