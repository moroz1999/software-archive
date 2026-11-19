-- Only confirmed hardware stays: the Vector family has the Vector-06C and the Krista-2, tape, the floppy disk and the AY;
-- every other platform is a planned family without hardware yet (docs/domain/hardware.md).
INSERT INTO hardware (code, hardware_group)
VALUES ('krista2', 'computers');
INSERT INTO hardware_name (hardware_code, language, name)
VALUES ('krista2', 'en', 'Krista-2'),
       ('krista2', 'ru', 'Криста-2');

DELETE FROM software_hardware
WHERE hardware_code NOT IN ('vector06c', 'krista2', 'tape', 'floppy', 'ay8910');
DELETE FROM software_release_hardware
WHERE hardware_code NOT IN ('vector06c', 'krista2', 'tape', 'floppy', 'ay8910');
DELETE FROM hardware
WHERE code NOT IN ('vector06c', 'krista2', 'tape', 'floppy', 'ay8910');
DELETE FROM hardware_platform
WHERE platform_code <> 'vector';
INSERT INTO hardware_platform (hardware_code, platform_code)
VALUES ('krista2', 'vector')
ON CONFLICT DO NOTHING;

-- A city whose country is not known belongs to the country "Unknown" until it is sorted out by hand
-- (docs/domain/geo.md).
INSERT INTO country (id)
VALUES ('00000000-0000-0000-0000-00000000c001');
INSERT INTO country_name (country_id, language, name)
VALUES ('00000000-0000-0000-0000-00000000c001', 'en', 'Unknown'),
       ('00000000-0000-0000-0000-00000000c001', 'ru', 'Неизвестно');

-- A screenshot keeps the name of its original file and its size (docs/domain/imports.md#software). The screenshots
-- stored so far came from the import, which takes them again with what it knows of them.
DELETE FROM software_screenshot;
DELETE FROM software_release_screenshot;
ALTER TABLE software_screenshot
    ADD COLUMN file_name TEXT   NOT NULL,
    ADD COLUMN size      BIGINT NOT NULL;
ALTER TABLE software_release_screenshot
    ADD COLUMN file_name TEXT   NOT NULL,
    ADD COLUMN size      BIGINT NOT NULL;
