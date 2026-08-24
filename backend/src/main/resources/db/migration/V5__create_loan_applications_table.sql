CREATE TABLE loan_applications (
       id BIGSERIAL PRIMARY KEY,
       business_id BIGINT NOT NULL,

       application_number VARCHAR(50) NOT NULL UNIQUE,

       loan_product VARCHAR(50) NOT NULL,
       loan_purpose VARCHAR(100) NOT NULL,
       requested_amount NUMERIC(19, 2) NOT NULL,
       requested_term_months INTEGER,

       status VARCHAR(50) NOT NULL,

       assigned_loan_officer_id BIGINT,
       assigned_underwriter_id BIGINT,

       submitted_at TIMESTAMP,
       decision_at TIMESTAMP,
       funded_at TIMESTAMP,

       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

       CONSTRAINT fk_loan_applications_business
           FOREIGN KEY (business_id)
               REFERENCES businesses(id),

       CONSTRAINT fk_loan_applications_loan_officer
           FOREIGN KEY (assigned_loan_officer_id)
               REFERENCES users(id),

       CONSTRAINT fk_loan_applications_underwriter
           FOREIGN KEY (assigned_underwriter_id)
               REFERENCES users(id),

       CONSTRAINT chk_loan_applications_requested_amount
           CHECK (requested_amount > 0),

       CONSTRAINT chk_loan_applications_requested_term
           CHECK (
               requested_term_months IS NULL
                   OR requested_term_months > 0
               )
);