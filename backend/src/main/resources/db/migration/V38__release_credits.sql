-- Credits of a release: who made, cracked, adapted or otherwise worked on this version of the software, as credits of
-- software do (docs/domain/software.md#releases). Each participant is credited once per release.
CREATE TABLE software_release_contribution
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    release_id UUID   NOT NULL REFERENCES software_release (id) ON DELETE CASCADE,
    person_id  UUID REFERENCES person (id) ON DELETE CASCADE,
    group_id   UUID REFERENCES "group" (id) ON DELETE CASCADE,
    label_id   UUID REFERENCES label (id) ON DELETE CASCADE,
    role_codes TEXT[] NOT NULL DEFAULT ARRAY['unknown']::TEXT[],
    CONSTRAINT software_release_contribution_one_participant CHECK (num_nonnulls(person_id, group_id, label_id) = 1)
);

CREATE UNIQUE INDEX software_release_contribution_person_unique
    ON software_release_contribution (release_id, person_id) WHERE person_id IS NOT NULL;
CREATE UNIQUE INDEX software_release_contribution_group_unique
    ON software_release_contribution (release_id, group_id) WHERE group_id IS NOT NULL;
CREATE UNIQUE INDEX software_release_contribution_label_unique
    ON software_release_contribution (release_id, label_id) WHERE label_id IS NOT NULL;
CREATE INDEX software_release_contribution_person ON software_release_contribution (person_id);
CREATE INDEX software_release_contribution_group ON software_release_contribution (group_id);
CREATE INDEX software_release_contribution_label ON software_release_contribution (label_id);
