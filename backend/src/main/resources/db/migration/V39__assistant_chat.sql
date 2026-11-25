-- The chat of editors with the AI assistant (docs/features/ai-assistant.md#chat).
-- A conversation is its owner's: deleting the account deletes their conversations, with all they hold.
CREATE TABLE assistant_conversation (
    id              uuid PRIMARY KEY,
    owner_id        uuid        NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    title           text,
    created_at      timestamptz NOT NULL,
    last_message_at timestamptz NOT NULL,
    -- The seq of its last message: the next one is given under the lock of this row.
    last_seq        bigint      NOT NULL DEFAULT 0
);

CREATE INDEX assistant_conversation_owner ON assistant_conversation (owner_id, last_message_at DESC);

CREATE TABLE assistant_message (
    conversation_id uuid        NOT NULL REFERENCES assistant_conversation (id) ON DELETE CASCADE,
    seq             bigint      NOT NULL,
    role            varchar(16) NOT NULL,
    kind            varchar(16) NOT NULL,
    text            text        NOT NULL,
    payload         jsonb,
    created_at      timestamptz NOT NULL,
    PRIMARY KEY (conversation_id, seq)
);

-- Its changes are one JSON array: a plan is read and written whole.
CREATE TABLE assistant_plan (
    id              uuid PRIMARY KEY,
    conversation_id uuid        NOT NULL REFERENCES assistant_conversation (id) ON DELETE CASCADE,
    title           text        NOT NULL,
    status          varchar(16) NOT NULL,
    proposed_at     timestamptz NOT NULL,
    finished_at     timestamptz,
    actions         jsonb       NOT NULL
);

CREATE INDEX assistant_plan_conversation ON assistant_plan (conversation_id, proposed_at);

CREATE TABLE assistant_run (
    id              uuid PRIMARY KEY,
    conversation_id uuid        NOT NULL REFERENCES assistant_conversation (id) ON DELETE CASCADE,
    type            varchar(16) NOT NULL,
    plan_id         uuid REFERENCES assistant_plan (id) ON DELETE CASCADE,
    status          varchar(16) NOT NULL,
    stop            varchar(16) NOT NULL,
    queued_at       timestamptz NOT NULL,
    steps           integer     NOT NULL,
    tokens_in       bigint      NOT NULL,
    tokens_out      bigint      NOT NULL,
    error           text,
    finished_at     timestamptz
);

CREATE INDEX assistant_run_active ON assistant_run (conversation_id) WHERE status IN ('QUEUED', 'RUNNING');
