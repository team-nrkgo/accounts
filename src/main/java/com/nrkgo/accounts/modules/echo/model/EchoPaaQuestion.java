package com.nrkgo.accounts.modules.echo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "echo_paa_questions")
public class EchoPaaQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "search_id", nullable = false)
    @JsonIgnore
    private EchoSearchResult searchResult;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String question;

    @Column(name = "answer_url", columnDefinition = "TEXT")
    private String answerUrl;

    @Column(length = 255)
    private String domain;

    private Integer position;

    @Column(name = "extra_data_json", columnDefinition = "TEXT")
    private String extraDataJson;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EchoSearchResult getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(EchoSearchResult searchResult) {
        this.searchResult = searchResult;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswerUrl() {
        return answerUrl;
    }

    public void setAnswerUrl(String answerUrl) {
        this.answerUrl = answerUrl;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public String getExtraDataJson() {
        return extraDataJson;
    }

    public void setExtraDataJson(String extraDataJson) {
        this.extraDataJson = extraDataJson;
    }
}
