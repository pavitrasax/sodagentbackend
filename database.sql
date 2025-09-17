CREATE SCHEMA sodagent;
SET search_path TO sodagent;

--------------------------------------------------------------------------
-- Table: sodagent.organisation_checklist
-- DROP TABLE IF EXISTS sodagent.organisation_checklist;
--------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS sodagent.organisation_checklist
(
    id SERIAL PRIMARY KEY,
    org_id integer NOT NULL,
    version integer NOT NULL,
    checklist_json jsonb NOT NULL,
    created_at timestamp without time zone NOT NULL DEFAULT now(),
    is_active boolean NOT NULL DEFAULT false,
    status integer DEFAULT 0,
    agent_code character varying(3) COLLATE pg_catalog."default",
    CONSTRAINT unique_org_version UNIQUE (org_id, version)
);

--------------------------------------------------------------------------
-- Table: sodagent.rollout_users
-- DROP TABLE IF EXISTS sodagent.rollout_users;
--------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS sodagent.rollout_users
(
    id SERIAL PRIMARY KEY,
    name character varying(255) COLLATE pg_catalog."default",
    mobile_number character varying(255) COLLATE pg_catalog."default" NOT NULL,
    organisation_id integer NOT NULL,
    created_at timestamp without time zone NOT NULL DEFAULT CURRENT_TIMESTAMP
);

--------------------------------------------------------------------------
-- Table: sodagent.user_checklist_data
-- DROP TABLE IF EXISTS sodagent.user_checklist_data;
--------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS sodagent.user_checklist_data
(
    id SERIAL PRIMARY KEY,
    user_credential character varying(100) COLLATE pg_catalog."default" NOT NULL,
    user_credential_type character varying(10) COLLATE pg_catalog."default" NOT NULL,
    filled_for_period character varying(20) COLLATE pg_catalog."default" NOT NULL,
    organisation_checklist_id bigint NOT NULL,
    data_json jsonb NOT NULL,
    created_at timestamp without time zone NOT NULL DEFAULT now(),
    filled_for_period_ts timestamp without time zone,
    compliance_score numeric(5,2),
    max_compliance_score numeric(5,2),
    compliance_feedback jsonb,
    CONSTRAINT unique_user_checklist UNIQUE (user_credential, user_credential_type, filled_for_period, organisation_checklist_id),
    CONSTRAINT fk_organisation_checklist FOREIGN KEY (organisation_checklist_id)
        REFERENCES sodagent.organisation_checklist (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);

--------------------------------------------------------------------------
-- Table: sodagent.vm_ideal_data
-- DROP TABLE IF EXISTS sodagent.vm_ideal_data;
--------------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS sodagent.vm_ideal_data
(
    id SERIAL PRIMARY KEY,
    user_credential character varying(100) COLLATE pg_catalog."default" NOT NULL,
    user_credential_type character varying(10) COLLATE pg_catalog."default" NOT NULL,
    filled_for_period character varying(20) COLLATE pg_catalog."default" NOT NULL,
    organisation_checklist_id bigint NOT NULL,
    data_json jsonb NOT NULL,
    created_at timestamp without time zone NOT NULL DEFAULT now(),
    CONSTRAINT unique_admin_period_checklist UNIQUE (user_credential, user_credential_type, filled_for_period, organisation_checklist_id),
    CONSTRAINT fk_organisation_checklist FOREIGN KEY (organisation_checklist_id)
        REFERENCES sodagent.organisation_checklist (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
);
