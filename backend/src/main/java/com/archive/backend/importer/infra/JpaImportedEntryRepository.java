package com.archive.backend.importer.infra;

import com.archive.backend.importer.domain.ExternalKind;
import com.archive.backend.importer.domain.ExternalOrigin;
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
                        new ImportedEntryId(row.getEntityId())));
    }

    @Override
    public void save(ImportedEntry entry) {
        ExternalOriginEntity row = new ExternalOriginEntity();
        row.setKey(keyOf(entry.getOrigin()));
        row.setEntityId(entry.getEntry().value());
        springDataExternalOriginRepository.saveAndFlush(row);
    }

    private static ExternalOriginKey keyOf(ExternalOrigin origin) {
        ExternalOriginKey key = new ExternalOriginKey();
        key.setOrigin(origin.archive());
        key.setKind(origin.kind().name());
        key.setExternalId(origin.externalId());
        return key;
    }
}
