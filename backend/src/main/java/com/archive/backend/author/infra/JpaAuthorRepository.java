package com.archive.backend.author.infra;

import com.archive.backend.author.domain.Author;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.domain.AuthorRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaAuthorRepository implements AuthorRepository {

    private final SpringDataAuthorRepository springDataAuthorRepository;
    private final AuthorMapper mapper;

    /**
     * A person without any name is not an author (docs/domain/authors.md): nothing to change.
     */
    @Override
    public Optional<Author> findById(AuthorId id) {
        return springDataAuthorRepository
                .findById(id.value())
                .filter(entity ->
                        entity.getRealName() != null || !entity.getLabels().isEmpty())
                .map(mapper::toDomain);
    }

    @Override
    public void save(Author author) {
        springDataAuthorRepository.save(mapper.toEntity(author));
    }

    @Override
    public void delete(Author author) {
        springDataAuthorRepository.deleteById(author.getId().value());
    }
}
