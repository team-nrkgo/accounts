package com.nrkgo.accounts.modules.drive.service;

import com.nrkgo.accounts.modules.drive.model.DriveFile;
import com.nrkgo.accounts.modules.drive.repository.DriveFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Service
public class DriveStorageService {

    private final DriveFileRepository repository;

    @Value("${drive.provider.active:CLOUDFLARE_R2}")
    private String activeProvider;

    @Value("${drive.cloudflare.endpoint:}")
    private String cloudflareEndpoint;

    @Value("${drive.cloudflare.access-key:}")
    private String cloudflareAccessKey;

    @Value("${drive.cloudflare.secret-key:}")
    private String cloudflareSecretKey;

    @Value("${drive.cloudflare.bucket:}")
    private String cloudflareBucket;

    @Value("${drive.cloudflare.public-url:}")
    private String cloudflarePublicUrl;

    private S3Client s3Client;

    public DriveStorageService(DriveFileRepository repository) {
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        if ("CLOUDFLARE_R2".equals(activeProvider) && cloudflareEndpoint != null && !cloudflareEndpoint.isEmpty()) {
            s3Client = S3Client.builder()
                    .endpointOverride(URI.create(cloudflareEndpoint))
                    .region(Region.of("auto"))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(cloudflareAccessKey, cloudflareSecretKey)))
                    .build();
        }
    }

    @Transactional
    public DriveFile uploadFile(MultipartFile file, String productModule, Long orgId, Long userId, String accessLevel,
            String subFolder)
            throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            originalFilename = "unknown";
        }

        String extension = "";
        int extIndex = originalFilename.lastIndexOf('.');
        if (extIndex > 0) {
            extension = originalFilename.substring(extIndex + 1);
        }

        String externalId = UUID.randomUUID().toString();

        // org_101/echo/settings/uuid.jpg
        String folderPath = (subFolder != null && !subFolder.trim().isEmpty()) ? subFolder.toLowerCase() + "/" : "";
        String objectKey = String.format("org_%d/%s/%s%s.%s", orgId, productModule.toLowerCase(), folderPath,
                externalId, extension);

        if ("CLOUDFLARE_R2".equals(activeProvider) && s3Client != null) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(cloudflareBucket)
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } else {
            // Fallback to purely saving DB record (useful for local dev if cloudflare is
            // not configured)
            System.err.println(
                    "Active provider S3 client is not initialized. File will only be recorded in the database.");
        }

        DriveFile driveFile = new DriveFile();
        driveFile.setExternalId(externalId);
        driveFile.setFileName(originalFilename);
        driveFile.setFileExtension(extension);
        driveFile.setStorageProvider(activeProvider);
        driveFile.setFileSize(file.getSize());
        driveFile.setFileType(file.getContentType());
        driveFile.setProductModule(productModule);
        driveFile.setUserId(userId);
        driveFile.setOrgId(orgId);
        driveFile.setAccessLevel(accessLevel);
        driveFile.setStoragePath(objectKey);
        driveFile.setCreatedTime(System.currentTimeMillis());

        return repository.save(driveFile);
    }

    public String generatePublicUrl(DriveFile file) {
        if ("CLOUDFLARE_R2".equals(file.getStorageProvider()) && cloudflarePublicUrl != null
                && !cloudflarePublicUrl.isEmpty()) {
            String folderPath = (file.getAccessLevel() != null && file.getAccessLevel().contains("/"))
                    ? file.getAccessLevel() + "/"
                    : "";
            // Note: We'll repurpose a field or just use the DB to reconstruct the key.
            // Actually, it's safer to store the full object key in the DB or calculate it.
            // For now, let's keep it simple and assume the same logic.
            return cloudflarePublicUrl + "/" + getObjectKey(file);
        }
        return "/api/drive/v1/files/download/" + file.getExternalId();
    }

    private String getObjectKey(DriveFile file) {
        return file.getStoragePath();
    }

    public DriveFile getFileByExternalId(String externalId) {
        return repository.findByExternalId(externalId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
    }

    public ResponseInputStream<GetObjectResponse> getFileStream(DriveFile file) {
        if ("CLOUDFLARE_R2".equals(file.getStorageProvider()) && s3Client != null) {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(cloudflareBucket)
                    .key(file.getStoragePath())
                    .build();

            return s3Client.getObject(getObjectRequest);
        }
        throw new RuntimeException("S3 client not initialized or provider mismatch");
    }
}
