CREATE TABLE eligibility_reviews (
        id BIGSERIAL PRIMARY KEY,
        application_id BIGINT NOT NULL UNIQUE,

        status VARCHAR(50) NOT NULL,

        reviewed_by_user_id BIGINT,

        started_at TIMESTAMP,
        completed_at TIMESTAMP,

        summary VARCHAR(2000),

        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

        CONSTRAINT fk_eligibility_reviews_application
         FOREIGN KEY (application_id)
             REFERENCES loan_applications(id)
             ON DELETE CASCADE,

        CONSTRAINT fk_eligibility_reviews_user
         FOREIGN KEY (reviewed_by_user_id)
             REFERENCES users(id)
);

CREATE TABLE eligibility_criteria (
        id BIGSERIAL PRIMARY KEY,
        eligibility_review_id BIGINT NOT NULL,

        criterion_type VARCHAR(100) NOT NULL,
        status VARCHAR(50) NOT NULL,

        notes VARCHAR(1000),

        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

        CONSTRAINT fk_eligibility_criteria_review
          FOREIGN KEY (eligibility_review_id)
              REFERENCES eligibility_reviews(id)
              ON DELETE CASCADE,

        CONSTRAINT uq_eligibility_criteria_review_type
          UNIQUE (eligibility_review_id, criterion_type)
);

CREATE INDEX idx_eligibility_reviews_application_id
    ON eligibility_reviews(application_id);

CREATE INDEX idx_eligibility_reviews_status
    ON eligibility_reviews(status);

CREATE INDEX idx_eligibility_criteria_review_id
    ON eligibility_criteria(eligibility_review_id);

CREATE INDEX idx_eligibility_criteria_status
    ON eligibility_criteria(status);