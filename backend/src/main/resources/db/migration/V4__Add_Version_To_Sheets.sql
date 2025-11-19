-- Add version column to sheets table for audit trail
-- Version increments each time maker submits for same processInstanceId + sheetType

-- Step 1: Add version column with default value 1
ALTER TABLE sheets ADD COLUMN version INTEGER NOT NULL DEFAULT 1;

-- Step 2: For existing sheets, set version based on creation order per processInstanceId + sheetType
-- This ensures existing data has proper versioning
-- H2 compatible syntax
UPDATE sheets s
SET version = (
    SELECT COUNT(*) + 1
    FROM sheets s2
    WHERE s2.process_instance_id = s.process_instance_id
      AND s2.sheet_type = s.sheet_type
      AND s2.created_at < s.created_at
);

-- Step 3: Drop the unique constraint that was on processInstanceId + sheetType
-- Now we can have multiple sheets with same processInstanceId + sheetType (different versions)
DROP INDEX IF EXISTS idx_sheets_process_type;

-- Step 4: Create new unique constraint on processInstanceId + sheetType + version
CREATE UNIQUE INDEX idx_sheets_process_type_version ON sheets(process_instance_id, sheet_type, version);

