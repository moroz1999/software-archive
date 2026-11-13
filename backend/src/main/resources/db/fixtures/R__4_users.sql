-- E2E fixture accounts: one of every user group, an account waiting for its link and an abandoned one.
-- Every password is "password1". Stable ids: tests may reference them.
-- The link of "Pending" is /verify-email?token=pending-fixture-token; it works for a week after the fixtures are applied.

INSERT INTO app_user (id, name, email, password_hash, registered_at, status, confirmation_digest, confirmation_expires_at)
VALUES ('00000000-0000-0000-0009-000000000001', 'Admin', 'admin@archive.test', '$2b$10$awezS..yZXtHnFe/vLFr0.3.lz5TiHRmFS6MARtDBzTkteyWGYlOy', '2026-01-01T00:00:00Z', 'CONFIRMED', NULL, NULL),
       ('00000000-0000-0000-0009-000000000002', 'Moderator', 'moderator@archive.test', '$2b$10$awezS..yZXtHnFe/vLFr0.3.lz5TiHRmFS6MARtDBzTkteyWGYlOy', '2026-01-01T00:00:00Z', 'CONFIRMED', NULL, NULL),
       ('00000000-0000-0000-0009-000000000003', 'Member', 'member@archive.test', '$2b$10$awezS..yZXtHnFe/vLFr0.3.lz5TiHRmFS6MARtDBzTkteyWGYlOy', '2026-01-01T00:00:00Z', 'CONFIRMED', NULL, NULL),
       ('00000000-0000-0000-0009-000000000004', 'Pending', 'pending@archive.test', '$2b$10$awezS..yZXtHnFe/vLFr0.3.lz5TiHRmFS6MARtDBzTkteyWGYlOy', now(), 'UNCONFIRMED',
        'd7d60d97a42fd29202ece4fe0efc39740e9b253d48303a5e4e80d8e4cec0f8d3', now() + interval '7 days'),
       ('00000000-0000-0000-0009-000000000005', 'Abandoned', 'abandoned@archive.test', '$2b$10$awezS..yZXtHnFe/vLFr0.3.lz5TiHRmFS6MARtDBzTkteyWGYlOy', now() - interval '10 days', 'UNCONFIRMED',
        '0000000000000000000000000000000000000000000000000000000000000005', now() - interval '3 days')
ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name,
                               email = EXCLUDED.email,
                               password_hash = EXCLUDED.password_hash,
                               registered_at = EXCLUDED.registered_at,
                               status = EXCLUDED.status,
                               confirmation_digest = EXCLUDED.confirmation_digest,
                               confirmation_expires_at = EXCLUDED.confirmation_expires_at;

DELETE FROM app_user_group WHERE user_id IN ('00000000-0000-0000-0009-000000000001', '00000000-0000-0000-0009-000000000002',
                                             '00000000-0000-0000-0009-000000000003', '00000000-0000-0000-0009-000000000004',
                                             '00000000-0000-0000-0009-000000000005');
INSERT INTO app_user_group (user_id, user_group)
VALUES ('00000000-0000-0000-0009-000000000001', 'admin'),
       ('00000000-0000-0000-0009-000000000001', 'member'),
       ('00000000-0000-0000-0009-000000000002', 'moderator'),
       ('00000000-0000-0000-0009-000000000002', 'member'),
       ('00000000-0000-0000-0009-000000000003', 'member');
