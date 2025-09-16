CREATE TABLE IF NOT EXISTS submission (
  id            BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
  assignment_id BIGINT NOT NULL,
  student_id    BIGINT NOT NULL,
  submitted_at  DATETIME(3) NOT NULL,
  artifact_url  VARCHAR(2048) NOT NULL,
  status        VARCHAR(30) NOT NULL,
  score         INT NULL,
  feedback      TEXT NULL,
  version       INT NOT NULL,

  CONSTRAINT fk_submission_assignment
    FOREIGN KEY (assignment_id) REFERENCES assignment(id)
    ON DELETE CASCADE,

  CONSTRAINT fk_submission_student
    FOREIGN KEY (student_id) REFERENCES app_user(id)
    ON DELETE CASCADE,

  CONSTRAINT uq_submission_one_per_student
    UNIQUE (assignment_id, student_id)
);

CREATE INDEX idx_submission_assignment ON submission(assignment_id);
CREATE INDEX idx_submission_student    ON submission(student_id);
CREATE INDEX idx_submission_status     ON submission(status);
