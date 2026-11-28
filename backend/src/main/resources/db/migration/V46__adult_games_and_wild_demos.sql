-- Two categories the games and the demos of bk-catalog need (docs/domain/categories.md): Adult under Games and Wild
-- under Demoscene > Demo, each last among its siblings. A parent an administrator deleted since gets nothing.
INSERT INTO category (id, parent_id, position)
SELECT new.id, new.parent_id, coalesce((SELECT max(c.position) + 1 FROM category c WHERE c.parent_id = new.parent_id), 0)
FROM (VALUES ('c3d2848f-3803-5b6b-b52d-102ebd3fa715'::uuid, '081bf3a1-782b-5a8b-8d12-3b155e0a58cb'::uuid),
             ('64ad90af-cf42-59c0-ad1f-52500c3ec926'::uuid, '936b5821-6c89-54c5-93f0-a21ffb1580b3'::uuid))
         AS new (id, parent_id)
WHERE EXISTS (SELECT 1 FROM category p WHERE p.id = new.parent_id)
ON CONFLICT (id) DO NOTHING;

INSERT INTO category_title (category_id, language, title)
SELECT title.category_id, title.language, title.title
FROM (VALUES ('c3d2848f-3803-5b6b-b52d-102ebd3fa715'::uuid, 'en', 'Adult'),
             ('c3d2848f-3803-5b6b-b52d-102ebd3fa715'::uuid, 'ru', 'Для взрослых'),
             ('64ad90af-cf42-59c0-ad1f-52500c3ec926'::uuid, 'en', 'Wild'),
             ('64ad90af-cf42-59c0-ad1f-52500c3ec926'::uuid, 'ru', 'Wild'))
         AS title (category_id, language, title)
WHERE EXISTS (SELECT 1 FROM category c WHERE c.id = title.category_id)
ON CONFLICT (category_id, language) DO NOTHING;
