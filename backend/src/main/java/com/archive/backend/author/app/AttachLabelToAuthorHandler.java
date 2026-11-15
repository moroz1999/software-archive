package com.archive.backend.author.app;

import com.archive.backend.author.domain.Author;
import com.archive.backend.author.domain.AuthorRepository;
import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelNotFoundException;
import com.archive.backend.label.domain.LabelOwnedException;
import com.archive.backend.label.domain.LabelOwnerUnknownException;
import com.archive.backend.label.domain.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Attaches a label of nobody to the author found out to be its owner (docs/domain/authors.md#label). */
@Service
@RequiredArgsConstructor
public class AttachLabelToAuthorHandler {

    private final AuthorRepository authors;
    private final LabelRepository labels;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(AttachLabelToAuthorCommand c) {
        Label label = labels.lockOfNobody(c.label().label())
                .orElseThrow(() ->
                        labels.exists(c.label().label()) ? new LabelOwnedException() : new LabelNotFoundException());
        Author author = authors.findById(c.author()).orElseThrow(LabelOwnerUnknownException::new);
        author.requireChangeableBy(c.actor());
        author.adopt(label, c.label().display());
        authors.save(author);
        author.pullEvents().forEach(events::publishEvent);
    }
}
