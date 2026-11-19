package com.archive.backend.author.app;

import com.archive.backend.author.domain.Author;
import com.archive.backend.author.domain.AuthorNotFoundException;
import com.archive.backend.author.domain.AuthorRepository;
import com.archive.backend.geo.domain.Places;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Fills the empty fields of a person from an import (docs/domain/imports.md#authors). */
@Service
@RequiredArgsConstructor
public class SupplementAuthorHandler {

    private final AuthorRepository repository;
    private final Places places;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(SupplementAuthorCommand c) {
        Author author = repository.findById(c.id()).orElseThrow(AuthorNotFoundException::new);
        author.requireChangeableBy(c.actor());
        author.supplement(c.realName(), places.locations(c.locations()));
        repository.save(author);
        author.pullEvents().forEach(events::publishEvent);
    }
}
