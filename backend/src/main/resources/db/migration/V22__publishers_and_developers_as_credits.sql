-- Publishers and developers are credits with the roles Publisher and Developer (docs/domain/software.md#credits); a
-- group is credited by itself as well as by a label.
ALTER TABLE software_contribution
    ADD COLUMN group_id UUID REFERENCES "group" (id) ON DELETE CASCADE,
    DROP CONSTRAINT software_contribution_one_person_reference,
    ADD CONSTRAINT software_contribution_one_participant CHECK (num_nonnulls(person_id, group_id, label_id) = 1);

CREATE UNIQUE INDEX software_contribution_group_unique ON software_contribution (software_id, group_id) WHERE group_id IS NOT NULL;

CREATE TEMPORARY TABLE named_role ON COMMIT DROP AS
SELECT software_id, author_id AS person_id, NULL::uuid AS group_id, NULL::uuid AS label_id, 'publisher' AS role
FROM software_publisher_author
UNION
SELECT software_id, NULL, group_id, NULL, 'publisher' FROM software_publisher_group
UNION
SELECT software_id, NULL, NULL, label_id, 'publisher' FROM software_publisher_author_label
UNION
SELECT software_id, NULL, NULL, label_id, 'publisher' FROM software_publisher_group_label
UNION
SELECT software_id, NULL, group_id, NULL, 'developer' FROM software_developer_group
UNION
SELECT software_id, NULL, NULL, label_id, 'developer' FROM software_developer_label;

INSERT INTO software_contribution (software_id, person_id, group_id, label_id)
SELECT DISTINCT r.software_id, r.person_id, r.group_id, r.label_id
FROM named_role r
WHERE NOT EXISTS (SELECT 1
                  FROM software_contribution c
                  WHERE c.software_id = r.software_id
                    AND c.person_id IS NOT DISTINCT FROM r.person_id
                    AND c.group_id IS NOT DISTINCT FROM r.group_id
                    AND c.label_id IS NOT DISTINCT FROM r.label_id);

-- A credit known by a real role is no longer of an unknown one.
UPDATE software_contribution c
SET role_codes = array_remove(c.role_codes, 'unknown') || ARRAY(
        SELECT DISTINCT r.role
        FROM named_role r
        WHERE r.software_id = c.software_id
          AND r.person_id IS NOT DISTINCT FROM c.person_id
          AND r.group_id IS NOT DISTINCT FROM c.group_id
          AND r.label_id IS NOT DISTINCT FROM c.label_id
        ORDER BY r.role)
WHERE EXISTS (SELECT 1
              FROM named_role r
              WHERE r.software_id = c.software_id
                AND r.person_id IS NOT DISTINCT FROM c.person_id
                AND r.group_id IS NOT DISTINCT FROM c.group_id
                AND r.label_id IS NOT DISTINCT FROM c.label_id);

DROP TABLE software_publisher_author, software_publisher_group, software_publisher_author_label,
    software_publisher_group_label, software_developer_group, software_developer_label;
