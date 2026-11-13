-- Accounts of the archive (docs/domain/users.md).
-- The email is kept in lower case; the user name is unique whatever its letter case.
CREATE TABLE app_user (
    id                      uuid PRIMARY KEY,
    name                    varchar(64)  NOT NULL,
    email                   varchar(254) NOT NULL,
    password_hash           varchar(100) NOT NULL,
    registered_at           timestamptz  NOT NULL,
    status                  varchar(16)  NOT NULL,
    confirmation_digest     varchar(64),
    confirmation_expires_at timestamptz
);

CREATE UNIQUE INDEX app_user_name_key ON app_user (lower(name));
CREATE UNIQUE INDEX app_user_email_key ON app_user (email);
CREATE UNIQUE INDEX app_user_confirmation_key ON app_user (confirmation_digest);

CREATE TABLE app_user_group (
    user_id    uuid        NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    user_group varchar(32) NOT NULL,
    PRIMARY KEY (user_id, user_group)
);

-- A session is known by the digest of its refresh secret, never by the secret itself.
CREATE TABLE user_session (
    digest     varchar(64) PRIMARY KEY,
    user_id    uuid        NOT NULL REFERENCES app_user (id) ON DELETE CASCADE,
    expires_at timestamptz NOT NULL,
    remember   boolean     NOT NULL
);

CREATE INDEX user_session_user_idx ON user_session (user_id);
