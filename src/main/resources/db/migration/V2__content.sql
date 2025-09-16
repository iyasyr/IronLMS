-- Courses (created by an Instructor)
CREATE TABLE IF NOT EXISTS course (
  id            BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  instructor_id BIGINT NOT NULL,
  title         VARCHAR(200) NOT NULL,
  description   TEXT NULL,
  status        VARCHAR(20) NOT NULL,
  created_at    DATETIME(3) NOT NULL,
  published_at  DATETIME(3) NULL,
  CONSTRAINT fk_course_instructor
    FOREIGN KEY (instructor_id) REFERENCES app_user(id)
);

CREATE INDEX idx_course_status       ON course(status);
CREATE INDEX idx_course_instructor   ON course(instructor_id);

-- Lessons in a course
CREATE TABLE IF NOT EXISTS lesson (
  id            BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  course_id     BIGINT NOT NULL,
  title         VARCHAR(200) NOT NULL,
  content_url   VARCHAR(2048) NULL,
  order_index   INT NOT NULL,
  CONSTRAINT fk_lesson_course
    FOREIGN KEY (course_id) REFERENCES course(id)
      ON DELETE CASCADE
);

CREATE INDEX idx_lesson_course       ON lesson(course_id);
CREATE UNIQUE INDEX uq_lesson_order  ON lesson(course_id, order_index);

-- Assignments in a course
CREATE TABLE IF NOT EXISTS assignment (
  id            BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  course_id     BIGINT NOT NULL,
  title         VARCHAR(200) NOT NULL,
  instructions  TEXT NULL,
  due_at        DATETIME(3) NULL,
  max_points    INT NOT NULL,
  allow_late    BOOLEAN NOT NULL,
  CONSTRAINT fk_assignment_course
    FOREIGN KEY (course_id) REFERENCES course(id)
      ON DELETE CASCADE
);

CREATE INDEX idx_assignment_course ON assignment(course_id);
