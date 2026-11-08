package com.archive.backend.software.app;

import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareNotFoundException;
import com.archive.backend.software.domain.SoftwareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteSoftwareHandler {

    private final SoftwareRepository repository;

    @Transactional
    public void handle(DeleteSoftwareCommand c) {
        Software software = repository.findById(c.id()).orElseThrow(SoftwareNotFoundException::new);
        repository.delete(software);
    }
}
