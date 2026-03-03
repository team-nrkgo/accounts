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
    private final ObjectMapper objectMapper;

    public SnapGuideServiceImpl(SnapGuideRepository guideRepository, ObjectMapper objectMapper) {
        this.guideRepository = guideRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public SnapGuide saveGuide(SnapGuideDto guideDto, User user) {
        String extId = guideDto.getExternalId();

        // Making it REQUIRED as requested
        if (extId == null || extId.trim().isEmpty()) {
            throw new IllegalArgumentException("The field 'external_id' is required for syncing guides.");
        }

        // Search by the EXTENSION ID (String) to handle updates
        Optional<SnapGuide> existing = guideRepository.findByExternalId(extId);

        SnapGuide guide = existing.orElse(new SnapGuide());
        guide.setExternalId(extId);
        guide.setTitle(guideDto.getTitle() != null ? guideDto.getTitle() : "Untitled Workflow");
        guide.setUser(user);
        guide.setStorageType(guideDto.getStorageType());

        // Hybrid Columns for Performance
        if (guideDto.getSteps() != null) {
            guide.setTotalSteps(guideDto.getSteps().size());
            if (!guideDto.getSteps().isEmpty()) {
                guide.setFirstUrl(guideDto.getSteps().get(0).getUrl());
            }
        }

        // Audit fields from BaseEntity
        long now = System.currentTimeMillis();
        if (existing.isEmpty()) {
            guide.setCreatedBy(user.getId());
            guide.setCreatedTime(now);
        }
        guide.setModifiedBy(user.getId());
        guide.setModifiedTime(now);

        // Serialize steps to JSON
        try {
            String json = objectMapper.writeValueAsString(guideDto.getSteps());
            guide.setStepsJson(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize steps to JSON", e);
        }

        return guideRepository.save(guide);
    }

    @Override
    public List<SnapGuide> getGuidesForUser(User user) {
        return guideRepository.findByUserId(user.getId());
    }

    @Override
    public SnapGuide getGuideById(String id, User user) {
        SnapGuide guide = guideRepository.findByExternalId(id)
                .orElseThrow(() -> new IllegalArgumentException("Guide not found with ID: " + id));

        if (!guide.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized access to guide");
        }
        return guide;
    }

    @Override
    @Transactional
    public void deleteGuide(String id, User user) {
        SnapGuide guide = getGuideById(id, user);
        guideRepository.delete(guide);
    }
}
