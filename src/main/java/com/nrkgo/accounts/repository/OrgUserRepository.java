package com.nrkgo.accounts.repository;

import com.nrkgo.accounts.model.OrgUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrgUserRepository extends JpaRepository<OrgUser, Long> {

    List<OrgUser> findByUserId(Long userId);
    
    List<OrgUser> findByOrgId(Long orgId);
    
    Optional<OrgUser> findByOrgIdAndUserId(Long orgId, Long userId);

    boolean existsByOrgIdAndUserId(Long orgId, Long userId);
}
