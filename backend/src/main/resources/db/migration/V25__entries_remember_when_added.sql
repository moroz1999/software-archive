-- Every entry of the archive remembers when it was added, to show it on its page (docs/domain/users.md#user-groups).
-- Releases from before get the moment of this migration: when they were added is not known.
ALTER TABLE software_release ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now();

ALTER TABLE software ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE person ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE "group" ALTER COLUMN created_at SET NOT NULL;
ALTER TABLE label ALTER COLUMN created_at SET NOT NULL;
