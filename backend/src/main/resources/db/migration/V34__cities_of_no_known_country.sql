-- A city whose country is not known has no country (docs/domain/geo.md): the cities of the stub country "Unknown" are
-- taken out of it, and so are the places located through them. Nothing is deleted: the stub country itself stays,
-- with the places given in it directly, for an administrator to sort out.

-- The software credited on people and groups located in such a city names the country "Unknown" in its search
-- document, built from their places as the search builds it: it is sent to the search again, before its places change.
INSERT INTO search_outbox (entity_type, entity_id)
SELECT DISTINCT 'SOFTWARE', sc.software_id
FROM software_contribution sc
LEFT JOIN label l ON l.id = sc.label_id
JOIN entity_location el
  ON (el.entity_type = 'PERSON' AND el.entity_id = COALESCE(sc.person_id, l.person_id))
  OR (el.entity_type = 'GROUP' AND el.entity_id = COALESCE(sc.group_id, l.group_id))
JOIN city c ON c.id = el.city_id
WHERE c.country_id = '00000000-0000-0000-0000-00000000c001'
ON CONFLICT (entity_type, entity_id) DO UPDATE
SET renewal = nextval('search_outbox_renewal'), attempts = 0, next_attempt_at = now();

-- A place keeps the country of its city as well: one in such a city is the city alone.
UPDATE entity_location
SET country_id = NULL
WHERE country_id = '00000000-0000-0000-0000-00000000c001'
  AND city_id IN (SELECT id FROM city WHERE country_id = '00000000-0000-0000-0000-00000000c001');

UPDATE city
SET country_id = NULL
WHERE country_id = '00000000-0000-0000-0000-00000000c001';
