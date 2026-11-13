-- Who added each entry of the archive; empty for entries added before accounts, and once the account is gone.
ALTER TABLE software ADD COLUMN added_by uuid REFERENCES app_user (id) ON DELETE SET NULL;
ALTER TABLE software_release ADD COLUMN added_by uuid REFERENCES app_user (id) ON DELETE SET NULL;
ALTER TABLE person ADD COLUMN added_by uuid REFERENCES app_user (id) ON DELETE SET NULL;
ALTER TABLE "group" ADD COLUMN added_by uuid REFERENCES app_user (id) ON DELETE SET NULL;
