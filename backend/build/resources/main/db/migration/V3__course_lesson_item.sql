CREATE TABLE courses (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    title VARCHAR(255) NOT NULL,
    level VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE lessons (
    id BIGSERIAL PRIMARY KEY,
    course_id BIGINT NOT NULL REFERENCES courses (id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    subtitle VARCHAR(255),
    order_index INT NOT NULL,
    level VARCHAR(50) NOT NULL,
    xp_reward INT NOT NULL DEFAULT 20,
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_lessons_course_order UNIQUE (course_id, order_index)
);

CREATE TABLE lesson_items (
    id BIGSERIAL PRIMARY KEY,
    lesson_id BIGINT NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    order_index INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    prompt TEXT,
    prompt_translation TEXT,
    buryat TEXT,
    russian TEXT,
    audio_url VARCHAR(1024),
    options JSONB,
    correct_option_index INT,
    correct_answer_text TEXT,
    explanation TEXT,
    needs_review BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_lessons_course_id ON lessons (course_id);
CREATE INDEX idx_lesson_items_lesson_id ON lesson_items (lesson_id);