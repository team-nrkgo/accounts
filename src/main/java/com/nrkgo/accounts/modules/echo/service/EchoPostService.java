package com.nrkgo.accounts.modules.echo.service;

import com.nrkgo.accounts.model.User;
import com.nrkgo.accounts.modules.echo.dto.EchoPostDto;
import com.nrkgo.accounts.modules.echo.model.EchoPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EchoPostService {

    EchoPostDto createPost(EchoPostDto dto, User user, Long orgId);

    EchoPostDto updatePost(Long id, EchoPostDto dto, User user, Long orgId);

    EchoPostDto getPostById(Long id, User user, Long orgId);

    EchoPostDto getPostByExternalId(String externalId);

    Page<EchoPostDto> listPosts(Long orgId, User user, String status, String search, Pageable pageable);

    void deletePost(Long id, User user, Long orgId);

    void changeStatus(Long id, String status, User user, Long orgId);

    Page<EchoPostDto> listPublishedPosts(Long orgId, Pageable pageable);

    EchoPostDto getPublishedPostBySlug(Long orgId, String slug);

    EchoPostDto getPublishedPostByExternalId(String externalId);

    EchoPostDto getPostByIdInternal(Long id);

    void publishPostInternal(Long id);
}
