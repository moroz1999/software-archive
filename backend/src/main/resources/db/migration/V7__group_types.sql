-- The reference book of group types (docs/domain/authors.md#groups).
INSERT INTO group_type (code)
VALUES ('demogroup'),
       ('crack group'),
       ('game studio'),
       ('label') ON CONFLICT DO NOTHING;
