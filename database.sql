CREATE SCHEMA sodagent;
SET search_path TO sodagent;

CREATE TABLE organisation_checklist (
                                        id SERIAL PRIMARY KEY,
                                        org_id INTEGER NOT NULL,
                                        version INTEGER NOT NULL,
                                        checklist_json JSONB NOT NULL,
                                        created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                        is_active BOOLEAN NOT NULL DEFAULT FALSE,
                                        CONSTRAINT unique_org_version UNIQUE (org_id, version)
);

CREATE TABLE sodagent.user_checklist_data (
                                              id SERIAL PRIMARY KEY,
                                              user_credential VARCHAR(100) NOT NULL,
                                              user_credential_type VARCHAR(10) NOT NULL,
                                              filled_for_period VARCHAR(20) NOT NULL,
                                              organisation_checklist_id BIGINT NOT NULL,
                                              data_json JSONB NOT NULL,
                                              created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                              CONSTRAINT fk_organisation_checklist
                                                  FOREIGN KEY (organisation_checklist_id)
                                                      REFERENCES sodagent.organisation_checklist(id)
                                                      ON DELETE CASCADE,
                                              CONSTRAINT unique_user_checklist
                                                  UNIQUE (user_credential, user_credential_type, filled_for_period, organisation_checklist_id)
);


ALTER TABLE organisation_checklist
    ADD COLUMN status INT DEFAULT 0;