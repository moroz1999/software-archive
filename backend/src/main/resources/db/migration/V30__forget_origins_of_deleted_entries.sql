-- Whatever an import added and the archive deleted since is forgotten with it (docs/domain/imports.md#origin):
-- origins still pointing to software, labels, people or groups no longer there go.
DELETE FROM external_origin o
WHERE (o.kind = 'SOFTWARE' AND NOT EXISTS (SELECT 1 FROM software s WHERE s.id = o.entity_id))
   OR (o.kind = 'LABEL' AND NOT EXISTS (SELECT 1 FROM label l WHERE l.id = o.entity_id))
   OR (o.kind = 'AUTHOR' AND NOT EXISTS (SELECT 1 FROM person p WHERE p.id = o.entity_id))
   OR (o.kind = 'GROUP' AND NOT EXISTS (SELECT 1 FROM "group" g WHERE g.id = o.entity_id));
