package com.nrkgo.accounts.repository;

import com.nrkgo.accounts.model.Digest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DigestRepository extends JpaRepository<Digest, Long> {
    
    Optional<Digest> findByToken(String token);

    Optional<Digest> findByEntityIdAndEntityType(String entityId, String entityType);
    
    void deleteByToken(String token);
}
