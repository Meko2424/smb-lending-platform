CREATE TABLE application_status_history (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    from_status VARCHAR(50),
    to_status VARCHAR(50) NOT NULL,
    changed_by_user_id BIGINT NOT NULL,
    comment VARCHAR(1000),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_application_status_history_application
        FOREIGN KEY (application_id)
            REFERENCES loan_applications(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_application_status_history_user
        FOREIGN KEY (changed_by_user_id)
            REFERENCES users(id)
);

CREATE INDEX idx_application_status_history_application_id
    ON application_status_history(application_id);

CREATE INDEX idx_application_status_history_changed_at
    ON application_status_history(changed_at);