package com.nrkgo.accounts.repository;

import com.nrkgo.accounts.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    @Query("SELECT r FROM Role r WHERE r.orgId IS NULL OR r.orgId = :orgId")
    List<Role> findAllGlobalAndOrgRoles(@Param("orgId") Long orgId);
    
    boolean existsByNameAndOrgId(String name, Long orgId);
}
