package com.archive.backend.assistant.web.software;

import com.archive.backend.importer.app.FindSoftwareOfPageHandler;
import com.archive.backend.importer.app.FindSoftwareOfPageQuery;
import com.archive.backend.importer.app.RecordSoftwarePageCommand;
import com.archive.backend.importer.app.RecordSoftwarePageHandler;
import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.importer.domain.ImportedEntryId;
import com.archive.backend.importer.domain.SoftwarePageTakenException;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * The page of another site software is taken from, recorded as its source like the page of an import
 * (docs/domain/ai-assistant.md). Checked before the software changes, so a page of other software changes nothing.
 */
@Component
@RequiredArgsConstructor
class SourcePages {

    private final FindSoftwareOfPageHandler find;
    private final RecordSoftwarePageHandler record;

    /** Fails when the page is the source of software other than this one, or of any when there is none yet. */
    void requireFree(@Nullable String url, @Nullable UUID software) {
        if (url == null) {
            return;
        }
        Optional<UUID> owner = find.handle(new FindSoftwareOfPageQuery(new ExternalPage(url)));
        if (owner.isPresent() && !owner.get().equals(software)) {
            throw new SoftwarePageTakenException();
        }
    }

    void record(@Nullable String url, UUID software) {
        if (url != null) {
            record.handle(new RecordSoftwarePageCommand(new ImportedEntryId(software), new ExternalPage(url)));
        }
    }
}
