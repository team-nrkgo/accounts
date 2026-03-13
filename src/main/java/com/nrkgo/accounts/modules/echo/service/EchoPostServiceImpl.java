package com.nrkgo.accounts.modules.echo.service;

import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.modules.echo.dto.EchoPostDto;
import com.nrkgo.accounts.modules.echo.model.EchoPost;
import com.nrkgo.accounts.modules.echo.model.EchoTag;
import com.nrkgo.accounts.modules.echo.repository.EchoPostRepository;
import com.nrkgo.accounts.modules.echo.repository.EchoTagRepository;
import com.nrkgo.accounts.modules.echo.integration.EchoIntegrationService;
import com.nrkgo.accounts.modules.scheduler.service.SchedulerService;
import com.nrkgo.accounts.modules.drive.service.DriveStorageService;
import com.nrkgo.accounts.modules.drive.model.DriveFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EchoPostServiceImpl implements EchoPostService {

    private final EchoPostRepository repository;
    private final EchoTagRepository tagRepository;
    private final EchoIntegrationService integrationService;
    private final SchedulerService schedulerService;
    private final DriveStorageService driveStorageService;

    public EchoPostServiceImpl(EchoPostRepository repository, EchoTagRepository tagRepository,
            EchoIntegrationService integrationService,
            SchedulerService schedulerService,
            DriveStorageService driveStorageService) {
        this.repository = repository;
        this.tagRepository = tagRepository;
        this.integrationService = integrationService;
        this.schedulerService = schedulerService;
        this.driveStorageService = driveStorageService;
    }

    @Override
    @Transactional
    public EchoPostDto createPost(EchoPostDto dto, User user, Long orgId) {
        EchoPost post = new EchoPost();
        post.setUserId(user.getId());
        post.setOrgId(orgId);
        post.setTitle(dto.getTitle());
        String baseSlug = null;
        if (dto.getSlug() != null && !dto.getSlug().trim().isEmpty()) {
            baseSlug = formatSlug(dto.getSlug());
        } else if (dto.getTitle() != null) {
            baseSlug = formatSlug(dto.getTitle());
        }
        if (baseSlug != null) {
            post.setSlug(generateUniqueSlug(orgId, baseSlug, null));
        }
        post.setCategoryId(dto.getCategoryId());
        post.setFeaturedImageUrl(dto.getFeaturedImageUrl());
        post.setContentJson(dto.getContentJson());
        post.setMetadataJson(dto.getMetadataJson());
        post.setStatus(dto.getStatus() != null ? dto.getStatus() : "draft");
        post.setScheduledTime(dto.getScheduledTime());

        if (dto.getTags() != null && !dto.getTags().isEmpty()) {
            post.setTags(resolveTags(orgId, dto.getTags()));
        }

        EchoPost savedPost = repository.save(post);
        EchoPostDto savedDto = convertToDto(savedPost);

        if ("published".equalsIgnoreCase(savedPost.getStatus())) {
            integrationService.handlePostPublished(savedDto);
        } else if ("scheduled".equalsIgnoreCase(savedPost.getStatus()) && savedPost.getScheduledTime() != null) {
            schedulerService.scheduleSingle("EchoPublishJob", savedPost.getId(), savedPost.getScheduledTime(), orgId,
                    user.getId(), 2, "{\"postId\":" + savedPost.getId() + "}", savedPost.getSlug());
        }

        return savedDto;
    }

    @Override
    @Transactional
    public EchoPostDto updatePost(Long id, EchoPostDto dto, User user, Long orgId) {
        EchoPost post = repository.findByIdAndUserIdAndOrgId(id, user.getId(), orgId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found or access denied"));

        if (dto.getTitle() != null)
            post.setTitle(dto.getTitle());
        String baseSlug = null;
        if (dto.getSlug() != null && !dto.getSlug().trim().isEmpty()) {
            baseSlug = formatSlug(dto.getSlug());
        } else if (dto.getSlug() != null && dto.getSlug().trim().isEmpty() && dto.getTitle() != null) {
            baseSlug = formatSlug(dto.getTitle());
        }
        if (baseSlug != null) {
            post.setSlug(generateUniqueSlug(orgId, baseSlug, post.getId()));
        }
        if (dto.getCategoryId() != null)
            post.setCategoryId(dto.getCategoryId());
        if (dto.getFeaturedImageUrl() != null)
            post.setFeaturedImageUrl(dto.getFeaturedImageUrl());
        if (dto.getContentJson() != null)
            post.setContentJson(dto.getContentJson());
        if (dto.getMetadataJson() != null)
            post.setMetadataJson(dto.getMetadataJson());
        if (dto.getStatus() != null)
            post.setStatus(dto.getStatus());
        if (dto.getScheduledTime() != null)
            post.setScheduledTime(dto.getScheduledTime());

        if (dto.getTags() != null) {
            post.setTags(resolveTags(orgId, dto.getTags()));
        }

        EchoPost savedPost = repository.save(post);
        EchoPostDto savedDto = convertToDto(savedPost);

        // Cancel any existing pending schedules for this post
        schedulerService.cancelJobByEntityId("EchoPublishJob", savedPost.getId(), orgId, 2, user.getId());

        if ("published".equalsIgnoreCase(savedPost.getStatus())) {
            integrationService.handlePostPublished(savedDto);
        } else if ("scheduled".equalsIgnoreCase(savedPost.getStatus()) && savedPost.getScheduledTime() != null) {
            schedulerService.scheduleSingle("EchoPublishJob", savedPost.getId(), savedPost.getScheduledTime(), orgId,
                    user.getId(), 2, "{\"postId\":" + savedPost.getId() + "}", savedPost.getSlug());
        }

        return savedDto;
    }

    private String formatSlug(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        return input.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    private String generateUniqueSlug(Long orgId, String baseSlug, Long excludeId) {
        String slug = baseSlug;
        List<String> existingSlugs = repository.findSlugsByOrgIdAndBaseSlug(orgId, baseSlug);

        if (excludeId != null) {
            EchoPost post = repository.findById(excludeId).orElse(null);
            if (post != null && post.getSlug() != null) {
                existingSlugs.remove(post.getSlug());
            }
        }

        if (!existingSlugs.contains(baseSlug)) {
            return baseSlug;
        }

        int maxSuffix = 0;
        for (String existing : existingSlugs) {
            if (existing.startsWith(baseSlug + "-")) {
                try {
                    String suffixStr = existing.substring(baseSlug.length() + 1);
                    int suffix = Integer.parseInt(suffixStr);
                    if (suffix > maxSuffix) {
                        maxSuffix = suffix;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }

        return baseSlug + "-" + (maxSuffix + 1);
    }

    private Set<EchoTag> resolveTags(Long orgId, List<String> tagNames) {
        Set<EchoTag> resolvedTags = new HashSet<>();
        for (String name : tagNames) {
            String slug = formatSlug(name);
            EchoTag tag = tagRepository.findByOrgIdAndSlug(orgId, slug)
                    .orElseGet(() -> {
                        EchoTag newTag = new EchoTag();
                        newTag.setOrgId(orgId);
                        newTag.setName(name);
                        newTag.setSlug(slug);
                        return tagRepository.save(newTag);
                    });
            resolvedTags.add(tag);
        }
        return resolvedTags;
    }

    @Override
    @Transactional(readOnly = true)
    public EchoPostDto getPostById(Long id, User user, Long orgId) {
        EchoPost post = repository.findByIdAndUserIdAndOrgId(id, user.getId(), orgId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found or access denied"));
        return convertToDto(post);
    }

    @Override
    @Transactional(readOnly = true)
    public EchoPostDto getPostByExternalId(String externalId) {
        EchoPost post = repository.findByExternalId(externalId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        return convertToDto(post);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EchoPostDto> listPosts(Long orgId, User user, String status, String search, Pageable pageable) {
        Page<EchoPost> posts;
        if (search != null && !search.trim().isEmpty()) {
            posts = repository.findByUserIdAndOrgIdAndTitleContainingIgnoreCase(user.getId(), orgId, search, pageable);
        } else if (status != null && !status.trim().isEmpty()) {
            posts = repository.findByUserIdAndOrgIdAndStatus(user.getId(), orgId, status, pageable);
        } else {
            posts = repository.findByUserIdAndOrgId(user.getId(), orgId, pageable);
        }
        return posts.map(this::convertToDto);
    }

    @Override
    @Transactional
    public void deletePost(Long id, User user, Long orgId) {
        EchoPost post = repository.findByIdAndUserIdAndOrgId(id, user.getId(), orgId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found or access denied"));
        post.setStatus("trash");
        repository.save(post);
        schedulerService.cancelJobByEntityId("EchoPublishJob", post.getId(), orgId, 2, user.getId());
    }

    @Override
    @Transactional
    public void changeStatus(Long id, String status, User user, Long orgId) {
        EchoPost post = repository.findByIdAndUserIdAndOrgId(id, user.getId(), orgId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found or access denied"));
        post.setStatus(status);
        EchoPost savedPost = repository.save(post);

        // Cancel any existing pending schedules
        schedulerService.cancelJobByEntityId("EchoPublishJob", savedPost.getId(), orgId, 2, user.getId());

        if ("published".equalsIgnoreCase(status)) {
            integrationService.handlePostPublished(convertToDto(savedPost));
        } else if ("scheduled".equalsIgnoreCase(status) && savedPost.getScheduledTime() != null) {
            schedulerService.scheduleSingle("EchoPublishJob", savedPost.getId(), savedPost.getScheduledTime(), orgId,
                    user.getId(), 2, "{\"postId\":" + savedPost.getId() + "}", savedPost.getSlug());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EchoPostDto> listPublishedPosts(Long orgId, Pageable pageable) {
        return repository.findByOrgIdAndStatus(orgId, "published", pageable).map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public EchoPostDto getPublishedPostBySlug(Long orgId, String slug) {
        EchoPost post = repository.findByOrgIdAndSlugAndStatus(orgId, slug, "published")
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        return convertToDto(post);
    }

    @Override
    @Transactional(readOnly = true)
    public EchoPostDto getPublishedPostByExternalId(String externalId) {
        EchoPost post = repository.findByExternalIdAndStatus(externalId, "published")
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        return convertToDto(post);
    }

    private EchoPostDto convertToDto(EchoPost post) {
        EchoPostDto dto = new EchoPostDto();
        dto.setId(post.getId());
        dto.setExternalId(post.getExternalId());
        dto.setOrgId(post.getOrgId());
        dto.setCategoryId(post.getCategoryId());
        dto.setTitle(post.getTitle());
        dto.setSlug(post.getSlug());
        dto.setFeaturedImageUrl(post.getFeaturedImageUrl());
        dto.setContentJson(post.getContentJson());
        dto.setMetadataJson(post.getMetadataJson());
        dto.setStatus(post.getStatus());
        dto.setScheduledTime(post.getScheduledTime());
        dto.setCreatedTime(post.getCreatedTime());
        dto.setModifiedTime(post.getModifiedTime());

        if (post.getTags() != null) {
            dto.setTags(post.getTags().stream()
                    .map(EchoTag::getName)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    @Override
    public EchoPostDto getPostByIdInternal(Long id) {
        return repository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    @Override
    @Transactional
    public void publishPostInternal(Long id) {
        repository.findById(id).ifPresent(post -> {
            boolean wasAlreadyPublished = "published".equalsIgnoreCase(post.getStatus());
            post.setStatus("published");
            EchoPost saved = repository.save(post);
            if (!wasAlreadyPublished) {
                integrationService.handlePostPublished(convertToDto(saved));
            }
        });
    }

    @Override
    @Transactional
    public String uploadFeaturedImage(Long id, MultipartFile file, User user, Long orgId) throws IOException {
        EchoPost post = repository.findByIdAndUserIdAndOrgId(id, user.getId(), orgId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found or access denied"));

        DriveFile driveFile = driveStorageService.uploadFile(
                file,
                "ECHO",
                orgId,
                user.getId(),
                "PUBLIC",
                "posts");

        String fileUrl = driveStorageService.generatePublicUrl(driveFile);
        post.setFeaturedImageUrl(fileUrl);
        repository.save(post);

        return fileUrl;
    }
}
