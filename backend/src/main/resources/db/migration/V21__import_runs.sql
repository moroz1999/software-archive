-- A run of an import with its report: what it imported, how far it got, and what each program came to.
CREATE TABLE import_run (
    id         uuid         PRIMARY KEY,
    archive    varchar(64)  NOT NULL,
    section    varchar(255),
    state      varchar(16)  NOT NULL,
    started_at timestamptz  NOT NULL,
    ended_at   timestamptz,
    added      integer      NOT NULL,
    known      integer      NOT NULL,
    skipped    integer      NOT NULL,
    failed     integer      NOT NULL
);

CREATE INDEX import_run_latest ON import_run (started_at DESC);

-- The software a run added: no reference to it, as the report outlives software deleted since.
CREATE TABLE import_run_added (
    run_id      uuid         NOT NULL REFERENCES import_run (id) ON DELETE CASCADE,
    position    integer      NOT NULL,
    external_id varchar(255) NOT NULL,
    software_id uuid         NOT NULL,
    title       varchar(255) NOT NULL,
    PRIMARY KEY (run_id, position)
);

CREATE TABLE import_run_failed (
    run_id      uuid          NOT NULL REFERENCES import_run (id) ON DELETE CASCADE,
    position    integer       NOT NULL,
    external_id varchar(255)  NOT NULL,
    url         varchar(2048) NOT NULL,
    reason      varchar(1000) NOT NULL,
    PRIMARY KEY (run_id, position)
);
