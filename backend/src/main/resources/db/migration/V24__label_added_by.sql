-- Who added each label (docs/domain/authors.md#label): whoever typed it, or the importer for what an import took.
-- Labels from before are given the importer when an import remembers them, else whoever added their owner.
ALTER TABLE label ADD COLUMN added_by uuid REFERENCES app_user (id) ON DELETE SET NULL;

UPDATE label l
SET added_by = '00000000-0000-0000-0000-00000000a001'
WHERE EXISTS (SELECT 1 FROM external_origin o WHERE o.kind = 'LABEL' AND o.entity_id = l.id);

UPDATE label l SET added_by = p.added_by FROM person p WHERE l.added_by IS NULL AND l.person_id = p.id;

UPDATE label l SET added_by = g.added_by FROM "group" g WHERE l.added_by IS NULL AND l.group_id = g.id;
