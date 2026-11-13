-- Screenshots of software and of its releases, in their order; the pictures live in the file storage under the ids.
-- The ids are unique only at the end of a change: reordering moves them between positions.
CREATE TABLE software_screenshot (
    software_id uuid        NOT NULL REFERENCES software (id) ON DELETE CASCADE,
    position    integer     NOT NULL,
    id          uuid        NOT NULL,
    format      varchar(10) NOT NULL,
    PRIMARY KEY (software_id, position),
    CONSTRAINT software_screenshot_id_unique UNIQUE (id) DEFERRABLE INITIALLY DEFERRED
);

CREATE TABLE software_release_screenshot (
    release_id uuid        NOT NULL REFERENCES software_release (id) ON DELETE CASCADE,
    position   integer     NOT NULL,
    id         uuid        NOT NULL,
    format     varchar(10) NOT NULL,
    PRIMARY KEY (release_id, position),
    CONSTRAINT software_release_screenshot_id_unique UNIQUE (id) DEFERRABLE INITIALLY DEFERRED
);
