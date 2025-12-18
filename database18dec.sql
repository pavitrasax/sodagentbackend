ALTER TABLE sodagent.user_checklist_data
    DROP CONSTRAINT IF EXISTS unique_user_checklist;

ALTER TABLE sodagent.user_checklist_data
    ALTER COLUMN store_id SET NOT NULL;

ALTER TABLE sodagent.user_checklist_data
    ADD CONSTRAINT unique_user_checklist_per_period_store UNIQUE (filled_for_period, organisation_checklist_id, store_id);
