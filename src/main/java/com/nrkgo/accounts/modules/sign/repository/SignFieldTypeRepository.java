package com.nrkgo.accounts.modules.sign.repository;

import com.nrkgo.accounts.modules.sign.model.SignFieldType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SignFieldTypeRepository extends JpaRepository<SignFieldType, Long> {
    List<SignFieldType> findByStatus(Integer status);
}
