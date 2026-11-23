package com.archive.backend.label.app;

import com.archive.backend.author.domain.Author;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.domain.AuthorRepository;
import com.archive.backend.group.domain.GroupRepository;
import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelRepository;
import com.archive.backend.user.domain.AddedBy;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Replaces a label with a person of the same name and transfers its relations (docs/domain/authors.md#label). */
@Service
@RequiredArgsConstructor
public class MakeAuthorOfLabelHandler {

    private final AuthorRepository authors;
    private final GroupRepository groups;
    private final LabelRepository labels;
    private final ApplicationEventPublisher events;

    @Transactional
    public AuthorId handle(MakeAuthorOfLabelCommand c) {
        AddedBy addedBy = AddedBy.of(c.actor());
        Label label = LabelHandOver.takeAway(c.label(), c.actor(), authors, groups, labels, events);
        AuthorId id = new AuthorId(UUID.randomUUID());
        Author author = Author.replacing(id, label, addedBy);
        authors.save(author);
        // The answers move the label's credits, memberships and origin to the person before the label goes.
        author.pullEvents().forEach(events::publishEvent);
        labels.delete(label);
        return author.getId();
    }
}
