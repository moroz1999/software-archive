-- Whatever an import added, by where it came from (docs/domain/imports.md): the next run adds only what is new.
-- The entry is software or a label, as the kind tells; deleting it in the archive leaves its origin, so an import
-- never brings back what the archive took out.
CREATE TABLE external_origin (
    origin      varchar(64)  NOT NULL,
    kind        varchar(16)  NOT NULL,
    external_id varchar(255) NOT NULL,
    entity_id   uuid         NOT NULL,
    PRIMARY KEY (origin, kind, external_id)
);

-- The fixed user who adds whatever an import adds. Nobody signs in as them: no password matches the hash.
INSERT INTO app_user (id, name, email, password_hash, registered_at, status)
VALUES ('00000000-0000-0000-0000-00000000a001', 'importer', 'importer@archive.invalid', '!', now(), 'CONFIRMED');

INSERT INTO app_user_group (user_id, user_group)
VALUES ('00000000-0000-0000-0000-00000000a001', 'member');
