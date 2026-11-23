-- The groups of hardware are a reference book of their own, kept by administrators: each has a code, a name in Russian
-- and in other languages, and its place in the order they are shown (docs/domain/hardware.md#hardware).
CREATE TABLE hardware_group
(
    code     TEXT PRIMARY KEY,
    position INT NOT NULL UNIQUE
);

CREATE TABLE hardware_group_name
(
    group_code TEXT NOT NULL REFERENCES hardware_group (code) ON DELETE CASCADE,
    language   TEXT NOT NULL REFERENCES language (code),
    name       TEXT NOT NULL,
    PRIMARY KEY (group_code, language)
);

-- The six groups so far, in the order they have been shown, named as the interface named them.
INSERT INTO hardware_group (code, position)
VALUES ('computers', 1),
       ('storage', 2),
       ('dos', 3),
       ('sound', 4),
       ('controls', 5),
       ('expansion', 6);

INSERT INTO hardware_group_name (group_code, language, name)
VALUES ('computers', 'en', 'Computers'),
       ('computers', 'ru', 'Компьютеры'),
       ('storage', 'en', 'Storage'),
       ('storage', 'ru', 'Носители'),
       ('dos', 'en', 'DOS'),
       ('dos', 'ru', 'DOS'),
       ('sound', 'en', 'Sound'),
       ('sound', 'ru', 'Звук'),
       ('controls', 'en', 'Controls'),
       ('controls', 'ru', 'Управление'),
       ('expansion', 'en', 'Expansion'),
       ('expansion', 'ru', 'Расширения');

ALTER TABLE hardware
    DROP CONSTRAINT hardware_group_check;
ALTER TABLE hardware
    ADD CONSTRAINT hardware_group_fkey FOREIGN KEY (hardware_group) REFERENCES hardware_group (code);
