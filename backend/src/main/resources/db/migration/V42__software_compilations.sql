CREATE TABLE software_compilation_item
(
    compilation_id UUID NOT NULL REFERENCES software (id) ON DELETE CASCADE,
    item_id        UUID NOT NULL REFERENCES software (id) ON DELETE CASCADE,
    PRIMARY KEY (compilation_id, item_id),
    CHECK (compilation_id <> item_id)
);

CREATE INDEX software_compilation_item_member_idx ON software_compilation_item (item_id);
