-- Refactor Sheet table: Replace formKey with sheetType
-- This makes the relationship clearer: sheetType = item/plan/product

-- Step 1: Add new column
ALTER TABLE sheets ADD COLUMN sheet_type VARCHAR(50);

-- Step 2: Migrate existing data from formKey to sheetType
-- Extract entity type from formKey patterns like "/maker/item-edit" or "maker-item-edit"
UPDATE sheets 
SET sheet_type = CASE
    WHEN form_key LIKE '%item%' THEN 'item'
    WHEN form_key LIKE '%plan%' THEN 'plan'
    WHEN form_key LIKE '%product%' THEN 'product'
    ELSE NULL
END;

-- Step 3: Make sheet_type NOT NULL (all rows should now have a value)
ALTER TABLE sheets ALTER COLUMN sheet_type SET NOT NULL;

-- Step 4: Drop the old formKey column
ALTER TABLE sheets DROP COLUMN form_key;

-- Step 5: Update the unique constraint to use sheetType instead of formKey
-- The combination of processInstanceId + sheetType should be unique
CREATE UNIQUE INDEX idx_sheets_process_type ON sheets(process_instance_id, sheet_type);

COMMENT ON COLUMN sheets.sheet_type IS 'Entity type: item, plan, or product';

