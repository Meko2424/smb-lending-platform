CREATE TABLE application_documents (
       id BIGSERIAL PRIMARY KEY,
       application_id BIGINT NOT NULL,

       document_type VARCHAR(100) NOT NULL,
       status VARCHAR(50) NOT NULL,

       file_name VARCHAR(255),
       storage_key VARCHAR(500),

       requested_at TIMESTAMP,
       received_at TIMESTAMP,
       reviewed_at TIMESTAMP,

       reviewed_by_user_id BIGINT,

       rejection_reason VARCHAR(1000),

       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

       CONSTRAINT fk_application_documents_application
           FOREIGN KEY (application_id)
               REFERENCES loan_applications(id)
               ON DELETE CASCADE,

       CONSTRAINT fk_application_documents_reviewed_by
           FOREIGN KEY (reviewed_by_user_id)
               REFERENCES users(id)
);

CREATE INDEX idx_application_documents_application_id
    ON application_documents(application_id);

CREATE INDEX idx_application_documents_status
    ON application_documents(status);

CREATE INDEX idx_application_documents_type
    ON application_documents(document_type);