-- V5: Change selected_choice_ids from JSONB to TEXT for simpler handling
DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = 'question_attempts'
      AND column_name = 'selected_choice_ids'
      AND data_type = 'jsonb'
  ) THEN
    ALTER TABLE question_attempts
      ALTER COLUMN selected_choice_ids TYPE TEXT
      USING selected_choice_ids::TEXT;
  END IF;
END $$;
