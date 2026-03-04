package com.nrkgo.accounts.modules.snapsteps.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nrkgo.accounts.modules.snapsteps.dto.SnapGuideDto;
import com.nrkgo.accounts.modules.snapsteps.model.SnapGuide;
import com.nrkgo.accounts.modules.snapsteps.repository.SnapGuideRepository;
import com.nrkgo.accounts.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SnapGuideServiceImpl implements SnapGuideService {

    private final SnapGuideRepository guideRepository;
    private final com.nrkgo.accounts.repository.OrganizationRepository organizationRepository;
    private final ObjectMapper objectMapper;

    public SnapGuideServiceImpl(SnapGuideRepository guideRepository,
            com.nrkgo.accounts.repository.OrganizationRepository organizationRepository,
            ObjectMapper objectMapper) {
        this.guideRepository = guideRepository;
        this.organizationRepository = organizationRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public SnapGuide saveGuide(SnapGuideDto guideDto, User user, Long orgId) {
        String extId = guideDto.getExternalId();

        if (extId == null || extId.trim().isEmpty()) {
            throw new IllegalArgumentException("The field 'external_id' is required for syncing guides.");
        }

        com.nrkgo.accounts.model.Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new IllegalArgumentException("Organization not found with ID: " + orgId));

        Optional<SnapGuide> existing = guideRepository.findByExternalId(extId);

        SnapGuide guide = existing.orElse(new SnapGuide());
        guide.setExternalId(extId);
        guide.setTitle(guideDto.getTitle() != null ? guideDto.getTitle() : "Untitled Workflow");
        guide.setUser(user);
        guide.setOrg(org);
        guide.setStorageType(guideDto.getStorageType());

        if (guideDto.getSteps() != null) {
            guide.setTotalSteps(guideDto.getSteps().size());
            if (!guideDto.getSteps().isEmpty()) {
                guide.setFirstUrl(guideDto.getSteps().get(0).getUrl());
            }
        }

        long now = System.currentTimeMillis();
        if (existing.isEmpty()) {
            guide.setCreatedBy(user.getId());
            guide.setCreatedTime(now);
        }
        guide.setModifiedBy(user.getId());
        guide.setModifiedTime(now);

        try {
            String json = objectMapper.writeValueAsString(guideDto.getSteps());
            guide.setStepsJson(json);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize steps to JSON", e);
        }

        return guideRepository.save(guide);
    }

    @Override
    public List<SnapGuide> getGuidesForUser(User user, Long orgId) {
        return guideRepository.findByUserIdAndOrgId(user.getId(), orgId);
    }

    @Override
    public org.springframework.data.domain.Page<SnapGuide> getGuidesForUser(User user, Long orgId,
            org.springframework.data.domain.Pageable pageable) {
        return guideRepository.findByUserIdAndOrgId(user.getId(), orgId, pageable);
    }

    @Override
    public org.springframework.data.domain.Page<SnapGuide> searchGuides(User user, Long orgId, String query,
            org.springframework.data.domain.Pageable pageable) {
        return guideRepository.findByUserIdAndOrgIdAndTitleContainingIgnoreCase(user.getId(), orgId, query, pageable);
    }

    @Override
    public SnapGuide getGuideById(String id, User user, Long orgId) {
        SnapGuide guide = guideRepository.findByExternalId(id)
                .orElseThrow(() -> new IllegalArgumentException("Guide not found with ID: " + id));

        if (!guide.getUser().getId().equals(user.getId()) || !guide.getOrg().getId().equals(orgId)) {
            throw new SecurityException("Unauthorized access to guide");
        }
        return guide;
    }

    @Override
    public SnapGuide getGuideByNumericId(Long id, User user, Long orgId) {
        SnapGuide guide = guideRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Guide not found with ID: " + id));

        if (!guide.getUser().getId().equals(user.getId()) || !guide.getOrg().getId().equals(orgId)) {
            throw new SecurityException("Unauthorized access to guide");
        }
        return guide;
    }

    @Override
    @Transactional
    public void deleteGuide(String id, User user, Long orgId) {
        SnapGuide guide = getGuideById(id, user, orgId);
        guideRepository.delete(guide);
    }
}
