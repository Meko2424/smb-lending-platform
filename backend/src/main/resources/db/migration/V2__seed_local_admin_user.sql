INSERT INTO users (
    first_name,
    last_name,
    email,
    password_hash,
    enabled
)
VALUES (
   'System',
   'Administrator',
   'admin@lending.local',
   '$2a$10$6PuRqtjNJo.5HuoGQ2BuQODwV77l49OzENhw8EI5nGRAHyjDMnu9a',
   TRUE
       );

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u
         JOIN roles r ON r.name = 'ADMIN'
WHERE u.email = 'admin@lending.local';