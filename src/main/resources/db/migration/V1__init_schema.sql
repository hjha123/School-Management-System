/* ============================================================
   V1__init_schema.sql  –  Initial schema for edu.zis.school
   MySQL 8.x / InnoDB / UTF‑8 MB4
   ============================================================ */

-- 1)  Roles
CREATE TABLE role (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(30) NOT NULL UNIQUE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- Optional seed data
INSERT INTO role (name) VALUES ('ADMIN'), ('TEACHER'), ('STUDENT'), ('PARENT');

-- 2)  Users
CREATE TABLE users (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    email        VARCHAR(100) NOT NULL UNIQUE,
    role_id      BIGINT       NOT NULL,
    is_active    BOOLEAN      DEFAULT TRUE,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role
        FOREIGN KEY (role_id)
        REFERENCES role(id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 3)  Students
CREATE TABLE student (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT       NOT NULL,
    admission_no   VARCHAR(20)  NOT NULL UNIQUE,
    first_name     VARCHAR(50)  NOT NULL,
    last_name      VARCHAR(50),
    date_of_birth  DATE,
    gender         ENUM('M','F','O'),
    class_id       BIGINT,             -- will FK to class table in a later migration
    enroll_date    DATE,
    is_active      BOOLEAN      DEFAULT TRUE,
    CONSTRAINT fk_student_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

/* ------------------------------------------------------------
   Indexes for faster look‑ups
   ------------------------------------------------------------ */
CREATE INDEX idx_users_role_id  ON users(role_id);
CREATE INDEX idx_student_class  ON student(class_id);
