package com.archive.backend.author.app;

import com.archive.backend.author.domain.AuthorNotFoundException;
import com.archive.backend.author.read.AuthorDetails;
import com.archive.backend.author.read.AuthorReadRepository;
import com.archive.backend.user.domain.AddedBy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAuthorHandler {

    private final AuthorReadRepository reads;

    @Transactional(readOnly = true)
    public AuthorDetails handle(GetAuthorQuery q) {
        AuthorDetails author = reads.findById(q.id().value()).orElseThrow(AuthorNotFoundException::new);
        return author.withCanChange(AddedBy.of(author.addedBy()).mayBeChangedBy(q.actor()));
    }
}
