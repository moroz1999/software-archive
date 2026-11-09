-- Platforms and the hardware reference book are reference data of the archive (docs/domain/hardware.md).
CREATE TABLE platform
(
    code TEXT PRIMARY KEY,
    name TEXT NOT NULL
);

INSERT INTO platform (code, name)
VALUES ('vector', 'Vector'),
       ('bk', 'BK'),
       ('agat', 'Agat'),
       ('msx', 'MSX'),
       ('specialist', 'Specialist');

CREATE TABLE hardware
(
    code           TEXT PRIMARY KEY,
    name           TEXT NOT NULL,
    hardware_group TEXT NOT NULL
        CONSTRAINT hardware_group_check
            CHECK (hardware_group IN ('computers', 'storage', 'dos', 'sound', 'controls', 'expansion'))
);

-- One item can belong to several platforms.
CREATE TABLE hardware_platform
(
    hardware_code TEXT REFERENCES hardware (code) ON DELETE CASCADE,
    platform_code TEXT REFERENCES platform (code) ON DELETE CASCADE,
    PRIMARY KEY (hardware_code, platform_code)
);

INSERT INTO hardware (code, name, hardware_group)
VALUES ('vector06c', 'Vector-06C', 'computers'),
       ('bk0010-01', 'BK-0010-01', 'computers'),
       ('bk0011m', 'BK-0011M', 'computers'),
       ('agat7', 'Agat-7', 'computers'),
       ('agat9', 'Agat-9', 'computers'),
       ('msx1', 'MSX1', 'computers'),
       ('msx2', 'MSX2', 'computers'),
       ('msx2plus', 'MSX2+', 'computers'),
       ('specialist', 'Specialist', 'computers'),
       ('specialist-m', 'Specialist-M', 'computers'),
       ('specialist-mx', 'Specialist-MX', 'computers'),
       ('tape', 'Tape', 'storage'),
       ('floppy', 'Floppy disk', 'storage'),
       ('cartridge', 'Cartridge', 'storage'),
       ('microdos', 'MicroDOS', 'dos'),
       ('andos', 'ANDOS', 'dos'),
       ('msxdos', 'MSX-DOS', 'dos'),
       ('beeper', 'Beeper', 'sound'),
       ('ay8910', 'AY-3-8910', 'sound'),
       ('ym2413', 'YM2413', 'sound'),
       ('scc', 'Konami SCC', 'sound'),
       ('joystick', 'Joystick', 'controls'),
       ('mouse', 'Mouse', 'controls'),
       ('quasidisk', 'Quasi-disk', 'expansion');

INSERT INTO hardware_platform (hardware_code, platform_code)
VALUES ('vector06c', 'vector'),
       ('bk0010-01', 'bk'),
       ('bk0011m', 'bk'),
       ('agat7', 'agat'),
       ('agat9', 'agat'),
       ('msx1', 'msx'),
       ('msx2', 'msx'),
       ('msx2plus', 'msx'),
       ('specialist', 'specialist'),
       ('specialist-m', 'specialist'),
       ('specialist-mx', 'specialist'),
       ('cartridge', 'msx'),
       ('microdos', 'vector'),
       ('andos', 'bk'),
       ('msxdos', 'msx'),
       ('ay8910', 'vector'),
       ('ay8910', 'specialist'),
       ('ay8910', 'msx'),
       ('ym2413', 'msx'),
       ('scc', 'msx'),
       ('mouse', 'msx'),
       ('quasidisk', 'vector');

-- Every platform has tape, floppy disks, a beeper and a joystick.
INSERT INTO hardware_platform (hardware_code, platform_code)
SELECT h.code, p.code
FROM hardware h,
     platform p
WHERE h.code IN ('tape', 'floppy', 'beeper', 'joystick');

-- Software is for exactly one platform; what exists so far is for Vector.
ALTER TABLE software
    ADD COLUMN platform_code TEXT REFERENCES platform (code);
UPDATE software
SET platform_code = 'vector';
ALTER TABLE software
    ALTER COLUMN platform_code SET NOT NULL;

-- The base hardware of software.
CREATE TABLE software_hardware
(
    software_id   UUID REFERENCES software (id) ON DELETE CASCADE,
    hardware_code TEXT REFERENCES hardware (code),
    PRIMARY KEY (software_id, hardware_code)
);

-- A release never lives apart from its software (docs/domain/software.md).
CREATE TABLE software_release
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    software_id  UUID NOT NULL REFERENCES software (id) ON DELETE CASCADE,
    title        TEXT,
    version      TEXT,
    type         TEXT NOT NULL DEFAULT 'unknown'
        CONSTRAINT software_release_type_check
            CHECK (type IN ('original', 'rerelease', 'adaptation', 'localization', 'bugfix', 'mod', 'crack', 'mia',
                            'corrupted', 'compilation', 'incomplete', 'demoversion', 'unknown')),
    release_date DATE,
    description  TEXT
);

-- The hardware of a release: every group it has replaces that group of its software.
CREATE TABLE software_release_hardware
(
    release_id    UUID REFERENCES software_release (id) ON DELETE CASCADE,
    hardware_code TEXT REFERENCES hardware (code),
    PRIMARY KEY (release_id, hardware_code)
);
