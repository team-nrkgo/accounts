package com.nrkgo.accounts.repository;

import com.nrkgo.accounts.model.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    List<Organization> findByCreatedBy(Long createdBy);
}
