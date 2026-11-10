-- Seven more platforms, each with its models and clones as its computers (docs/domain/hardware.md#platform).
INSERT INTO platform (code, name)
VALUES ('orion', 'Orion'),
       ('pk8000', 'PK8000'),
       ('korvet', 'Korvet'),
       ('lviv', 'Lviv'),
       ('okean', 'Okean'),
       ('bashkiria', 'Bashkiria'),
       ('xt', 'XT clones');

INSERT INTO hardware (code, name, hardware_group)
VALUES ('orion128', 'Orion-128', 'computers'),
       ('orion-pro', 'Orion-PRO', 'computers'),
       ('pk8000-vesta', 'PK8000 Vesta', 'computers'),
       ('pk8000-hobby', 'PK8000 Hobby', 'computers'),
       ('pk8010', 'PK8010', 'computers'),
       ('pk8020', 'PK8020', 'computers'),
       ('lviv-pk01', 'PK-01 Lviv', 'computers'),
       ('okean240', 'Okean-240', 'computers'),
       ('bashkiria2m', 'Bashkiria-2M', 'computers'),
       ('poisk', 'Poisk', 'computers'),
       ('es1840', 'ES-1840', 'computers'),
       ('iskra1030', 'Iskra-1030', 'computers'),
       ('neuron-i966', 'Neuron I9.66', 'computers');

INSERT INTO hardware_platform (hardware_code, platform_code)
VALUES ('orion128', 'orion'),
       ('orion-pro', 'orion'),
       ('pk8000-vesta', 'pk8000'),
       ('pk8000-hobby', 'pk8000'),
       ('pk8010', 'korvet'),
       ('pk8020', 'korvet'),
       ('lviv-pk01', 'lviv'),
       ('okean240', 'okean'),
       ('bashkiria2m', 'bashkiria'),
       ('poisk', 'xt'),
       ('es1840', 'xt'),
       ('iskra1030', 'xt'),
       ('neuron-i966', 'xt');

-- Every platform has tape, floppy disks, a beeper and a joystick.
INSERT INTO hardware_platform (hardware_code, platform_code)
SELECT h.code, p.code
FROM hardware h,
     platform p
WHERE h.code IN ('tape', 'floppy', 'beeper', 'joystick')
  AND p.code IN ('orion', 'pk8000', 'korvet', 'lviv', 'okean', 'bashkiria', 'xt');
