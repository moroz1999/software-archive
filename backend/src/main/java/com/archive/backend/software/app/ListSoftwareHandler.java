package com.archive.backend.software.app;

import com.archive.backend.software.domain.Software;
import com.archive.backend.software.infra.SoftwareMapper;
import com.archive.backend.software.infra.SoftwareRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListSoftwareHandler {
    private final SoftwareRepository repository;
    private final SoftwareMapper mapper;

    @Transactional(readOnly = true)
    public Page<Software> handle(ListSoftware q) {
        return repository.findAll(q.pageable()).map(mapper::toDomain);
    }
}