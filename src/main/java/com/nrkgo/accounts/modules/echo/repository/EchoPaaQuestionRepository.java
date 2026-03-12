package com.nrkgo.accounts.modules.echo.repository;

import com.nrkgo.accounts.modules.echo.model.EchoPaaQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EchoPaaQuestionRepository extends JpaRepository<EchoPaaQuestion, Long> {
    List<EchoPaaQuestion> findBySearchResultId(Long searchId);
}
