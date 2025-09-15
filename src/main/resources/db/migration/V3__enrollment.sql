-- Student enrolls into a Course
CREATE TABLE IF NOT EXISTS enrollment (
  id           BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  student_id   BIGINT NOT NULL,
  course_id    BIGINT NOT NULL,
  enrolled_at  DATETIME(3) NOT NULL,
  status       VARCHAR(20) NOT NULL,

  CONSTRAINT fk_enroll_student FOREIGN KEY (student_id) REFERENCES app_user(id)
    ON DELETE CASCADE,
  CONSTRAINT fk_enroll_course  FOREIGN KEY (course_id)  REFERENCES course(id)
    ON DELETE CASCADE,
  CONSTRAINT uq_enroll UNIQUE (student_id, course_id)
);

CREATE INDEX idx_enroll_student ON enrollment(student_id);
CREATE INDEX idx_enroll_course  ON enrollment(course_id);
CREATE INDEX idx_enroll_status  ON enrollment(status);
