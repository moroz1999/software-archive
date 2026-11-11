package com.archive.backend.author.app;

import com.archive.backend.author.domain.Author;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.domain.AuthorRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

class InMemoryAuthorRepository implements AuthorRepository {

    private final Map<AuthorId, Author> items = new LinkedHashMap<>();

    int count() {
        return items.size();
    }

    @Override
    public Optional<Author> findById(AuthorId id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public void save(Author author) {
        items.put(author.getId(), author);
    }

    @Override
    public void delete(Author author) {
        items.remove(author.getId());
    }
}
