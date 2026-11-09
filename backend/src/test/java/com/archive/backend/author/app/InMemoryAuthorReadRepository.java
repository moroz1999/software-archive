package com.archive.backend.author.app;

import com.archive.backend.author.read.AuthorCatalogEntry;
import com.archive.backend.author.read.AuthorDetails;
import com.archive.backend.author.read.AuthorReadRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Serves what it was given and remembers the filter it was asked with: filtering itself is the SQL's job. */
class InMemoryAuthorReadRepository implements AuthorReadRepository {

    record Filter(@Nullable String letter, @Nullable UUID countryId, @Nullable UUID cityId) {
    }

    private final List<AuthorCatalogEntry> entries = new ArrayList<>();
    private final List<AuthorDetails> details = new ArrayList<>();
    private @Nullable Filter lastFilter;

    void add(AuthorCatalogEntry entry) {
        entries.add(entry);
    }

    void add(AuthorDetails author) {
        details.add(author);
    }

    @Nullable Filter lastFilter() {
        return lastFilter;
    }

    @Override
    public Page<AuthorCatalogEntry> findCatalogPage(@Nullable String letter, @Nullable UUID countryId, @Nullable UUID cityId, Pageable pageable) {
        lastFilter = new Filter(letter, countryId, cityId);
        int from = (int) Math.min(pageable.getOffset(), entries.size());
        int to = Math.min(from + pageable.getPageSize(), entries.size());
        return new PageImpl<>(entries.subList(from, to), pageable, entries.size());
    }

    @Override
    public Optional<AuthorDetails> findById(UUID id) {
        return details.stream().filter(author -> author.id().equals(id)).findFirst();
    }
}
