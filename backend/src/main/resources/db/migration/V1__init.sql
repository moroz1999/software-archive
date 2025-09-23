-- Base software table
CREATE TABLE software
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL,
    title        TEXT        NOT NULL,
    description  TEXT,
    created_at   TIMESTAMP   DEFAULT now(),
    release_date TIMESTAMP   DEFAULT NULL
);

-- Software alternative titles
CREATE TABLE software_alternative_titles
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL,
    title       TEXT                                       NOT NULL,
    software_id UUID                                       NOT NULL,
    lang_code   VARCHAR(10) DEFAULT NULL,
    CONSTRAINT software_alternative_titles_software_id_fk
        FOREIGN KEY (software_id)
            REFERENCES software (id)
);

-- Software languages
CREATE TABLE software_languages
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL,
    software_id UUID                                       NOT NULL
        REFERENCES software,
    language    VARCHAR(10)                               NOT NULL
);

-- Software link type as table (final design)
CREATE TABLE software_link_type
(
    code TEXT PRIMARY KEY
);

INSERT INTO software_link_type (code)
VALUES ('general'),
       ('homepage'),
       ('store'),
       ('youtube');

-- Software links
CREATE TABLE software_links
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    url         TEXT NOT NULL,
    type        TEXT NOT NULL REFERENCES software_link_type (code),
    software_id UUID NOT NULL REFERENCES software (id)
);

-- Country
CREATE TABLE country
(
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name      TEXT NOT NULL,
    latitude  DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    code      CHAR(2)
);

-- City
CREATE TABLE city
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       TEXT NOT NULL,
    latitude   DOUBLE PRECISION,
    longitude  DOUBLE PRECISION,
    country_id UUID REFERENCES country (id)
);

-- Person
CREATE TABLE person
(
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    real_name TEXT,
    notes     TEXT
);

-- Group type
CREATE TABLE group_type
(
    code TEXT PRIMARY KEY
);

-- Group
CREATE TABLE "group"
(
    id    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name  TEXT NOT NULL,
    notes TEXT
);

-- Group - group type relation
CREATE TABLE group_group_type
(
    group_id        UUID REFERENCES "group" (id) ON DELETE CASCADE,
    group_type_code TEXT REFERENCES group_type (code) ON DELETE CASCADE,
    PRIMARY KEY (group_id, group_type_code)
);

-- Alias
CREATE TABLE alias
(
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            TEXT NOT NULL,
    person_id       UUID REFERENCES person (id),
    group_id        UUID REFERENCES "group" (id),
    is_display_name BOOLEAN DEFAULT FALSE
);

-- Alias - city relation
CREATE TABLE alias_city
(
    alias_id UUID REFERENCES alias (id) ON DELETE CASCADE,
    city_id  UUID REFERENCES city (id) ON DELETE CASCADE,
    PRIMARY KEY (alias_id, city_id)
);

-- Group member
CREATE TABLE group_member
(
    group_id UUID REFERENCES "group" (id) ON DELETE CASCADE,
    alias_id UUID REFERENCES alias (id) ON DELETE CASCADE,
    PRIMARY KEY (group_id, alias_id)
);

-- Role
CREATE TABLE role
(
    id   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code TEXT UNIQUE NOT NULL
);

-- Software contribution
CREATE TABLE software_contribution
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    software_id UUID REFERENCES software (id) ON DELETE CASCADE,
    alias_id    UUID REFERENCES alias (id) ON DELETE CASCADE,
    role_id     UUID REFERENCES role (id),
    notes       TEXT
);

-- Event type
CREATE TABLE event_type
(
    code TEXT PRIMARY KEY
);

INSERT INTO event_type (code)
VALUES ('demoparty'),
       ('competition');

-- Event
CREATE TABLE event
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        TEXT NOT NULL,
    type        TEXT REFERENCES event_type (code),
    description TEXT,
    website     TEXT,
    date_start  DATE,
    date_end    DATE
);

-- Compo category
CREATE TABLE compo_category
(
    code  TEXT PRIMARY KEY,
    label TEXT NOT NULL
);

INSERT INTO compo_category (code, label)
VALUES ('general', 'General');

-- Compo
CREATE TABLE compo
(
    id       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id UUID REFERENCES event (id) ON DELETE CASCADE,
    name     TEXT NOT NULL,
    category TEXT REFERENCES compo_category (code)
);

-- Compo entry
CREATE TABLE compo_entry
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    compo_id    UUID REFERENCES compo (id) ON DELETE CASCADE,
    software_id UUID REFERENCES software (id) ON DELETE CASCADE,
    placement   INTEGER,
    notes       TEXT
);

-- Entity location
CREATE TABLE entity_location
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type TEXT NOT NULL CHECK (entity_type IN ('PERSON', 'GROUP', 'ALIAS', 'EVENT')),
    entity_id   UUID NOT NULL,
    city_id     UUID REFERENCES city (id),
    country_id  UUID REFERENCES country (id),
    is_primary  BOOLEAN DEFAULT FALSE,
    notes       TEXT
);
