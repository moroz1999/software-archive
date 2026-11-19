-- The whole Vector family as the card index "Базис" knows it (docs/domain/hardware.md): its computers, storage, sound,
-- controls and the Z80 processor.
INSERT INTO hardware (code, hardware_group)
VALUES ('pk6128c', 'computers'),
       ('start1200', 'computers'),
       ('hdd', 'storage'),
       ('kvaz', 'storage'),
       ('barkar', 'storage'),
       ('beeper', 'sound'),
       ('vi53', 'sound'),
       ('joystick-p', 'controls'),
       ('joystick-s', 'controls'),
       ('z80', 'expansion');

INSERT INTO hardware_name (hardware_code, language, name)
VALUES ('pk6128c', 'en', 'PK-6128C'),
       ('pk6128c', 'ru', 'ПК-6128Ц'),
       ('start1200', 'en', 'Vector Start-1200'),
       ('start1200', 'ru', 'Вектор Старт-1200'),
       ('hdd', 'en', 'Hard disk'),
       ('hdd', 'ru', 'Жёсткий диск'),
       ('kvaz', 'en', 'Quasi-disk'),
       ('kvaz', 'ru', 'Квазидиск'),
       ('barkar', 'en', 'Barkar''s quasi-disk'),
       ('barkar', 'ru', 'Квазидиск Баркаря'),
       ('beeper', 'en', 'Beeper'),
       ('beeper', 'ru', 'Бипер'),
       ('vi53', 'en', 'VI53 timer sound'),
       ('vi53', 'ru', 'Звук таймера ВИ53'),
       ('joystick-p', 'en', 'Joystick P'),
       ('joystick-p', 'ru', 'Джойстик П'),
       ('joystick-s', 'en', 'Joystick S'),
       ('joystick-s', 'ru', 'Джойстик С'),
       ('z80', 'en', 'Z80 processor'),
       ('z80', 'ru', 'Процессор Z80');

INSERT INTO hardware_platform (hardware_code, platform_code)
VALUES ('pk6128c', 'vector'),
       ('start1200', 'vector'),
       ('hdd', 'vector'),
       ('kvaz', 'vector'),
       ('barkar', 'vector'),
       ('beeper', 'vector'),
       ('vi53', 'vector'),
       ('joystick-p', 'vector'),
       ('joystick-s', 'vector'),
       ('z80', 'vector');
