package com.nrkgo.accounts.modules.echo.dto;

import com.nrkgo.accounts.modules.echo.model.EchoPaaQuestion;
import com.nrkgo.accounts.modules.echo.model.EchoSearchResult;
import java.util.List;

public class EchoResearchDetailDto {
    private EchoSearchResult search;
    private List<EchoPaaQuestion> questions;

    public EchoResearchDetailDto(EchoSearchResult search, List<EchoPaaQuestion> questions) {
        this.search = search;
        this.questions = questions;
    }

    public EchoSearchResult getSearch() {
        return search;
    }

    public void setSearch(EchoSearchResult search) {
        this.search = search;
    }

    public List<EchoPaaQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<EchoPaaQuestion> questions) {
        this.questions = questions;
    }
}
