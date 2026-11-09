package com.archive.backend.software.app;

import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareReadRepository;
import com.archive.backend.software.read.SoftwareSummary;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

class InMemorySoftwareReadRepository implements SoftwareReadRepository {

    private final List<SoftwareDetails> items = new ArrayList<>();

    void add(SoftwareDetails details) {
        items.add(details);
    }

    @Override
    public Page<SoftwareSummary> findPage(Pageable pageable) {
        int from = (int) Math.min(pageable.getOffset(), items.size());
        int to = Math.min(from + pageable.getPageSize(), items.size());
        List<SoftwareSummary> content = items.subList(from, to).stream()
                .map(details -> new SoftwareSummary(details.id(), details.title(), details.releaseDate()))
                .toList();
        return new PageImpl<>(content, pageable, items.size());
    }

    /** Added last means newest. */
    @Override
    public Page<SoftwareSummary> findLatest(Pageable pageable) {
        List<SoftwareDetails> newestFirst = new ArrayList<>(items);
        Collections.reverse(newestFirst);
        List<SoftwareSummary> content = newestFirst.stream()
                .limit(pageable.getPageSize())
                .map(details -> new SoftwareSummary(details.id(), details.title(), details.releaseDate()))
                .toList();
        return new PageImpl<>(content, pageable, items.size());
    }

    @Override
    public Optional<SoftwareDetails> findById(UUID id) {
        return items.stream().filter(details -> details.id().equals(id)).findFirst();
    }
}
