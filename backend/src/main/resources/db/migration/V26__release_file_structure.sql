-- A release file and everything it contains, ready for display and later use by emulators.
CREATE TABLE release_file
(
    id         UUID PRIMARY KEY,
    release_id UUID    NOT NULL REFERENCES software_release (id) ON DELETE CASCADE,
    parent_id  UUID,
    file_name  TEXT    NOT NULL CHECK (file_name <> ''),
    position   INTEGER NOT NULL CHECK (position >= 0),
    md5        CHAR(32),
    directory  BOOLEAN NOT NULL,
    executable BOOLEAN NOT NULL,
    CONSTRAINT release_file_content_check CHECK (
        (directory AND md5 IS NULL AND NOT executable)
        OR (NOT directory AND md5 IS NOT NULL)
    ),
    CONSTRAINT release_file_id_release_uq UNIQUE (id, release_id),
    CONSTRAINT release_file_parent_fk FOREIGN KEY (parent_id, release_id)
        REFERENCES release_file (id, release_id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX release_file_root_uq ON release_file (release_id) WHERE parent_id IS NULL;
CREATE UNIQUE INDEX release_file_position_uq ON release_file (release_id, parent_id, position)
    WHERE parent_id IS NOT NULL;
CREATE INDEX release_file_release_idx ON release_file (release_id);
