CREATE TABLE software_publisher_author
(
    software_id UUID REFERENCES software (id) ON DELETE CASCADE,
    author_id   UUID REFERENCES person (id) ON DELETE CASCADE,
    PRIMARY KEY (software_id, author_id)
);

CREATE TABLE software_publisher_group
(
    software_id UUID REFERENCES software (id) ON DELETE CASCADE,
    group_id    UUID REFERENCES "group" (id) ON DELETE CASCADE,
    PRIMARY KEY (software_id, group_id)
);

CREATE TABLE software_publisher_author_label
(
    software_id UUID REFERENCES software (id) ON DELETE CASCADE,
    label_id    UUID REFERENCES label (id) ON DELETE CASCADE,
    PRIMARY KEY (software_id, label_id)
);

CREATE TABLE software_publisher_group_label
(
    software_id UUID REFERENCES software (id) ON DELETE CASCADE,
    label_id    UUID REFERENCES label (id) ON DELETE CASCADE,
    PRIMARY KEY (software_id, label_id)
);

CREATE TABLE software_developer_group
(
    software_id UUID REFERENCES software (id) ON DELETE CASCADE,
    group_id    UUID REFERENCES "group" (id) ON DELETE CASCADE,
    PRIMARY KEY (software_id, group_id)
);

CREATE TABLE software_developer_label
(
    software_id UUID REFERENCES software (id) ON DELETE CASCADE,
    label_id    UUID REFERENCES label (id) ON DELETE CASCADE,
    PRIMARY KEY (software_id, label_id)
);
