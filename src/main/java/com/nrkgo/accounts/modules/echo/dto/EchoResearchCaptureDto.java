package com.nrkgo.accounts.modules.echo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class EchoResearchCaptureDto {

    @JsonProperty("org_id")
    private Long orgId;

    private String keyword;
    private String location;

    @JsonProperty("source_url")
    private String sourceUrl;

    private List<PaaQuestionDto> questions;

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public List<PaaQuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<PaaQuestionDto> questions) {
        this.questions = questions;
    }

    public static class PaaQuestionDto {
        private String text;
        private String link;
        private Integer rank;
        private String extraData; // JSON string

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public Integer getRank() {
            return rank;
        }

        public void setRank(Integer rank) {
            this.rank = rank;
        }

        public String getExtraData() {
            return extraData;
        }

        public void setExtraData(String extraData) {
            this.extraData = extraData;
        }
    }
}
