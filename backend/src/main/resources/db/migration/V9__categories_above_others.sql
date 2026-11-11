-- Software is never in a category together with one above it (docs/domain/categories.md): links stored before the
-- rule are taken out of every category above another category of the same software.
WITH RECURSIVE above (software_id, category_id) AS (
    SELECT sc.software_id, c.parent_id
    FROM software_category sc
    JOIN category c ON c.id = sc.category_id
    WHERE c.parent_id IS NOT NULL
    UNION
    SELECT a.software_id, c.parent_id
    FROM above a
    JOIN category c ON c.id = a.category_id
    WHERE c.parent_id IS NOT NULL
)
DELETE FROM software_category sc
USING above a
WHERE sc.software_id = a.software_id
  AND sc.category_id = a.category_id;
