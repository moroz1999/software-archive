package com.archive.backend.label.app;

import com.archive.backend.author.domain.Author;
import com.archive.backend.author.domain.AuthorRepository;
import com.archive.backend.group.domain.Group;
import com.archive.backend.group.domain.GroupRepository;
import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelNotFoundException;
import com.archive.backend.label.domain.LabelOwnedException;
import com.archive.backend.label.domain.LabelRepository;
import com.archive.backend.user.domain.Actor;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Takes a label away from whoever has it, so that a new owner made of it gets it with its works; only someone who may
 * change that owner takes it away (docs/domain/authors.md#label).
 */
final class LabelHandOver {

    private LabelHandOver() {}

    static Label takeAway(
            LabelId id,
            Actor actor,
            AuthorRepository authors,
            GroupRepository groups,
            LabelRepository labels,
            ApplicationEventPublisher events) {
        if (!labels.exists(id)) {
            throw new LabelNotFoundException();
        }
        authors.findByLabel(id).ifPresent(author -> release(author, id, actor, authors, events));
        groups.findByLabel(id).ifPresent(group -> release(group, id, actor, groups, events));
        return labels.lockOfNobody(id).orElseThrow(LabelOwnedException::new);
    }

    private static void release(
            Author author, LabelId id, Actor actor, AuthorRepository authors, ApplicationEventPublisher events) {
        author.requireChangeableBy(actor);
        author.release(id);
        authors.save(author);
        author.pullEvents().forEach(events::publishEvent);
    }

    private static void release(
            Group group, LabelId id, Actor actor, GroupRepository groups, ApplicationEventPublisher events) {
        group.requireChangeableBy(actor);
        group.release(id);
        groups.save(group);
        group.pullEvents().forEach(events::publishEvent);
    }
}
