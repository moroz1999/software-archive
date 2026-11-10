-- RK-compatible machines: Radio-86RK, Mikrosha, Apogey and Partner are one platform, each machine its own hardware
-- (docs/domain/hardware.md#the-rk-family).
INSERT INTO platform (code, name)
VALUES ('rk', 'RK-compatible');

INSERT INTO hardware (code, name, hardware_group)
VALUES ('radio86rk-16k', 'Radio-86RK 16K', 'computers'),
       ('radio86rk-32k', 'Radio-86RK 32K', 'computers'),
       ('mikrosha', 'Mikrosha', 'computers'),
       ('apogey-bk01', 'Apogey BK-01', 'computers'),
       ('apogey-bk01c', 'Apogey BK-01C', 'computers'),
       ('partner', 'Partner 01.01', 'computers'),
       ('vi53', 'KR580VI53 timer', 'sound'),
       ('partner-mcpg', 'Partner MCPG colour module', 'expansion');

INSERT INTO hardware_platform (hardware_code, platform_code)
VALUES ('radio86rk-16k', 'rk'),
       ('radio86rk-32k', 'rk'),
       ('mikrosha', 'rk'),
       ('apogey-bk01', 'rk'),
       ('apogey-bk01c', 'rk'),
       ('partner', 'rk'),
       ('vi53', 'rk'),
       ('partner-mcpg', 'rk'),
       ('tape', 'rk'),
       ('floppy', 'rk'),
       ('beeper', 'rk'),
       ('joystick', 'rk');
