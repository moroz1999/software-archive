-- A release date is a year, or a day of that year when the day is known (docs/domain/software.md).
ALTER TABLE software
    ADD COLUMN release_year integer,
    ADD COLUMN release_day  date;
UPDATE software
SET release_year = EXTRACT(YEAR FROM release_date)::integer,
    release_day  = release_date
WHERE release_date IS NOT NULL;
ALTER TABLE software
    DROP COLUMN release_date,
    ADD CONSTRAINT software_release_day_of_its_year
        CHECK (release_day IS NULL OR (release_year IS NOT NULL AND EXTRACT(YEAR FROM release_day) = release_year));

ALTER TABLE software_release
    ADD COLUMN release_year integer,
    ADD COLUMN release_day  date;
UPDATE software_release
SET release_year = EXTRACT(YEAR FROM release_date)::integer,
    release_day  = release_date
WHERE release_date IS NOT NULL;
ALTER TABLE software_release
    DROP COLUMN release_date,
    ADD CONSTRAINT software_release_release_day_of_its_year
        CHECK (release_day IS NULL OR (release_year IS NOT NULL AND EXTRACT(YEAR FROM release_day) = release_year));
