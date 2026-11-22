-- A group membership keeps only its roles (docs/domain/authors.md#groups). The days go only while no membership has
-- any: a day found stops the migration, so none is lost without a decision.
DO
$$
    BEGIN
        IF EXISTS (SELECT 1 FROM group_member WHERE start_date IS NOT NULL OR end_date IS NOT NULL) THEN
            RAISE EXCEPTION 'A group membership has a start or end date; decide what becomes of it before dropping the dates.';
        END IF;
    END
$$;

ALTER TABLE group_member
    DROP CONSTRAINT group_member_dates_ordered,
    DROP COLUMN start_date,
    DROP COLUMN end_date;
