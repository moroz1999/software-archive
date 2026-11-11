package com.archive.backend.author.app;

import com.archive.backend.author.domain.Author;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.domain.AuthorRepository;
import com.archive.backend.geo.domain.Places;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateAuthorHandler {

    private final AuthorRepository repository;
    private final Places places;

    @Transactional
    public AuthorId handle(CreateAuthorCommand c) {
        AuthorId id = new AuthorId(UUID.randomUUID());
        repository.save(Author.create(id, c.realName(), c.notes(), c.labels(), places.locations(c.locations())));
        return id;
    }
}
