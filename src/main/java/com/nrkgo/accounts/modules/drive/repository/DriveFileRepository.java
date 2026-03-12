package com.nrkgo.accounts.modules.drive.repository;

import com.nrkgo.accounts.modules.drive.model.DriveFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DriveFileRepository extends JpaRepository<DriveFile, Long> {
    Optional<DriveFile> findByExternalId(String externalId);
}
