CREATE TABLE underwriting_reviews (
      id BIGSERIAL PRIMARY KEY,

      application_id BIGINT NOT NULL UNIQUE,

      status VARCHAR(50) NOT NULL,

      underwriter_user_id BIGINT,

      requested_amount NUMERIC(15, 2),
      annual_revenue NUMERIC(15, 2),
      existing_debt NUMERIC(15, 2),
      monthly_debt_service NUMERIC(15, 2),
      monthly_cash_flow NUMERIC(15, 2),
      debt_service_coverage_ratio NUMERIC(10, 4),

      risk_rating VARCHAR(50),

      strengths VARCHAR(3000),
      weaknesses VARCHAR(3000),
      risk_factors VARCHAR(3000),
      mitigants VARCHAR(3000),
      financial_analysis VARCHAR(4000),
      recommendation_notes VARCHAR(4000),

      started_at TIMESTAMP,
      completed_at TIMESTAMP,

      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

      CONSTRAINT fk_underwriting_reviews_application
          FOREIGN KEY (application_id)
              REFERENCES loan_applications(id)
              ON DELETE CASCADE,

      CONSTRAINT fk_underwriting_reviews_underwriter
          FOREIGN KEY (underwriter_user_id)
              REFERENCES users(id)
);

CREATE INDEX idx_underwriting_reviews_application_id
    ON underwriting_reviews(application_id);

CREATE INDEX idx_underwriting_reviews_status
    ON underwriting_reviews(status);

CREATE INDEX idx_underwriting_reviews_underwriter_user_id
    ON underwriting_reviews(underwriter_user_id);