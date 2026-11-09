package com.archive.backend.software.infra;

import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareReadRepository;
import com.archive.backend.software.read.SoftwareSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaSoftwareReadRepository implements SoftwareReadRepository {

    private final SoftwareJpaRepository jpa;

    @Override
    public Page<SoftwareSummary> findPage(Pageable pageable) {
        return jpa.findAll(pageable).map(e -> new SoftwareSummary(e.getId(), e.getTitle(), e.getReleaseDate()));
    }

    @Override
    public Page<SoftwareSummary> findLatest(Pageable pageable) {
        PageRequest newestFirst = PageRequest.of(0, pageable.getPageSize(), Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id")));
        return jpa.findAll(newestFirst).map(e -> new SoftwareSummary(e.getId(), e.getTitle(), e.getReleaseDate()));
    }

    @Override
    public Optional<SoftwareDetails> findById(UUID id) {
        return jpa.findById(id).map(e -> new SoftwareDetails(e.getId(), e.getTitle(), e.getDescription(), e.getReleaseDate()));
    }
}
