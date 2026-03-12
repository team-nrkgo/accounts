package com.nrkgo.accounts.modules.snapsteps.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nrkgo.accounts.modules.snapsteps.dto.SnapGuideDto;
import com.nrkgo.accounts.modules.snapsteps.dto.SnapStepDto;
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

        if (guideDto.getIsStarred() != null) {
            guide.setIsStarred(guideDto.getIsStarred() ? 1 : 0);
        }

        long now = System.currentTimeMillis();
        if (existing.isEmpty()) {
            guide.setCreatedBy(user.getId());
            guide.setCreatedTime(now);
        }

        if (guideDto.getIsStarred() != null) {
            guide.setIsStarred(guideDto.getIsStarred() ? 1 : 0);
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
    public List<SnapGuide> getStarredGuides(User user, Long orgId) {
        return guideRepository.findByUserIdAndOrgIdAndIsStarred(user.getId(), orgId, 1);
    }

    @Override
    public List<SnapGuide> searchGuides(User user, Long orgId, String query) {
        return guideRepository.findByUserIdAndOrgIdAndTitleContainingIgnoreCase(user.getId(), orgId, query);
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
    public SnapGuide updateGuide(Long id, SnapGuideDto guideDto, User user, Long orgId) {
        SnapGuide guide = getGuideByNumericId(id, user, orgId);

        if (guideDto.getTitle() != null) {
            guide.setTitle(guideDto.getTitle());
        }
        if (guideDto.getStorageType() != null) {
            guide.setStorageType(guideDto.getStorageType());
        }

        if (guideDto.getSteps() != null) {
            guide.setTotalSteps(guideDto.getSteps().size());
            if (!guideDto.getSteps().isEmpty()) {
                guide.setFirstUrl(guideDto.getSteps().get(0).getUrl());
            }
            try {
                String json = objectMapper.writeValueAsString(guideDto.getSteps());
                guide.setStepsJson(json);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize steps to JSON", e);
            }
        }

        if (guideDto.getIsStarred() != null) {
            guide.setIsStarred(guideDto.getIsStarred() ? 1 : 0);
        }

        guide.setModifiedBy(user.getId());
        guide.setModifiedTime(System.currentTimeMillis());

        return guideRepository.save(guide);
    }

    @Override
    @Transactional
    public void deleteGuide(String id, User user, Long orgId) {
        SnapGuide guide = getGuideById(id, user, orgId);
        guideRepository.delete(guide);
    }

    @Override
    @Transactional
    public void deleteGuideByNumericId(Long id, User user, Long orgId) {
        SnapGuide guide = getGuideByNumericId(id, user, orgId);
        guideRepository.delete(guide);
    }

    @Override
    public byte[] exportGuide(Long id, String format, User user, Long orgId) {
        SnapGuide guide = getGuideByNumericId(id, user, orgId);

        // TODO: PLAN CHECK - Check orgId's active plan here for "Premium" formats
        // if (format.equalsIgnoreCase("HTML") && !planService.hasFeature(orgId,
        // "EXPORT_HTML")) { ... }

        List<SnapStepDto> steps;
        try {
            steps = objectMapper.readValue(guide.getStepsJson(),
                    new com.fasterxml.jackson.core.type.TypeReference<List<SnapStepDto>>() {
                    });
        } catch (Exception e) {
            throw new RuntimeException("Failed to decode guide steps", e);
        }

        String content;
        String upperFormat = format.toUpperCase();

        switch (upperFormat) {
            case "MARKDOWN":
                content = generateMarkdown(guide, steps);
                return content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            case "HTML":
                content = generateHtml(guide, steps);
                return content.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            case "PDF":
                return generatePdf(guide, steps);
            default:
                throw new IllegalArgumentException("Unsupported export format: " + format);
        }
    }

    private byte[] generatePdf(SnapGuide guide, List<SnapStepDto> steps) {
        String html = generateHtml(guide, steps);
        try (java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream()) {
            com.openhtmltopdf.pdfboxout.PdfRendererBuilder builder = new com.openhtmltopdf.pdfboxout.PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private String generateMarkdown(SnapGuide guide, List<SnapStepDto> steps) {
        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(guide.getTitle()).append("\n\n");
        sb.append("> Generated with SnapSteps\n\n");

        for (int i = 0; i < steps.size(); i++) {
            SnapStepDto step = steps.get(i);
            sb.append("## Step ").append(i + 1).append("\n");
            sb.append(step.getDescription() != null ? step.getDescription() : "No description").append("\n\n");
            if (step.getScreenshot() != null) {
                sb.append("![Step ").append(i + 1).append("](").append(step.getScreenshot()).append(")\n\n");
            }
            if (step.getUrl() != null) {
                sb.append("**URL**: [").append(step.getUrl()).append("](").append(step.getUrl()).append(")\n\n");
            }
            sb.append("---\n\n");
        }
        return sb.toString();
    }

    private String generateHtml(SnapGuide guide, List<SnapStepDto> steps) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<!DOCTYPE html PUBLIC \"-//OPENHTMLTOPDF//DOC XHTML Character Entities Only 1.0//EN\" \"\">");
        sb.append("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
        sb.append("<head><title>").append(guide.getTitle()).append("</title>");
        sb.append("<style>");
        sb.append(
                "body { font-family: Arial, sans-serif; max-width: 800px; margin: 40px auto; padding: 20px; color: #333; }");
        sb.append("h1 { color: #000; border-bottom: 2px solid #333; padding-bottom: 10px; }");
        sb.append(".step { border-bottom: 1px solid #eee; margin-bottom: 30px; padding-bottom: 20px; }");
        sb.append("h2 { color: #444; margin-top: 20px; }");
        sb.append("p { line-height: 1.6; }");
        sb.append(
                "img { max-width: 100%; border: 1px solid #ddd; border-radius: 8px; display: block; margin-top: 15px; }");
        sb.append(
                "footer { font-size: 12px; color: #888; text-align: center; margin-top: 50px; border-top: 1px solid #eee; padding-top: 10px; }");
        sb.append("</style></head><body>");
        sb.append("<h1>").append(guide.getTitle()).append("</h1>");

        for (int i = 0; i < steps.size(); i++) {
            SnapStepDto step = steps.get(i);
            sb.append("<div class=\"step\">");
            sb.append("<h2>Step ").append(i + 1).append("</h2>");
            sb.append("<p>").append(step.getDescription() != null ? step.getDescription() : "No description")
                    .append("</p>");
            if (step.getScreenshot() != null && !step.getScreenshot().isEmpty()) {
                sb.append("<img src=\"").append(step.getScreenshot()).append("\" alt=\"Step ").append(i + 1)
                        .append("\" />");
            }
            sb.append("</div>");
        }
        sb.append("<footer>Generated by SnapSteps</footer></body></html>");
        return sb.toString();
    }

    @Override
    public org.springframework.data.domain.Page<SnapGuide> getStarredGuides(User user, Long orgId,
            org.springframework.data.domain.Pageable pageable) {
        return guideRepository.findByUserIdAndOrgIdAndIsStarred(user.getId(), orgId, 1, pageable);
    }
}
