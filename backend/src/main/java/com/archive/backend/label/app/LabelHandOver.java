package com.archive.backend.label.app;

import com.archive.backend.author.domain.Author;
import com.archive.backend.author.domain.AuthorRepository;
import com.archive.backend.group.domain.Group;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.domain.GroupRepository;
import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelNotFoundException;
import com.archive.backend.label.domain.LabelOfPersonException;
import com.archive.backend.label.domain.LabelOwnedException;
import com.archive.backend.label.domain.LabelOwnerUnknownException;
import com.archive.backend.label.domain.LabelRepository;
import com.archive.backend.user.domain.Actor;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Takes a label away from whoever has it; only someone who may change that owner takes it away
 * (docs/domain/authors.md#label).
 */
final class LabelHandOver {

    private LabelHandOver() {}

    /**
     * The label, taken away from whoever has it, to be made into an owner: only whoever added the label, and
     * administrators, make one of it.
     */
    static Label takeAway(
            LabelId id,
            Actor actor,
            AuthorRepository authors,
            GroupRepository groups,
            LabelRepository labels,
            ApplicationEventPublisher events) {
        labels.lock(id).orElseThrow(LabelNotFoundException::new).requireChangeableBy(actor);
        release(id, actor, authors, groups, events);
        return labels.lockOfNobody(id).orElseThrow(LabelOwnedException::new);
    }

    /**
     * Lets a label be given to a group only by an administrator, the label and the group being in the archive and the
     * label not a person's.
     */
    static void requireGivableToGroup(
            LabelId label,
            GroupId group,
            Actor actor,
            AuthorRepository authors,
            GroupRepository groups,
            LabelRepository labels) {
        actor.administrator();
        if (labels.lock(label).isEmpty()) {
            throw new LabelNotFoundException();
        }
        if (authors.findByLabel(label).isPresent()) {
            throw new LabelOfPersonException();
        }
        if (groups.findById(group).isEmpty()) {
            throw new LabelOwnerUnknownException().onField("group");
        }
    }

    /** Leaves the label to nobody, if somebody has it. */
    static void release(
            LabelId id,
            Actor actor,
            AuthorRepository authors,
            GroupRepository groups,
            ApplicationEventPublisher events) {
        authors.findByLabel(id).ifPresent(author -> release(author, id, actor, authors, events));
        groups.findByLabel(id).ifPresent(group -> release(group, id, actor, groups, events));
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
