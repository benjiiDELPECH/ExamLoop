-- V4: Fix legacy schema where question_attempts.is_correct was stored as TEXT/VARCHAR.
--
-- Symptom:
--   java.lang.String cannot be cast to java.lang.Boolean
--   when Hibernate reads/writes QuestionAttemptEntity.isCorrect.
--
-- Root cause:
--   In some dev DBs, question_attempts was created earlier with is_correct as a string column.
--   V2 uses CREATE TABLE IF NOT EXISTS, so it doesn't repair existing column types.
DO $$
BEGIN
  IF EXISTS (
    SELECT 1
    FROM information_schema.columns
    WHERE table_schema = current_schema()
      AND table_name = 'question_attempts'
      AND column_name = 'is_correct'
      AND data_type <> 'boolean'
  ) THEN
    ALTER TABLE question_attempts
      ALTER COLUMN is_correct TYPE BOOLEAN
      USING (
        CASE
          WHEN is_correct IS NULL THEN FALSE
          WHEN lower(is_correct::text) IN ('true', 't', '1', 'yes', 'y') THEN TRUE
          WHEN lower(is_correct::text) IN ('false', 'f', '0', 'no', 'n', '') THEN FALSE
          ELSE FALSE
        END
      );

    ALTER TABLE question_attempts
      ALTER COLUMN is_correct SET NOT NULL;
  END IF;
END $$;

