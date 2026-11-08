-- E2E fixture data set for software. Stable ids: tests may reference them.
INSERT INTO software (id, title, description, release_date)
VALUES ('00000000-0000-0000-0001-000000000001', 'Fixture Software Alpha', 'Fixture software with a release date.', '1985-03-02'),
       ('00000000-0000-0000-0001-000000000002', 'Fixture Software Beta', 'Fixture software without a release date.', NULL),
       ('00000000-0000-0000-0001-000000000003', 'Fixture Software Gamma', NULL, '1991-12-24')
ON CONFLICT (id) DO UPDATE
    SET title        = EXCLUDED.title,
        description  = EXCLUDED.description,
        release_date = EXCLUDED.release_date;
