package com.archive.backend.software.app;

import com.archive.backend.hardware.domain.HardwareSets;
import com.archive.backend.software.domain.ReleaseArchive;
import com.archive.backend.software.domain.ReleaseArchiveId;
import com.archive.backend.software.domain.ReleaseArchiveRepository;
import com.archive.backend.software.domain.ReleaseArchiveUpload;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.domain.SoftwareRepository;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateReleaseHandler {

    private final SoftwareRepository repository;
    private final HardwareSets hardwareSets;
    private final ReleaseArchiveRepository archiveFiles;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(UpdateReleaseCommand c) {
        Software software = repository.findById(c.softwareId()).orElseThrow(SoftwareNotFoundException::new);
        ReleaseArchiveUpload upload = c.archive();
        @Nullable
        ReleaseArchive replacement = upload == null
                ? null
                : new ReleaseArchive(new ReleaseArchiveId(UUID.randomUUID()), upload.fileName(), upload.size());
        software.editRelease(
                c.releaseId(),
                c.content(),
                hardwareSets.of(software.getPlatform(), c.hardware()),
                c.actor(),
                replacement,
                c.removeArchive().value());
        if (upload != null) {
            archiveFiles.store(Objects.requireNonNull(replacement).id(), upload);
        }
        repository.save(software);
        software.pullEvents().forEach(events::publishEvent);
    }
}
