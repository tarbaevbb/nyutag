CREATE TABLE users (
 id BIGSERIAL PRIMARY KEY,
 telegram_id BIGINT NOT NULL UNIQUE,
 username VARCHAR(255),
 first_name VARCHAR(255),
 level VARCHAR(50) NOT NULL,
 goal VARCHAR(255),
 current_lesson INT NOT NULL,
 streak INT NOT NULL,
 last_activity_at TIMESTAMPTZ,
 created_at TIMESTAMPTZ NOT NULL
);
