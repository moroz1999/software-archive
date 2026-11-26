-- E2E fixture votes (docs/domain/voting.md). The ratings are what the rule makes of the votes with the average vote 3.8:
-- Elite has 5, 5 and 4: (14 + 3.8 × 10) / (3 + 10) = 4;
-- Khryukostyag has a 3 and a vote withdrawn: (3 + 3.8 × 10) / (1 + 10).

INSERT INTO software_vote (software_id, user_id, value, cast_at)
VALUES ('00000000-0000-0000-0001-000000000001', '00000000-0000-0000-0009-000000000001', 5, '2026-11-20T10:00:00Z'),
       ('00000000-0000-0000-0001-000000000001', '00000000-0000-0000-0009-000000000003', 5, '2026-11-21T10:00:00Z'),
       ('00000000-0000-0000-0001-000000000001', '00000000-0000-0000-0009-000000000002', 4, '2026-11-22T10:00:00Z'),
       ('00000000-0000-0000-0001-000000000015', '00000000-0000-0000-0009-000000000003', 3, '2026-11-23T10:00:00Z'),
       ('00000000-0000-0000-0001-000000000015', '00000000-0000-0000-0009-000000000002', 0, '2026-11-24T10:00:00Z')
ON CONFLICT (software_id, user_id) DO UPDATE SET value = EXCLUDED.value, cast_at = EXCLUDED.cast_at;

INSERT INTO software_rating (software_id, rating, votes_amount)
VALUES ('00000000-0000-0000-0001-000000000001', 4, 3),
       ('00000000-0000-0000-0001-000000000015', 41.0 / 11, 1)
ON CONFLICT (software_id) DO UPDATE SET rating = EXCLUDED.rating, votes_amount = EXCLUDED.votes_amount;
