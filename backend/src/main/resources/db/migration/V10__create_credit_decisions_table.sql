CREATE TABLE credit_decisions (
      id BIGSERIAL PRIMARY KEY,

      application_id BIGINT NOT NULL UNIQUE,

      status VARCHAR(50) NOT NULL,
      decision_type VARCHAR(50),

      credit_manager_user_id BIGINT,

      requested_amount NUMERIC(15, 2),
      approved_amount NUMERIC(15, 2),

      requested_term_months INTEGER,
      approved_term_months INTEGER,

      interest_rate NUMERIC(8, 4),

      conditions VARCHAR(4000),
      decline_reason VARCHAR(2000),
      decision_notes VARCHAR(4000),

      started_at TIMESTAMP,
      decided_at TIMESTAMP,

      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

      CONSTRAINT fk_credit_decisions_application
          FOREIGN KEY (application_id)
              REFERENCES loan_applications(id)
              ON DELETE CASCADE,

      CONSTRAINT fk_credit_decisions_credit_manager
          FOREIGN KEY (credit_manager_user_id)
              REFERENCES users(id),

      CONSTRAINT chk_credit_decisions_approved_amount
          CHECK (
              approved_amount IS NULL
                  OR approved_amount > 0
              ),

      CONSTRAINT chk_credit_decisions_approved_term
          CHECK (
              approved_term_months IS NULL
                  OR approved_term_months > 0
              ),

      CONSTRAINT chk_credit_decisions_interest_rate
          CHECK (
              interest_rate IS NULL
                  OR interest_rate >= 0
              )
);

CREATE INDEX idx_credit_decisions_application_id
    ON credit_decisions(application_id);

CREATE INDEX idx_credit_decisions_status
    ON credit_decisions(status);

CREATE INDEX idx_credit_decisions_credit_manager_user_id
    ON credit_decisions(credit_manager_user_id);