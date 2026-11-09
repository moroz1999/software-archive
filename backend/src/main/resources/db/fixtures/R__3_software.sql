-- E2E fixture data set for software. Stable ids: tests may reference them.
INSERT INTO software (id, title, description, release_date)
VALUES ('00000000-0000-0000-0001-000000000001', 'Elite', 'Space trading and combat.', '1984-09-20'),
       ('00000000-0000-0000-0001-000000000002', 'Manic Miner', 'Software without a release date.', NULL),
       ('00000000-0000-0000-0001-000000000003', 'Knight Lore', NULL, '1984-11-01')
ON CONFLICT (id) DO UPDATE
    SET title        = EXCLUDED.title,
        description  = EXCLUDED.description,
        release_date = EXCLUDED.release_date;
