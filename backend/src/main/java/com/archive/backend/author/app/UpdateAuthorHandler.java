package com.archive.backend.author.app;

import com.archive.backend.author.domain.Author;
import com.archive.backend.author.domain.AuthorNotFoundException;
import com.archive.backend.author.domain.AuthorRepository;
import com.archive.backend.geo.domain.Places;
import com.archive.backend.user.domain.AddedBy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UpdateAuthorHandler {

    private final AuthorRepository repository;
    private final Places places;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(UpdateAuthorCommand c) {
        Author author = repository.findById(c.id()).orElseThrow(AuthorNotFoundException::new);
        author.requireChangeableBy(c.actor());
        author.edit(c.realName(), c.notes(), c.labels(), places.locations(c.locations()), AddedBy.of(c.actor()));
        repository.save(author);
        author.pullEvents().forEach(events::publishEvent);
    }
}
