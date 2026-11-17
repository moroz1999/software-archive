package com.archive.backend.label.app;

import com.archive.backend.author.domain.Author;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.domain.AuthorRepository;
import com.archive.backend.geo.domain.Places;
import com.archive.backend.group.domain.Group;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupRepository;
import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelNotFoundException;
import com.archive.backend.label.domain.LabelOwnerChoice;
import com.archive.backend.label.domain.LabelOwnerUnknownException;
import com.archive.backend.label.domain.LabelRepository;
import com.archive.backend.shared.domain.error.RuleViolationException;
import com.archive.backend.user.domain.Actor;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gives a label its name, its owner and its locations: whoever added it, and administrators, rename and relocate it;
 * only administrators change whose it is, by the rules of taking it away and attaching it
 * (docs/domain/authors.md#editing-a-label).
 */
@Service
@RequiredArgsConstructor
public class EditLabelHandler {

    /** The field of the label form that a refusal of the owner falls under. */
    private static final String OWNER = "owner";

    private final LabelRepository labels;
    private final AuthorRepository authors;
    private final GroupRepository groups;
    private final Places places;
    private final ApplicationEventPublisher events;

    @Transactional
    public void handle(EditLabelCommand c) {
        Label label = labels.lock(c.id()).orElseThrow(LabelNotFoundException::new);
        label.requireChangeableBy(c.actor());
        label.edit(c.name(), places.locations(c.locations()), c.actor());
        if (!isOwner(c.owner(), c.id())) {
            label.requireOwnerChangeableBy(c.actor());
            try {
                LabelHandOver.release(c.id(), c.actor(), authors, groups, events);
                LabelOwnerChoice owner = c.owner();
                if (owner != null) {
                    attach(label, owner, c.actor());
                }
            } catch (RuleViolationException refusal) {
                throw refusal.onField(OWNER);
            }
        }
        labels.save(label);
        label.pullEvents().forEach(events::publishEvent);
    }

    /** Whether the label is the owner's already: then the form changes nothing of whose it is. */
    private boolean isOwner(@Nullable LabelOwnerChoice owner, LabelId label) {
        Optional<Author> author = authors.findByLabel(label);
        Optional<Group> group = groups.findByLabel(label);
        if (owner == null) {
            return author.isEmpty() && group.isEmpty();
        }
        return switch (owner.kind()) {
            case AUTHOR -> author.map(a -> a.getId().value().equals(owner.id())).orElse(false);
            case GROUP -> group.map(g -> g.getId().value().equals(owner.id())).orElse(false);
        };
    }

    private void attach(Label label, LabelOwnerChoice owner, Actor actor) {
        switch (owner.kind()) {
            case AUTHOR -> {
                Author author = authors.findById(new AuthorId(owner.id())).orElseThrow(LabelOwnerUnknownException::new);
                author.requireChangeableBy(actor);
                author.adopt(label, owner.display());
                authors.save(author);
                author.pullEvents().forEach(events::publishEvent);
            }
            case GROUP -> {
                Group group = groups.findById(new GroupId(owner.id())).orElseThrow(LabelOwnerUnknownException::new);
                group.requireChangeableBy(actor);
                group.adopt(label);
                groups.save(group);
                group.pullEvents().forEach(events::publishEvent);
            }
        }
    }
}
