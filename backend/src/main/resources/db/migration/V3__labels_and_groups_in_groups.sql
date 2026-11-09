-- Aliases are labels: the names works are released under (docs/domain/authors.md).
ALTER TABLE alias RENAME TO label;
ALTER TABLE label RENAME CONSTRAINT alias_pkey TO label_pkey;
ALTER TABLE label RENAME CONSTRAINT alias_person_id_fkey TO label_person_id_fkey;
ALTER TABLE label RENAME CONSTRAINT alias_group_id_fkey TO label_group_id_fkey;

-- A label belongs to nobody known yet, to a person or to a group, never to both.
ALTER TABLE label
    ADD CONSTRAINT label_one_owner CHECK (person_id IS NULL OR group_id IS NULL);

ALTER TABLE alias_city RENAME TO label_city;
ALTER TABLE label_city RENAME COLUMN alias_id TO label_id;
ALTER TABLE label_city RENAME CONSTRAINT alias_city_pkey TO label_city_pkey;
ALTER TABLE label_city RENAME CONSTRAINT alias_city_alias_id_fkey TO label_city_label_id_fkey;
ALTER TABLE label_city RENAME CONSTRAINT alias_city_city_id_fkey TO label_city_city_id_fkey;

ALTER TABLE group_member RENAME COLUMN alias_id TO label_id;
ALTER TABLE group_member RENAME CONSTRAINT group_member_alias_id_fkey TO group_member_label_id_fkey;

ALTER TABLE software_contribution RENAME COLUMN alias_id TO label_id;
ALTER TABLE software_contribution RENAME CONSTRAINT software_contribution_alias_id_fkey TO software_contribution_label_id_fkey;

ALTER TABLE entity_location DROP CONSTRAINT entity_location_entity_type_check;
UPDATE entity_location SET entity_type = 'LABEL' WHERE entity_type = 'ALIAS';
ALTER TABLE entity_location
    ADD CONSTRAINT entity_location_entity_type_check CHECK (entity_type IN ('PERSON', 'GROUP', 'LABEL', 'EVENT'));

-- A group can be a member of another group: a relation of its own, apart from the labels that are members.
CREATE TABLE group_member_group
(
    group_id        UUID REFERENCES "group" (id) ON DELETE CASCADE,
    member_group_id UUID REFERENCES "group" (id) ON DELETE CASCADE,
    PRIMARY KEY (group_id, member_group_id),
    CONSTRAINT group_member_group_not_itself CHECK (group_id <> member_group_id)
);
