-- Deleting software removes everything that belongs to it.
ALTER TABLE software_alternative_titles
    DROP CONSTRAINT software_alternative_titles_software_id_fk,
    ADD CONSTRAINT software_alternative_titles_software_id_fk
        FOREIGN KEY (software_id) REFERENCES software (id) ON DELETE CASCADE;

ALTER TABLE software_languages
    DROP CONSTRAINT software_languages_software_id_fkey,
    ADD CONSTRAINT software_languages_software_id_fkey
        FOREIGN KEY (software_id) REFERENCES software (id) ON DELETE CASCADE;

ALTER TABLE software_links
    DROP CONSTRAINT software_links_software_id_fkey,
    ADD CONSTRAINT software_links_software_id_fkey
        FOREIGN KEY (software_id) REFERENCES software (id) ON DELETE CASCADE;
