package com.archive.backend.importer.infra;

import com.archive.backend.importer.domain.ExternalKind;
import com.archive.backend.importer.domain.ExternalOrigin;
import com.archive.backend.importer.domain.ExternalPage;
import com.archive.backend.importer.domain.ImportedEntry;
import com.archive.backend.importer.domain.ImportedEntryId;
import com.archive.backend.importer.domain.ImportedEntryRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaImportedEntryRepository implements ImportedEntryRepository {

    private final SpringDataExternalOriginRepository springDataExternalOriginRepository;

    @Override
    public Optional<ImportedEntry> findByOrigin(ExternalOrigin origin) {
        return springDataExternalOriginRepository
                .findById(keyOf(origin))
                .map(row -> new ImportedEntry(
                        new ExternalOrigin(
                                row.getKey().getOrigin(),
                                ExternalKind.valueOf(row.getKey().getKind()),
                                row.getKey().getExternalId()),
                        new ImportedEntryId(row.getEntityId()),
                        row.getUrl() == null ? null : new ExternalPage(row.getUrl())));
    }

    @Override
    public void save(ImportedEntry entry) {
        ExternalOriginEntity row = new ExternalOriginEntity();
        row.setKey(keyOf(entry.getOrigin()));
        row.setEntityId(entry.getEntry().value());
        ExternalPage page = entry.getPage();
        row.setUrl(page == null ? null : page.url());
        springDataExternalOriginRepository.saveAndFlush(row);
    }

    @Override
    public void forget(ExternalKind kind, ImportedEntryId entry) {
        springDataExternalOriginRepository.deleteByKeyKindAndEntityId(kind.name(), entry.value());
    }

    @Override
    public boolean isFrom(String archive, ExternalKind kind, ImportedEntryId entry) {
        return springDataExternalOriginRepository.existsByKeyOriginAndKeyKindAndEntityId(
                archive, kind.name(), entry.value());
    }

    @Override
    public void transfer(ExternalKind kind, ImportedEntryId entry, ExternalKind toKind, ImportedEntryId to) {
        springDataExternalOriginRepository.transfer(kind.name(), entry.value(), toKind.name(), to.value());
    }

    private static ExternalOriginKey keyOf(ExternalOrigin origin) {
        ExternalOriginKey key = new ExternalOriginKey();
        key.setOrigin(origin.archive());
        key.setKind(origin.kind().name());
        key.setExternalId(origin.externalId());
        return key;
    }
}
