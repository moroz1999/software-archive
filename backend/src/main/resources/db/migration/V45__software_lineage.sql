-- Where the work comes from (docs/domain/software.md#lineage): the kind, its reliability, the rework in the order of
-- the list, and the original as free text. Every part is unknown for software stored before.
ALTER TABLE software
    ADD COLUMN lineage_kind        TEXT,
    ADD COLUMN lineage_reliability TEXT,
    ADD COLUMN lineage_rework      TEXT[] NOT NULL DEFAULT '{}',
    ADD COLUMN lineage_source      TEXT,
    ADD CONSTRAINT software_lineage_source_length CHECK (char_length(lineage_source) <= 255),
    ADD CONSTRAINT software_lineage_kind_given CHECK (
        lineage_kind IS NOT NULL OR (lineage_reliability IS NULL AND cardinality(lineage_rework) = 0));
