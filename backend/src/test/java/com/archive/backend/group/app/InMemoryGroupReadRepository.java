package com.archive.backend.group.app;

import com.archive.backend.group.read.GroupCatalogEntry;
import com.archive.backend.group.read.GroupDetails;
import com.archive.backend.group.read.GroupReadRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

/** Serves what it was given and remembers the filter it was asked with: filtering itself is the SQL's job. */
class InMemoryGroupReadRepository implements GroupReadRepository {

    record Filter(
            @Nullable String letter,
            @Nullable UUID countryId,
            @Nullable UUID cityId,
            @Nullable String platform) {}

    private final List<GroupCatalogEntry> entries = new ArrayList<>();
    private final List<GroupDetails> details = new ArrayList<>();
    private @Nullable Filter lastFilter;

    void add(GroupCatalogEntry entry) {
        entries.add(entry);
    }

    void add(GroupDetails group) {
        details.add(group);
    }

    @Nullable
    Filter lastFilter() {
        return lastFilter;
    }

    @Override
    public Page<GroupCatalogEntry> findCatalogPage(
            @Nullable String letter,
            @Nullable UUID countryId,
            @Nullable UUID cityId,
            @Nullable String platform,
            Pageable pageable) {
        lastFilter = new Filter(letter, countryId, cityId, platform);
        int from = (int) Math.min(pageable.getOffset(), entries.size());
        int to = Math.min(from + pageable.getPageSize(), entries.size());
        return new PageImpl<>(entries.subList(from, to), pageable, entries.size());
    }

    @Override
    public Optional<GroupDetails> findById(UUID id) {
        return details.stream().filter(group -> group.id().equals(id)).findFirst();
    }
}
