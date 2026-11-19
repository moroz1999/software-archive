-- Whether software can be had and how (docs/domain/software.md); unknown until it is known.
ALTER TABLE software
    ADD COLUMN preservation TEXT NOT NULL DEFAULT 'unknown'
        CONSTRAINT software_preservation_check
            CHECK (preservation IN ('unknown', 'missing-in-action', 'donationware', 'in-sales'));
