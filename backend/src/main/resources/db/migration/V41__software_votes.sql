-- The votes of users on software and the rating they give it (docs/domain/voting.md).
-- A withdrawn vote stays with the value 0 and counts for nothing.
CREATE TABLE software_vote (
    software_id uuid        NOT NULL REFERENCES software (id) ON DELETE CASCADE,
    user_id     uuid        NOT NULL REFERENCES app_user (id),
    value       smallint    NOT NULL CHECK (value BETWEEN 0 AND 5),
    cast_at     timestamptz NOT NULL,
    PRIMARY KEY (software_id, user_id)
);

CREATE INDEX software_vote_user_idx ON software_vote (user_id);

-- Software nobody voted on has no row: its rating is 0 with no votes.
CREATE TABLE software_rating (
    software_id  uuid             PRIMARY KEY REFERENCES software (id) ON DELETE CASCADE,
    rating       double precision NOT NULL,
    votes_amount integer          NOT NULL
);
