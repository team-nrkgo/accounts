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

    long countByOrgIdAndRoleId(Long orgId, Long roleId);

    @org.springframework.data.jpa.repository.Query("SELECT new com.nrkgo.accounts.dto.OrgMemberResponse(ou.id, u.email, r.name, ou.designation, u.firstName, u.lastName, ou.status, ou.createdTime, ou.roleId, d.token) " +
           "FROM OrgUser ou JOIN User u ON ou.userId = u.id JOIN Role r ON ou.roleId = r.id " +
           "LEFT JOIN Digest d ON d.entityId = CAST(ou.id as string) AND d.entityType = 'INVITE' " +
           "WHERE ou.orgId = :orgId")
    List<com.nrkgo.accounts.dto.OrgMemberResponse> findMembersByOrgId(@org.springframework.data.repository.query.Param("orgId") Long orgId);

    @org.springframework.data.jpa.repository.Query("SELECT new com.nrkgo.accounts.dto.OrgMemberResponse(ou.id, u.email, r.name, ou.designation, u.firstName, u.lastName, ou.status, ou.createdTime, ou.roleId, d.token) " +
           "FROM OrgUser ou JOIN User u ON ou.userId = u.id JOIN Role r ON ou.roleId = r.id " +
           "LEFT JOIN Digest d ON d.entityId = CAST(ou.id as string) AND d.entityType = 'INVITE' " +
           "WHERE ou.orgId = :orgId AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<com.nrkgo.accounts.dto.OrgMemberResponse> findMembersByOrgIdAndSearch(@org.springframework.data.repository.query.Param("orgId") Long orgId, @org.springframework.data.repository.query.Param("search") String search);
}
