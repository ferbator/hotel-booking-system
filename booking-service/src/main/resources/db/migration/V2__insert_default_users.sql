INSERT INTO users (username, password, role)
SELECT 'admin',
       '$2a$10$uWc8N1yKZ9Q1mZpZz0rY4uRr6YzvH3EJ3gZ8eY2pL0zZyYyYy', -- admin
       'ADMIN' WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin'
);

INSERT INTO users (username, password, role)
SELECT 'user',
       '$2a$10$uWc8N1yKZ9Q1mZpZz0rY4uRr6YzvH3EJ3gZ8eY2pL0zZyYyYy', -- user
       'USER' WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'user'
);
