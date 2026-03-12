package com.nrkgo.accounts.modules.echo.model;

import com.nrkgo.accounts.model.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "echo_search_results")
public class EchoSearchResult extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "org_id", nullable = false)
    private Long orgId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 500)
    private String keyword;

    @Column(name = "source_url", columnDefinition = "TEXT")
    private String sourceUrl;

    @Column(length = 50)
    private String location;

    @Column(name = "total_questions")
    private Integer totalQuestions = 0;

    @OneToMany(mappedBy = "searchResult", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<EchoPaaQuestion> questions = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public List<EchoPaaQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<EchoPaaQuestion> questions) {
        this.questions = questions;
    }

    public void addQuestion(EchoPaaQuestion question) {
        questions.add(question);
        question.setSearchResult(this);
    }

    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("created_at_formatted")
    public String getCreatedAtFormatted() {
        if (getCreatedTime() == null)
            return null;
        String timeStr = String.valueOf(getCreatedTime());
        if (timeStr.length() == 14) {
            return timeStr.substring(0, 4) + "-" +
                    timeStr.substring(4, 6) + "-" +
                    timeStr.substring(6, 8) + " " +
                    timeStr.substring(8, 10) + ":" +
                    timeStr.substring(10, 12) + ":" +
                    timeStr.substring(12, 14);
        }
        return timeStr;
    }
}
