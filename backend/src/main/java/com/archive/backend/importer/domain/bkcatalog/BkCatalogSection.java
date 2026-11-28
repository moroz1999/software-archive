package com.archive.backend.importer.domain.bkcatalog;

import com.archive.backend.importer.domain.run.ImportSection;
import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Arrays;

/**
 * A section of bk-catalog, one list of its own: games, programs or the demoscene (docs/domain/imports.md#bk-catalog).
 * Each names its entries on the site by a word of its own and keeps their files and screenshots in folders of its own.
 */
@ValueObject
public enum BkCatalogSection implements ImportSection {
    GAMES("games", "game", "bk_games_files", "bk_games_screenshots"),
    SOFTWARE("software", "software", "bk_files", "bk_screenshots"),
    DEMOSCENE("demoscene", "demo", "bk_files", "bk_screenshots");

    private final String code;
    private final String card;
    private final String files;
    private final String screenshots;

    BkCatalogSection(String code, String card, String files, String screenshots) {
        this.code = code;
        this.card = card;
        this.files = files;
        this.screenshots = screenshots;
    }

    @Override
    public String code() {
        return code;
    }

    /** The word the site names an entry of the section by, such as {@code game} in {@code #game-555901}. */
    public String card() {
        return card;
    }

    /** The folder of the repository holding the files of the section's entries. */
    public String files() {
        return files;
    }

    /** The folder of the repository holding the full-size screenshots of the section's entries. */
    public String screenshots() {
        return screenshots;
    }

    public static BkCatalogSection fromCode(String code) {
        return Arrays.stream(values())
                .filter(section -> section.code.equals(code))
                .findFirst()
                .orElseThrow(() ->
                        new InvalidValueException("import.section-invalid", "bk-catalog has no section " + code + "."));
    }
}
