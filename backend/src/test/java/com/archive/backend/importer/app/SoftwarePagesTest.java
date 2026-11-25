package com.archive.backend.importer.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.importer.domain.ExternalOrigin;
import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.importer.domain.ImportedEntry;
import com.archive.backend.importer.domain.ImportedEntryId;
import com.archive.backend.shared.domain.error.ConflictException;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** The pages of other sites software was taken from, which an editor's assistant records (docs/domain/imports.md). */
class SoftwarePagesTest {

    private static final ImportedEntryId ELITE =
            new ImportedEntryId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final ImportedEntryId TETRIS =
            new ImportedEntryId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
    private static final ExternalPage PAGE = new ExternalPage("https://www.zxart.ee/eng/software/game/elite/");

    private final InMemoryImportedEntryRepository repository = new InMemoryImportedEntryRepository();
    private final RecordSoftwarePageHandler record = new RecordSoftwarePageHandler(repository);
    private final FindSoftwareOfPageHandler find = new FindSoftwareOfPageHandler(repository);

    @Test
    void recordsThePageAsTheSourceOfTheSoftwareUnderTheNameOfItsSite() {
        record.handle(new RecordSoftwarePageCommand(ELITE, PAGE));

        assertThat(repository.entries).singleElement().satisfies(entry -> {
            assertThat(entry.getOrigin()).isEqualTo(ExternalOrigin.software("zxart.ee", PAGE.url()));
            assertThat(entry.getEntry()).isEqualTo(ELITE);
            assertThat(entry.getPage()).isEqualTo(PAGE);
        });
    }

    @Test
    void recordingThePageOfTheSameSoftwareAgainChangesNothing() {
        record.handle(new RecordSoftwarePageCommand(ELITE, PAGE));

        record.handle(new RecordSoftwarePageCommand(ELITE, PAGE));

        assertThat(repository.entries).extracting(ImportedEntry::getEntry).containsExactly(ELITE);
    }

    @Test
    void refusesThePageOfOtherSoftwareAndKeepsItsSoftware() {
        record.handle(new RecordSoftwarePageCommand(ELITE, PAGE));

        assertThatThrownBy(() -> record.handle(new RecordSoftwarePageCommand(TETRIS, PAGE)))
                .isInstanceOfSatisfying(
                        ConflictException.class, e -> assertThat(e.code()).isEqualTo("software.page-taken"));
        assertThat(repository.entries).extracting(ImportedEntry::getEntry).containsExactly(ELITE);
    }

    @Test
    void findsTheSoftwareAPageIsTheSourceOf() {
        record.handle(new RecordSoftwarePageCommand(ELITE, PAGE));

        assertThat(find.handle(new FindSoftwareOfPageQuery(PAGE))).contains(ELITE.value());
        assertThat(find.handle(
                        new FindSoftwareOfPageQuery(new ExternalPage("https://zxart.ee/eng/software/game/tetris/"))))
                .isEmpty();
    }

    @Test
    void refusesAnAddressThatNamesNoSite() {
        assertThatThrownBy(() -> record.handle(new RecordSoftwarePageCommand(ELITE, new ExternalPage("elite.html"))))
                .isInstanceOf(InvalidValueException.class);
        assertThat(repository.entries).isEmpty();
    }
}
