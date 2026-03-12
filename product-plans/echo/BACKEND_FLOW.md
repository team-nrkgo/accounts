# Backend Engineering Flow: Echo (Blog Publishing)

**Focus:** AI Content Generation, Organization-Based Persistence, and SnapSteps Architecture.

## 1. Module Structure (SnapSteps Pattern)

Echo will be implemented as a new module following the established structure in `com.nrkgo.accounts.modules.echo`:

- **`controller`**: REST endpoints (Org-prefixed).
- **`service`**: Business logic (AI orchestration, Post lifecycle).
- **`repository`**: JPA repositories with `org_id` and `user_id` filtering.
- **`dto`**: Request/Response objects for Posts and Initialization.
- **`model`**: Persistent entities (`EchoPost`, `EchoCategory`, etc.).

## 2. Initialization Flow (`/init`)

Before the dashboard loads, the frontend calls the unified product init endpoint to fetch user, organization, and plan context.

- **Endpoint:** `GET /echo/init?orgId={orgId}`
- **Handled by:** `ProductInitController` (already implemented, handles product: "echo").
- **Returns:**
  - `user_information`: Current user profile.
  - `default_organization`: Currently selected org.
  - `plan`: Active plan for "Echo" (Free/Pro/Team) including feature gates.

## 3. Organization-Based API Design

All product-specific APIs require an `orgId` to ensure total data isolation. The `orgId` is provided via the **Request Payload** (for POST/PUT) or **Query Parameters** (for GET/DELETE).

| Method     | Endpoint                      | Description            | Logic Note                                 |
| :--------- | :---------------------------- | :--------------------- | :----------------------------------------- |
| **GET**    | `/echo/api/posts`             | List Posts (Paginated) | Requires `orgId` as query param.           |
| **GET**    | `/echo/api/posts/{id}`        | Get Post Detail        | Includes Tiptap JSON content.              |
| **POST**   | `/echo/api/posts`             | Create/Generate Post   | `orgId` in JSON payload.                   |
| **PUT**    | `/echo/api/posts/{id}`        | Update Post            | `orgId` in JSON payload.                   |
| **PATCH**  | `/echo/api/posts/{id}/status` | Change Status          | `orgId` in payload or params.              |
| **DELETE** | `/echo/api/posts`             | Delete Post            | Requires `id` and `orgId` as query params. |

**Validation Rule:** The backend MUST verify that the authenticated `userId` has active membership in the provided `orgId` (validated from the session/context).

## 4. AI Generation Flow (SnapSteps Engine)

The generation logic follows the "3 Entry Methods" defined in the PRD:

1. **Manual:** Creates an empty post with a default Tiptap JSON schema (`"type": "doc", "content": []`).
2. **Source (URL):** Scraper -> Extractor -> AI Summary -> Tiptap JSON.
3. **Search (Keyword):** SERP Research -> AI Draft -> Tiptap JSON.

## 5. Pagination & Search (Spring Data)

Listings must use the existing pagination flow to handle high-density data.

- **Controller:** `@RequestParam(defaultValue = "0") Integer page`, `@RequestParam(defaultValue = "15") Integer size`.
- **Repository:** `findByUserIdAndOrgId(userId, orgId, Pageable pageable)`.
- **Service:** Returns `ApiResponse.paginatedSuccess`.

## 6. Database Rules & Persistence

Table: `echo_posts`

- `id`: BigInt (Primary Key).
- `external_id`: String (UUID for public/API access).
- `user_id`: BigInt (Owner).
- `org_id`: BigInt (Organization context).
- `title`: String.
- `slug`: String (URL friendly).
- `content_json`: Text/JSON (Tiptap document nodes).
- `metadata_json`: Text/JSON (SEO titles, descriptions, featured images).
- `status`: Enum (Draft, Published, Scheduled, Trash).
- `created_time` / `modified_time`: Timestamps.

**Security Rule:** Every query MUST filter by `user_id` AND `org_id` fetched from the session/context. If the IDs are taken from the request payload, they MUST be validated against the `org_user` mapping before execution. Never trust a naked `org_id` from a JSON body.
