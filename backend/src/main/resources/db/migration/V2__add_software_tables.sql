DROP TYPE IF EXISTS software_link_type CASCADE;
create type software_link_type as enum ('general', 'homepage', 'store', 'youtube');

CREATE TABLE software_alternative_titles
(
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid() NOT NULL,
    title       TEXT                                       NOT NULL,
    software_id UUID                                       NOT NULL,
    lang_code   VARCHAR(10)      DEFAULT NULL,
    CONSTRAINT software_alternative_titles_software_id_fk
        FOREIGN KEY (software_id)
            REFERENCES software (id)
);

create table software_languages
(
    software_id uuid                           not null
        constraint software_languages_software_id_fk
            references software,
    language    varchar(10)                    not null,
    id          uuid default gen_random_uuid() not null
        constraint software_languages_pk
            primary key
);

create table software_links
(
    id          uuid default gen_random_uuid() not null
        constraint software_links_pk
            primary key,
    url         text                           not null,
    type        software_link_type             not null,
    software_id uuid                           not null
        constraint software_links_software_id_fk
            references software
);


ALTER TABLE software
    RENAME COLUMN name TO title;

ALTER TABLE software
    ADD COLUMN release_date TIMESTAMP DEFAULT NULL;
