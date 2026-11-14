-- Software deleted before its deletion forgot its origin: forgotten now, so the next import takes the program again.
DELETE FROM external_origin o
WHERE o.kind = 'SOFTWARE'
  AND NOT EXISTS (SELECT 1 FROM software s WHERE s.id = o.entity_id);
