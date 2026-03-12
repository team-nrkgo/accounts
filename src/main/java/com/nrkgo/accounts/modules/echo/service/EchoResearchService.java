package com.nrkgo.accounts.modules.echo.service;

import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.modules.echo.dto.EchoResearchCaptureDto;
import com.nrkgo.accounts.modules.echo.dto.EchoResearchDetailDto;
import com.nrkgo.accounts.modules.echo.model.EchoPaaQuestion;
import com.nrkgo.accounts.modules.echo.model.EchoSearchResult;
import com.nrkgo.accounts.modules.echo.repository.EchoSearchResultRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;

@Service
public class EchoResearchService {

    private final EchoSearchResultRepository searchRepository;

    public EchoResearchService(EchoSearchResultRepository searchRepository) {
        this.searchRepository = searchRepository;
    }

    @Transactional
    public EchoSearchResult captureResearch(EchoResearchCaptureDto dto, User user) {
        EchoSearchResult search = new EchoSearchResult();
        search.setOrgId(dto.getOrgId());
        search.setUserId(user.getId());
        search.setCreatedBy(user.getId());
        search.setModifiedBy(user.getId());
        search.setKeyword(dto.getKeyword());
        search.setLocation(dto.getLocation());
        search.setSourceUrl(dto.getSourceUrl());
        search.setTotalQuestions(dto.getQuestions() != null ? dto.getQuestions().size() : 0);

        if (dto.getQuestions() != null) {
            for (EchoResearchCaptureDto.PaaQuestionDto qDto : dto.getQuestions()) {
                EchoPaaQuestion q = new EchoPaaQuestion();
                q.setQuestion(qDto.getText());
                q.setAnswerUrl(qDto.getLink());
                q.setPosition(qDto.getRank());
                q.setExtraDataJson(qDto.getExtraData());
                q.setDomain(extractDomain(qDto.getLink()));
                search.addQuestion(q);
            }
        }

        return searchRepository.save(search);
    }

    public Page<EchoSearchResult> listSearchResults(Long orgId, Pageable pageable) {
        return searchRepository.findByOrgId(orgId, pageable);
    }

    @Transactional(readOnly = true)
    public EchoResearchDetailDto getSearchResultDetails(Long id, Long orgId) {
        EchoSearchResult search = searchRepository.findById(id)
                .filter(s -> s.getOrgId().equals(orgId))
                .orElseThrow(() -> new RuntimeException("Search result not found or access denied"));

        // Explicitly load questions before session closes
        search.getQuestions().size();

        return new EchoResearchDetailDto(search, search.getQuestions());
    }

    private String extractDomain(String url) {
        if (url == null || url.isEmpty())
            return null;
        try {
            // Google URLs often have the destination URL as a q parameter
            // Handle both direct and redirect URLs
            String destinationUrl = url;
            if (url.contains("url?q=")) {
                String sub = url.substring(url.indexOf("url?q=") + 6);
                if (sub.contains("&")) {
                    destinationUrl = sub.substring(0, sub.indexOf("&"));
                } else {
                    destinationUrl = sub;
                }
            }
            URI uri = new URI(destinationUrl);
            String domain = uri.getHost();
            return domain != null && domain.startsWith("www.") ? domain.substring(4) : domain;
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
