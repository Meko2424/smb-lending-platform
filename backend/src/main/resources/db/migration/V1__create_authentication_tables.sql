CREATE TABLE roles (
       id BIGSERIAL PRIMARY KEY,
       name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
       id BIGSERIAL PRIMARY KEY,
       first_name VARCHAR(100) NOT NULL,
       last_name VARCHAR(100) NOT NULL,
       email VARCHAR(255) NOT NULL UNIQUE,
       password_hash VARCHAR(255) NOT NULL,
       enabled BOOLEAN NOT NULL DEFAULT TRUE,
       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
       user_id BIGINT NOT NULL,
       role_id BIGINT NOT NULL,

       PRIMARY KEY (user_id, role_id),

       CONSTRAINT fk_user_roles_user
            FOREIGN KEY (user_id)
                REFERENCES users(id)
                ON DELETE CASCADE,

       CONSTRAINT fk_user_roles_role
            FOREIGN KEY (role_id)
                REFERENCES roles(id)
                ON DELETE CASCADE
);

INSERT INTO roles (name) VALUES
     ('ADMIN'),
     ('LOAN_OFFICER'),
     ('PROCESSOR'),
     ('UNDERWRITER'),
     ('CREDIT_MANAGER'),
     ('CLOSING_SPECIALIST');