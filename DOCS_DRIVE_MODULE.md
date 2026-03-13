# NRKGO Drive Module Integration Guide

This document explains how to use the centralized Cloudflare R2 file storage system in your Java Spring Boot backend.

## 1. Core Architecture (The "DB-First" Flow)

In our system, the **Database is the Security Guard**.

- **Storage (Cloudflare R2)**: Holds the raw bytes of the file. It is private and has no public access.
- **Database (MySQL/H2)**: Holds the metadata: What the file is, which product it belongs to, who uploaded it, and what the **Access Level** is.
- **Java Controller**: Acting as a secure proxy, it checks the database before fetching anything from Cloudflare.

---

## 2. Storage Path Structure

Files are organized in a way that is optimized for SaaS scalability (Org-First):
`{org_id} / {product_module} / {optional_subfolder} / {uuid}.{ext}`

**Example:** `org_1/echo/posts/a1b2c3d4.jpg`

---

## 3. Access Levels (Security Rules)

The `DriveFile` table uses an `access_level` field to decide who can see a file:

| Level            | Rule                                                  | Use Case                                 |
| :--------------- | :---------------------------------------------------- | :--------------------------------------- |
| **PUBLIC**       | Anyone with the link can view.                        | Blog images, public site logos.          |
| **ORG_SHARED**   | Any user logged into that specific Organization.      | Shared contracts, internal team assets.  |
| **USER_PRIVATE** | Only the original uploader (current user == creator). | Private drafts, user identity documents. |

**How validation works:**

1. User requests `/api/drive/v1/files/download/{uuid}`.
2. Java fetches file metadata from DB.
3. If `PUBLIC`, serve file.
4. If `ORG_SHARED`, check if the requester belongs to the `org_id` in the `org_users` table.
5. If `USER_PRIVATE`, check if the requester's `id` matches the file's `user_id`.

---

## 4. How to Upload a File (Java Code)

Inject the `DriveStorageService` into your module to start saving files.

```java
@Service
public class EchoService {
    private final DriveStorageService drive;

    public void updateLogo(MultipartFile file, Long orgId, User user) {
        // Upload to Cloudflare and save to DB
        DriveFile saved = drive.uploadFile(
            file,
            "ECHO",     // Product Module
            orgId,
            user.getId(),
            "PUBLIC",   // Access Level
            "settings"  // Sub-Folder
        );

        // Use the External ID to generate the Proxy URL
        String url = drive.generatePublicUrl(saved);
    }
}
```

---

## 5. API Endpoints for Frontend

### Upload File

`POST /api/drive/v1/files/upload`

- **Body (Multipart)**: `file`, `org_id`, `product`, `access_level`

### Download/View File

`GET /api/drive/v1/files/download/{external_id}`

- Returns the raw file (image, pdf, etc.) with correct content-type headers.

---

## 6. Pro-Tips for Developers

- **Caching**: The download endpoint is ready for server-side caching (e.g., Redis).
- **Security**: Never expose your Cloudflare Secret Keys in the frontend. Always use this backend proxy.
- **Orphan Prevention**: If you delete a record from your product table (like a blog post), remember to also decide if you want to delete the file from the `drive_files` table.
