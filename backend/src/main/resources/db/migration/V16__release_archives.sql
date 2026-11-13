ALTER TABLE software_release
    ADD COLUMN archive_id UUID,
    ADD COLUMN archive_file_name TEXT,
    ADD COLUMN archive_size BIGINT,
    ADD CONSTRAINT software_release_archive_complete CHECK (
        (archive_id IS NULL AND archive_file_name IS NULL AND archive_size IS NULL)
        OR (archive_id IS NOT NULL AND archive_file_name IS NOT NULL AND archive_size BETWEEN 0 AND 104857600)
    );

CREATE UNIQUE INDEX software_release_archive_id_uq ON software_release (archive_id) WHERE archive_id IS NOT NULL;
