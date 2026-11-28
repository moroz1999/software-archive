-- The BK family (docs/domain/hardware.md): its computers, the SMK-512 controller, and BASIC and FOCAL, the
-- interpreters in the ROM of the BK, in a group of their own.
-- After the groups there are: an administrator may have added some of their own.
INSERT INTO hardware_group (code, position)
SELECT 'firmware', COALESCE(MAX(position), 0) + 1
FROM hardware_group;

INSERT INTO hardware_group_name (group_code, language, name)
VALUES ('firmware', 'en', 'ROM firmware'),
       ('firmware', 'ru', 'Прошивка ПЗУ');

INSERT INTO hardware (code, hardware_group)
VALUES ('bk0010', 'computers'),
       ('bk0010-01', 'computers'),
       ('bk0011', 'computers'),
       ('bk0011m', 'computers'),
       ('azbk', 'computers'),
       ('smk512', 'expansion'),
       ('basic', 'firmware'),
       ('focal', 'firmware');

INSERT INTO hardware_name (hardware_code, language, name)
VALUES ('bk0010', 'en', 'BK-0010'),
       ('bk0010', 'ru', 'БК-0010'),
       ('bk0010-01', 'en', 'BK-0010-01'),
       ('bk0010-01', 'ru', 'БК-0010-01'),
       ('bk0011', 'en', 'BK-0011'),
       ('bk0011', 'ru', 'БК-0011'),
       ('bk0011m', 'en', 'BK-0011M'),
       ('bk0011m', 'ru', 'БК-0011М'),
       ('azbk', 'en', 'AZBK'),
       ('azbk', 'ru', 'AZБК'),
       ('smk512', 'en', 'SMK-512'),
       ('smk512', 'ru', 'СМК-512'),
       ('basic', 'en', 'BASIC'),
       ('basic', 'ru', 'Бейсик'),
       ('focal', 'en', 'FOCAL'),
       ('focal', 'ru', 'Фокал');

INSERT INTO hardware_platform (hardware_code, platform_code)
VALUES ('bk0010', 'bk'),
       ('bk0010-01', 'bk'),
       ('bk0011', 'bk'),
       ('bk0011m', 'bk'),
       ('azbk', 'bk'),
       ('smk512', 'bk'),
       ('basic', 'bk'),
       ('focal', 'bk');
