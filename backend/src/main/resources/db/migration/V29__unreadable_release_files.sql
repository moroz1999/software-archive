-- A release file that could not be parsed whole keeps no contents and is marked unreadable
-- (docs/domain/software.md#releases).
ALTER TABLE release_file
    ADD COLUMN unreadable BOOLEAN NOT NULL DEFAULT FALSE;
