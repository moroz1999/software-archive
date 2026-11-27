CREATE INDEX software_links_software_id_idx ON software_links (software_id);

-- The assistant used the entire page address as the external ID. Importers use the source's entry ID.
INSERT INTO software_links (software_id, type, url)
SELECT DISTINCT o.entity_id, 'homepage', o.url
FROM external_origin o
JOIN software s ON s.id = o.entity_id
WHERE o.kind = 'SOFTWARE' AND o.external_id = o.url AND o.url ~* '^https?://'
  AND NOT EXISTS (
      SELECT 1 FROM software_links l
      WHERE l.software_id = o.entity_id AND l.type = 'homepage' AND l.url = o.url
  );

-- Remove a synthetic origin only after its address exists on the same entry as a homepage.
DELETE FROM external_origin o
WHERE o.kind = 'SOFTWARE' AND o.external_id = o.url AND o.url ~* '^https?://'
  AND EXISTS (
      SELECT 1 FROM software_links l
      WHERE l.software_id = o.entity_id AND l.type = 'homepage' AND l.url = o.url
  );
