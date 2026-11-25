-- Software keeps maps, covers and adverts beside its screenshots, each a gallery of its own; every picture there is
-- a screenshot (docs/domain/software.md#pictures). The positions stay one order per software across its galleries.
ALTER TABLE software_screenshot
    ADD COLUMN gallery VARCHAR(20) NOT NULL DEFAULT 'screenshots'
        CHECK (gallery IN ('screenshots', 'maps', 'covers', 'adverts'));

-- How the software is loaded, run and played, apart from its description.
ALTER TABLE software
    ADD COLUMN instructions TEXT;
