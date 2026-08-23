CREATE TABLE business_owners (
     id BIGSERIAL PRIMARY KEY,
     business_id BIGINT NOT NULL,

     first_name VARCHAR(100) NOT NULL,
     last_name VARCHAR(100) NOT NULL,
     title VARCHAR(100),

     ownership_percentage NUMERIC(5, 2) NOT NULL,

     email VARCHAR(255),
     phone VARCHAR(30),

     guarantor BOOLEAN NOT NULL DEFAULT FALSE,

     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

     CONSTRAINT fk_business_owners_business
         FOREIGN KEY (business_id)
             REFERENCES businesses(id)
             ON DELETE CASCADE,

     CONSTRAINT chk_business_owners_ownership_percentage
         CHECK (
             ownership_percentage >= 0
                 AND ownership_percentage <= 100
             )
);