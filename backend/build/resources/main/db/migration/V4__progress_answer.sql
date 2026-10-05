CREATE TABLE user_lesson_progress (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    lesson_id BIGINT NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'AVAILABLE',
    progress INT NOT NULL DEFAULT 0,
    completed_at TIMESTAMPTZ,
    attempts INT NOT NULL DEFAULT 0,
    best_score INT,
    xp_earned INT NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_progress_user_lesson UNIQUE (user_id, lesson_id)
);

CREATE TABLE answers (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    lesson_id BIGINT NOT NULL REFERENCES lessons (id) ON DELETE CASCADE,
    lesson_item_id BIGINT NOT NULL REFERENCES lesson_items (id) ON DELETE CASCADE,
    selected_option_index INT,
    selected_text TEXT,
    is_correct BOOLEAN NOT NULL,
    response_ms INT,
    answered_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_progress_user_id ON user_lesson_progress (user_id);
CREATE INDEX idx_progress_lesson_id ON user_lesson_progress (lesson_id);
CREATE INDEX idx_answers_user_id ON answers (user_id);
CREATE INDEX idx_answers_lesson_id ON answers (lesson_id);
CREATE INDEX idx_answers_lesson_item_id ON answers (lesson_item_id);