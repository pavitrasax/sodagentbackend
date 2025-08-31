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

CREATE TABLE sodagent.stores (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    store_code VARCHAR(50) ,
    address TEXT,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE sodagent.whatsapp_users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255),
    mobile_number VARCHAR(20) NOT NULL,
    store_id INTEGER,
    created_at TIMESTAMP DEFAULT NOW(),
    CONSTRAINT fk_store
        FOREIGN KEY (store_id)
        REFERENCES sodagent.stores(id)
        ON DELETE CASCADE
);

ALTER TABLE sodagent.whatsapp_users
ADD COLUMN organisation_id INTEGER;


ALTER TABLE organisation_checklist
    ADD COLUMN status INT DEFAULT 0;

    ALTER TABLE sodagent.user_checklist_data
    ADD COLUMN filled_for_period_ts timestamp;