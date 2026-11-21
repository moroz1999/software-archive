-- The report of a run tells where the run is and, for each program, more of what it came to (docs/domain/imports.md).
-- Runs before it recorded none of this: their lists stay empty, their counts as they were.
ALTER TABLE import_run
    ADD COLUMN progress_position integer       NOT NULL DEFAULT 0,
    ADD COLUMN progress_total    integer,
    ADD COLUMN progress_url      varchar(2048);

-- The software a run found in the archive and what it added to it, the parts by their codes joined by commas.
CREATE TABLE import_run_known (
    run_id      uuid         NOT NULL REFERENCES import_run (id) ON DELETE CASCADE,
    position    integer      NOT NULL,
    external_id varchar(255) NOT NULL,
    software_id uuid         NOT NULL,
    title       varchar(255) NOT NULL,
    supplements varchar(255) NOT NULL,
    PRIMARY KEY (run_id, position)
);

-- The programs a run left out, by the section or the category of the source named in detail.
CREATE TABLE import_run_skipped (
    run_id      uuid          NOT NULL REFERENCES import_run (id) ON DELETE CASCADE,
    position    integer       NOT NULL,
    external_id varchar(255)  NOT NULL,
    url         varchar(2048) NOT NULL,
    title       varchar(255)  NOT NULL,
    reason      varchar(16)   NOT NULL,
    detail      varchar(255)  NOT NULL,
    PRIMARY KEY (run_id, position)
);

-- The authors a run took for the first time: created, or found among those of the archive. No reference to them, as
-- the report outlives authors deleted since.
CREATE TABLE import_run_author (
    run_id    uuid         NOT NULL REFERENCES import_run (id) ON DELETE CASCADE,
    position  integer      NOT NULL,
    kind      varchar(16)  NOT NULL,
    entity_id uuid         NOT NULL,
    name      varchar(255) NOT NULL,
    created   boolean      NOT NULL,
    PRIMARY KEY (run_id, position)
);

-- The cities and countries a run added, under the names the source gave.
CREATE TABLE import_run_place (
    run_id   uuid         NOT NULL REFERENCES import_run (id) ON DELETE CASCADE,
    position integer      NOT NULL,
    kind     varchar(16)  NOT NULL,
    name     varchar(255) NOT NULL,
    PRIMARY KEY (run_id, position)
);
