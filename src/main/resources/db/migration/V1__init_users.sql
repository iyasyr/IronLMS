-- Single-table inheritance for User / Student / Instructor
-- We avoid table name "user" (reserved) and use app_user instead.
CREATE TABLE IF NOT EXISTS app_user (
  id               BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  dtype            VARCHAR(31)      NOT NULL,         -- discriminator: USER/STUDENT/INSTRUCTOR
  email            VARCHAR(255)     NOT NULL,
  password_hash    VARCHAR(255)     NOT NULL,
  full_name        VARCHAR(255)     NOT NULL,
  role             VARCHAR(32)      NOT NULL,         -- ADMIN / INSTRUCTOR / STUDENT
  created_at       DATETIME(3)      NOT NULL,

  -- Child-specific fields (nullable in SINGLE_TABLE)
  student_number   VARCHAR(64)      NULL,
  bio              TEXT             NULL,

  CONSTRAINT uq_app_user_email UNIQUE (email)
);

CREATE INDEX idx_app_user_role ON app_user(role);
CREATE INDEX idx_app_user_dtype ON app_user(dtype);
