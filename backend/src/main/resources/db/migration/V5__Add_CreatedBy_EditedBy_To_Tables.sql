-- Add createdBy and editedBy columns to staging tables and Sheet table
-- This migration adds audit trail fields to track who created and last edited records

-- ========== STAGING TABLES ==========

-- Add createdBy to item_staging
ALTER TABLE item_staging ADD COLUMN created_by VARCHAR(255);
COMMENT ON COLUMN item_staging.created_by IS 'User who first created this record';

-- Add createdBy to plan_staging
ALTER TABLE plan_staging ADD COLUMN created_by VARCHAR(255);
COMMENT ON COLUMN plan_staging.created_by IS 'User who first created this record';

-- Add createdBy to product_staging
ALTER TABLE product_staging ADD COLUMN created_by VARCHAR(255);
COMMENT ON COLUMN product_staging.created_by IS 'User who first created this record';

-- Note: editedBy already exists in staging tables, so we don't need to add it

-- ========== SHEET TABLE ==========

-- Add editedBy to sheets table
ALTER TABLE sheets ADD COLUMN edited_by VARCHAR(255);
COMMENT ON COLUMN sheets.edited_by IS 'User who last edited this sheet';

-- Note: createdBy already exists in sheets table, so we don't need to add it

-- ========== DATA MIGRATION ==========

-- For existing staging records, set createdBy = editedBy (best guess for existing data)
UPDATE item_staging SET created_by = edited_by WHERE created_by IS NULL AND edited_by IS NOT NULL;
UPDATE plan_staging SET created_by = edited_by WHERE created_by IS NULL AND edited_by IS NOT NULL;
UPDATE product_staging SET created_by = edited_by WHERE created_by IS NULL AND edited_by IS NOT NULL;

-- For existing staging records without editedBy, set both to 'system' (for data integrity)
UPDATE item_staging SET created_by = 'system', edited_by = 'system' WHERE created_by IS NULL;
UPDATE plan_staging SET created_by = 'system', edited_by = 'system' WHERE created_by IS NULL;
UPDATE product_staging SET created_by = 'system', edited_by = 'system' WHERE created_by IS NULL;

-- For existing sheets, set editedBy = createdBy (initial state - no edits yet)
UPDATE sheets SET edited_by = created_by WHERE edited_by IS NULL AND created_by IS NOT NULL;

-- For existing sheets without createdBy, set both to 'system' (for data integrity)
UPDATE sheets SET created_by = 'system', edited_by = 'system' WHERE created_by IS NULL;

-- ========== REMOVE REDUNDANT updated_at COLUMNS ==========
-- Remove updated_at from staging tables since we have editedAt for business edits
-- and createdAt/approvedAt for other timestamps

ALTER TABLE item_staging DROP COLUMN IF EXISTS updated_at;
ALTER TABLE plan_staging DROP COLUMN IF EXISTS updated_at;
ALTER TABLE product_staging DROP COLUMN IF EXISTS updated_at;
ALTER TABLE sheets DROP COLUMN IF EXISTS updated_at;

