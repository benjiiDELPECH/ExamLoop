-- V2: Schema for Hybrid Learning Algorithm
-- Adds support for:
-- - Question difficulty levels
-- - Spaced repetition (review states)
-- - Question attempts tracking
-- - Usage quotas

-- ============================================================================
-- PROFILES (User identity)
-- ============================================================================
CREATE TABLE IF NOT EXISTS profiles (
    user_id VARCHAR(255) PRIMARY KEY,
    premium BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- ============================================================================
-- GOALS (Exams) - Enhanced with public flag
-- ============================================================================
ALTER TABLE goals ADD COLUMN IF NOT EXISTS is_public BOOLEAN NOT NULL DEFAULT FALSE;

-- ============================================================================
-- QUESTIONS (Enhanced from items)
-- ============================================================================
-- Add difficulty column to items table
ALTER TABLE items ADD COLUMN IF NOT EXISTS difficulty VARCHAR(20) NOT NULL DEFAULT 'MEDIUM';
ALTER TABLE items ADD COLUMN IF NOT EXISTS chapter VARCHAR(255);
ALTER TABLE items ADD COLUMN IF NOT EXISTS explanation TEXT;
ALTER TABLE items ADD COLUMN IF NOT EXISTS item_type VARCHAR(20) NOT NULL DEFAULT 'SINGLE_CHOICE';

-- Create index on difficulty for filtering
CREATE INDEX IF NOT EXISTS idx_items_difficulty ON items(difficulty);
CREATE INDEX IF NOT EXISTS idx_items_chapter ON items(chapter);

-- ============================================================================
-- QUESTION ATTEMPTS (History of all answers)
-- ============================================================================
CREATE TABLE IF NOT EXISTS question_attempts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    question_id BIGINT NOT NULL REFERENCES items(id),
    user_id VARCHAR(255) NOT NULL,
    is_correct BOOLEAN NOT NULL,
    selected_choice_ids JSONB,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_attempts_user_id ON question_attempts(user_id);
CREATE INDEX idx_attempts_question_id ON question_attempts(question_id);
CREATE INDEX idx_attempts_timestamp ON question_attempts(timestamp);
CREATE INDEX idx_attempts_user_question ON question_attempts(user_id, question_id);

-- ============================================================================
-- REVIEW STATES (Spaced Repetition state per user/question)
-- ============================================================================
CREATE TABLE IF NOT EXISTS review_states (
    question_id BIGINT NOT NULL REFERENCES items(id),
    user_id VARCHAR(255) NOT NULL,
    mastery_level VARCHAR(20) NOT NULL DEFAULT 'NOVICE',
    success_count INT NOT NULL DEFAULT 0,
    fail_count INT NOT NULL DEFAULT 0,
    last_reviewed_at TIMESTAMP WITH TIME ZONE,
    next_review_at TIMESTAMP WITH TIME ZONE NOT NULL,
    optimal_interval_days INT NOT NULL DEFAULT 1,
    PRIMARY KEY (question_id, user_id)
);

CREATE INDEX idx_review_states_user ON review_states(user_id);
CREATE INDEX idx_review_states_next_review ON review_states(next_review_at);
CREATE INDEX idx_review_states_mastery ON review_states(mastery_level);

-- ============================================================================
-- USAGE DAILY (Quota tracking)
-- ============================================================================
CREATE TABLE IF NOT EXISTS usage_daily (
    user_id VARCHAR(255) NOT NULL,
    day DATE NOT NULL,
    reviews_count INT NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id, day)
);

CREATE INDEX idx_usage_daily_day ON usage_daily(day);

-- ============================================================================
-- COMMENTS
-- ============================================================================
COMMENT ON TABLE profiles IS 'User profiles with premium status';
COMMENT ON TABLE question_attempts IS 'History of all question answers for analytics';
COMMENT ON TABLE review_states IS 'Spaced repetition state per user/question';
COMMENT ON TABLE usage_daily IS 'Daily quota tracking for free tier';

COMMENT ON COLUMN items.difficulty IS 'EASY, MEDIUM, HARD, VERY_HARD';
COMMENT ON COLUMN items.item_type IS 'SINGLE_CHOICE, MULTIPLE_CHOICE, OPEN';
COMMENT ON COLUMN review_states.mastery_level IS 'NOVICE, LEARNING, COMPETENT, MASTERED';
