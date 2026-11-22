package com.archive.backend.search.app;

import com.archive.backend.author.domain.AuthorChanged;
import com.archive.backend.category.domain.CategoryDeleted;
import com.archive.backend.category.domain.CategoryMoved;
import com.archive.backend.geo.domain.CityMoved;
import com.archive.backend.group.domain.GroupChanged;
import com.archive.backend.label.domain.LabelChanged;
import com.archive.backend.label.domain.LabelCreated;
import com.archive.backend.label.domain.LabelDeleted;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.OwnerOfLabelsChanged;
import com.archive.backend.search.domain.AffectedOwners;
import com.archive.backend.search.domain.AffectedSoftware;
import com.archive.backend.search.domain.SearchEntity;
import com.archive.backend.search.domain.SearchEntityType;
import com.archive.backend.search.domain.SearchOutbox;
import com.archive.backend.software.domain.SoftwareChanged;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Writes the tasks of the search documents a change reaches, in the transaction of the change
 * (docs/features/search.md#synchronization).
 */
@Service
@RequiredArgsConstructor
public class EnqueueSearchDocumentsHandler {

    private final SearchOutbox outbox;
    private final AffectedSoftware affected;
    private final AffectedOwners owners;

    /** The software, and the people, groups and labels of nobody credited on it: their platforms may have changed. */
    @EventListener
    public void on(SoftwareChanged event) {
        UUID software = event.software().value();
        enqueueSoftware(List.of(software));
        enqueue(SearchEntityType.AUTHOR, owners.peopleCreditedOn(software));
        enqueue(SearchEntityType.GROUP, owners.groupsCreditedOn(software));
        enqueue(SearchEntityType.AUTHOR, owners.labelsOfNobodyCreditedOn(software));
    }

    /** A new label of nobody is found among the people. */
    @EventListener
    public void on(LabelCreated event) {
        enqueue(SearchEntityType.AUTHOR, List.of(event.label().value()));
    }

    /**
     * The software credited on the label, which its card names, the label itself and its owner: a renamed label is
     * found under its new name.
     */
    @EventListener
    public void on(LabelChanged event) {
        enqueueLabel(event.label().value());
    }

    /** Told while its credits are still there: what they reach is found before they go. */
    @EventListener
    public void on(LabelDeleted event) {
        enqueueLabel(event.label().value());
    }

    private void enqueueLabel(UUID label) {
        enqueueSoftware(affected.creditedOn(List.of(label)));
        enqueue(SearchEntityType.AUTHOR, List.of(label));
        enqueue(SearchEntityType.AUTHOR, owners.peopleOwning(label));
        enqueue(SearchEntityType.GROUP, owners.groupsOwning(label));
    }

    /** The person, and the software crediting them by themselves: its card names them. */
    @EventListener
    public void on(AuthorChanged event) {
        UUID author = event.author().value();
        enqueue(SearchEntityType.AUTHOR, List.of(author));
        enqueueSoftware(affected.creditingPerson(author));
    }

    /** The group, and the software crediting it by itself: its card names it, its countries may have changed. */
    @EventListener
    public void on(GroupChanged event) {
        UUID group = event.group().value();
        enqueue(SearchEntityType.GROUP, List.of(group));
        enqueueSoftware(affected.creditingGroup(group));
    }

    /** A city put in another country changes the country of the software made by whoever is located in it. */
    @EventListener
    public void on(CityMoved event) {
        enqueueSoftware(affected.creditedFromCity(event.city().value()));
    }

    @EventListener
    public void on(CategoryMoved event) {
        enqueueSoftware(affected.inSubtreeOf(event.category().value()));
    }

    @EventListener
    public void on(CategoryDeleted event) {
        enqueueSoftware(affected.inSubtreeOf(event.category().value()));
    }

    /**
     * The software credited on the labels, and the labels themselves: a label given an owner is no longer found by
     * itself, a label left without one is again.
     */
    @EventListener
    public void on(OwnerOfLabelsChanged event) {
        List<UUID> labels = event.labels().stream().map(LabelId::value).toList();
        if (!labels.isEmpty()) {
            enqueueSoftware(affected.creditedOn(labels));
            enqueue(SearchEntityType.AUTHOR, labels);
        }
    }

    private void enqueueSoftware(Collection<UUID> software) {
        enqueue(SearchEntityType.SOFTWARE, software);
    }

    private void enqueue(SearchEntityType type, Collection<UUID> ids) {
        if (!ids.isEmpty()) {
            outbox.enqueue(ids.stream().map(id -> new SearchEntity(type, id)).toList());
        }
    }
}
