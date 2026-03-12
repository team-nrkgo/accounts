# Technical Architecture: Echo.nrkgo.com

## 1. Technology Stack

- **Framework:** recat
- **Styling:** Tailwind CSS + Shadcn UI
- **Editor Engine:** Tiptap (ProseMirror-based Headless Editor)
- **AI Engine:** OpenAI API (GPT-4o for complex logic, o3-mini for fast drafting)

## 2. MySQL Schema (`posts` table)

```sql
CREATE TABLE posts (
  id VARCHAR(36) PRIMARY KEY,
  user_id VARCHAR(36) NOT NULL,
  title TEXT NOT NULL,
  content JSON NOT NULL, -- Stores Tiptap JSON structure
  status ENUM('draft', 'review', 'scheduled', 'published', 'trash') DEFAULT 'draft',
  slug VARCHAR(255) UNIQUE NOT NULL,
  featured_image TEXT,
  scheduled_at DATETIME,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user (user_id),
  INDEX idx_status (status)
);
```
