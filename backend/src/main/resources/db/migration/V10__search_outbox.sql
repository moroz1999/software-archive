-- The entities whose search documents wait to be brought up to date (docs/features/search.md#synchronization).
-- One row per entity: a new change renews the row (a new renewal number, due at once), so the worker
-- acknowledges a task only while its row was not renewed during the processing.
CREATE SEQUENCE search_outbox_renewal;

CREATE TABLE search_outbox
(
    entity_type     TEXT        NOT NULL,
    entity_id       UUID        NOT NULL,
    renewal         BIGINT      NOT NULL DEFAULT nextval('search_outbox_renewal'),
    attempts        INT         NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (entity_type, entity_id)
);

CREATE INDEX search_outbox_next_attempt_at_idx ON search_outbox (next_attempt_at);
