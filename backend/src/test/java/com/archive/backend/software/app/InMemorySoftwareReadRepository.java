package com.archive.backend.software.app;

import com.archive.backend.software.read.ReleaseDetails;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareReadRepository;
import com.archive.backend.software.read.SoftwareRelease;
import com.archive.backend.software.read.SoftwareSummary;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/** Serves what it was given and remembers the platform it was asked for: filtering itself is the SQL's job. */
class InMemorySoftwareReadRepository implements SoftwareReadRepository {

    private final List<SoftwareDetails> items = new ArrayList<>();
    private final Map<UUID, List<SoftwareRelease>> releases = new HashMap<>();
    private final Map<UUID, List<ReleaseDetails>> releaseDetails = new HashMap<>();
    private @Nullable String lastPlatform;

    void add(SoftwareDetails details) {
        items.add(details);
    }

    void addReleases(UUID softwareId, SoftwareRelease... ofSoftware) {
        releases.put(softwareId, List.of(ofSoftware));
    }

    void addRelease(UUID softwareId, ReleaseDetails release) {
        releaseDetails.computeIfAbsent(softwareId, key -> new ArrayList<>()).add(release);
    }

    @Nullable
    String lastPlatform() {
        return lastPlatform;
    }

    @Override
    public Page<SoftwareSummary> findPage(@Nullable String platform, Pageable pageable) {
        lastPlatform = platform;
        int from = (int) Math.min(pageable.getOffset(), items.size());
        int to = Math.min(from + pageable.getPageSize(), items.size());
        List<SoftwareSummary> content = items.subList(from, to).stream()
                .map(details -> new SoftwareSummary(details.id(), details.title(), details.releaseDate()))
                .toList();
        return new PageImpl<>(content, pageable, items.size());
    }

    /** Added last means newest. */
    @Override
    public Page<SoftwareSummary> findLatest(@Nullable String platform, Pageable pageable) {
        lastPlatform = platform;
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

    @Override
    public List<SoftwareRelease> findReleases(UUID softwareId) {
        return releases.getOrDefault(softwareId, List.of());
    }

    @Override
    public Optional<ReleaseDetails> findRelease(UUID softwareId, UUID releaseId) {
        return releaseDetails.getOrDefault(softwareId, List.of()).stream()
                .filter(release -> release.id().equals(releaseId))
                .findFirst();
    }
}
