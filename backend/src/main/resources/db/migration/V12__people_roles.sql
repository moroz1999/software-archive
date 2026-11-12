ALTER TABLE group_member
    DROP CONSTRAINT group_member_pkey,
    ADD COLUMN id UUID DEFAULT gen_random_uuid() NOT NULL,
    ADD COLUMN person_id UUID REFERENCES person (id) ON DELETE CASCADE,
    ADD COLUMN role_codes TEXT[] DEFAULT ARRAY['unknown']::TEXT[] NOT NULL,
    ADD COLUMN start_date DATE,
    ADD COLUMN end_date DATE,
    ALTER COLUMN label_id DROP NOT NULL,
    ADD PRIMARY KEY (id),
    ADD CONSTRAINT group_member_one_person_reference CHECK ((person_id IS NULL) <> (label_id IS NULL)),
    ADD CONSTRAINT group_member_dates_ordered CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date);

CREATE UNIQUE INDEX group_member_person_unique ON group_member (group_id, person_id) WHERE person_id IS NOT NULL;
CREATE UNIQUE INDEX group_member_label_unique ON group_member (group_id, label_id) WHERE label_id IS NOT NULL;

ALTER TABLE software_contribution
    ADD COLUMN person_id UUID REFERENCES person (id) ON DELETE CASCADE,
    ADD COLUMN role_codes TEXT[] DEFAULT ARRAY['unknown']::TEXT[] NOT NULL,
    ADD CONSTRAINT software_contribution_one_person_reference CHECK ((person_id IS NULL) <> (label_id IS NULL));

UPDATE software_contribution c
SET role_codes = ARRAY[COALESCE(r.code, 'unknown')]
FROM role r
WHERE r.id = c.role_id;

CREATE UNIQUE INDEX software_contribution_person_unique ON software_contribution (software_id, person_id) WHERE person_id IS NOT NULL;
CREATE UNIQUE INDEX software_contribution_label_unique ON software_contribution (software_id, label_id) WHERE label_id IS NOT NULL;
