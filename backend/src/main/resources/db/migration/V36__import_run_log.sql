-- The log of a run: every step it took, in the order it went (docs/domain/imports.md). seq orders the lines of every
-- run, so a report read again asks only for the lines after the last one it has. What a line tells depends on its
-- kind: entry holds it as JSON.
CREATE TABLE import_run_log (
    seq    bigserial   PRIMARY KEY,
    run_id uuid        NOT NULL REFERENCES import_run (id) ON DELETE CASCADE,
    at     timestamptz,
    kind   varchar(16) NOT NULL,
    entry  jsonb       NOT NULL
);

CREATE INDEX import_run_log_run ON import_run_log (run_id, seq);

-- The reports of the runs before the log become their logs, group after group as the report listed them: in which
-- order the steps really went, and when, nobody wrote down, so the lines have no time. The reports stay where they are.
INSERT INTO import_run_log (run_id, at, kind, entry)
SELECT line.run_id, NULL, line.kind, line.entry
FROM (
    SELECT run_id, 1 AS part, position, 'ADDED' AS kind,
           jsonb_build_object('externalId', external_id, 'softwareId', software_id, 'title', title) AS entry
    FROM import_run_added
    UNION ALL
    SELECT run_id, 2, position, 'KNOWN',
           jsonb_build_object('externalId', external_id, 'softwareId', software_id, 'title', title,
                              'supplements', CASE WHEN supplements = '' THEN '[]'::jsonb
                                                  ELSE to_jsonb(string_to_array(supplements, ',')) END)
    FROM import_run_known
    UNION ALL
    SELECT run_id, 3, position, 'SKIPPED',
           jsonb_build_object('externalId', external_id, 'url', url, 'title', title, 'reason', reason,
                              'detail', detail)
    FROM import_run_skipped
    UNION ALL
    SELECT run_id, 4, position, 'FAILED',
           jsonb_build_object('externalId', external_id, 'url', url, 'reason', reason)
    FROM import_run_failed
    UNION ALL
    SELECT run_id, 5, position, 'AUTHOR',
           jsonb_build_object('kind', kind, 'entityId', entity_id, 'name', name, 'created', created)
    FROM import_run_author
    UNION ALL
    SELECT run_id, 6, position, 'PLACE', jsonb_build_object('kind', kind, 'name', name)
    FROM import_run_place
) line
JOIN import_run r ON r.id = line.run_id
ORDER BY r.started_at, r.id, line.part, line.position;
