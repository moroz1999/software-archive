package com.archive.backend.software.app;

import com.archive.backend.hardware.domain.PlatformRepository;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.domain.SoftwarePlatformUnknownException;
import com.archive.backend.software.domain.SoftwareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateSoftwareHandler {

    private final SoftwareRepository repository;

    private final PlatformRepository platforms;

    @Transactional
    public void handle(UpdateSoftwareCommand c) {
        Software software = repository.findById(c.id()).orElseThrow(SoftwareNotFoundException::new);
        if (!platforms.exists(c.platform())) {
            throw new SoftwarePlatformUnknownException();
        }
        software.edit(c.title(), c.description(), c.releaseDate(), c.platform());
        repository.save(software);
    }
}
