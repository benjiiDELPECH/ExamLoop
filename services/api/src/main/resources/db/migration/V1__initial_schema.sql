-- Create goals table
CREATE TABLE goals (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_goals_device_id ON goals(device_id);

-- Create items table
CREATE TABLE items (
    id BIGSERIAL PRIMARY KEY,
    goal_id BIGINT NOT NULL,
    device_id VARCHAR(255) NOT NULL,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    box INTEGER NOT NULL DEFAULT 1,
    next_review TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_items_device_id ON items(device_id);
CREATE INDEX idx_items_goal_id ON items(goal_id);
CREATE INDEX idx_items_next_review ON items(next_review);

-- Create sessions table
CREATE TABLE sessions (
    id BIGSERIAL PRIMARY KEY,
    device_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_sessions_device_id ON sessions(device_id);
