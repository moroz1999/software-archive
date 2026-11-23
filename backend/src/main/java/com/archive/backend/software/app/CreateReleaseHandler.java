package com.archive.backend.software.app;

import com.archive.backend.hardware.domain.HardwareSets;
import com.archive.backend.software.domain.Release;
import com.archive.backend.software.domain.ReleaseArchive;
import com.archive.backend.software.domain.ReleaseArchiveId;
import com.archive.backend.software.domain.ReleaseArchiveRepository;
import com.archive.backend.software.domain.ReleaseArchiveUpload;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.domain.SoftwareRepository;
import com.archive.backend.user.domain.AddedBy;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateReleaseHandler {

    private final SoftwareRepository repository;
    private final HardwareSets hardwareSets;
    private final ReleaseArchiveRepository archiveFiles;
    private final ApplicationEventPublisher events;

    @Transactional
    public ReleaseId handle(CreateReleaseCommand c) {
        AddedBy addedBy = AddedBy.of(c.actor());
        Software software = repository.findById(c.softwareId()).orElseThrow(SoftwareNotFoundException::new);
        ReleaseId id = new ReleaseId(UUID.randomUUID());
        ReleaseArchiveUpload upload = c.archive();
        @Nullable
        ReleaseArchive archive = upload == null
                ? null
                : new ReleaseArchive(new ReleaseArchiveId(UUID.randomUUID()), upload.fileName(), upload.size());
        Release added = software.addRelease(
                id, c.content(), hardwareSets.of(software.getPlatform(), c.hardware()), addedBy, archive);
        if (upload != null) {
            archiveFiles.store(Objects.requireNonNull(archive).id(), upload);
        }
        repository.save(software);
        software.pullEvents().forEach(events::publishEvent);
        return added.id();
    }
}
