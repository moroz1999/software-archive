package com.archive.backend.author.app;

import com.archive.backend.author.domain.Author;
import com.archive.backend.author.domain.AuthorNotFoundException;
import com.archive.backend.author.domain.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteAuthorHandler {

    private final AuthorRepository repository;

    @Transactional
    public void handle(DeleteAuthorCommand c) {
        Author author = repository.findById(c.id()).orElseThrow(AuthorNotFoundException::new);
        repository.delete(author);
    }
}
