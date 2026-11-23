package com.archive.backend.author.app;

import com.archive.backend.author.domain.Author;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.domain.AuthorRepository;
import com.archive.backend.geo.domain.Places;
import com.archive.backend.user.domain.AddedBy;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateAuthorHandler {

    private final AuthorRepository repository;
    private final Places places;
    private final ApplicationEventPublisher events;

    @Transactional
    public AuthorId handle(CreateAuthorCommand c) {
        AddedBy addedBy = AddedBy.of(c.actor());
        AuthorId id = new AuthorId(UUID.randomUUID());
        Author author =
                Author.create(id, c.realName(), c.notes(), c.labels(), places.locations(c.locations()), addedBy);
        repository.save(author);
        author.pullEvents().forEach(events::publishEvent);
        return author.getId();
    }
}
