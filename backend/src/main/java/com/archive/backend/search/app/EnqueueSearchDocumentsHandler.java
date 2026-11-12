package com.archive.backend.search.app;

import com.archive.backend.author.domain.AuthorChanged;
import com.archive.backend.category.domain.CategoryDeleted;
import com.archive.backend.category.domain.CategoryMoved;
import com.archive.backend.group.domain.GroupChanged;
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

    /** The software, and the people and groups credited on it: their platforms may have changed. */
    @EventListener
    public void on(SoftwareChanged event) {
        UUID software = event.software().value();
        enqueueSoftware(List.of(software));
        enqueue(SearchEntityType.AUTHOR, owners.peopleCreditedOn(software));
        enqueue(SearchEntityType.GROUP, owners.groupsCreditedOn(software));
    }

    @EventListener
    public void on(AuthorChanged event) {
        enqueue(SearchEntityType.AUTHOR, List.of(event.author().value()));
    }

    @EventListener
    public void on(GroupChanged event) {
        enqueue(SearchEntityType.GROUP, List.of(event.group().value()));
    }

    @EventListener
    public void on(CategoryMoved event) {
        enqueueSoftware(affected.inSubtreeOf(event.category().value()));
    }

    @EventListener
    public void on(CategoryDeleted event) {
        enqueueSoftware(affected.inSubtreeOf(event.category().value()));
    }

    @EventListener
    public void on(OwnerOfLabelsChanged event) {
        if (!event.labels().isEmpty()) {
            enqueueSoftware(affected.creditedOn(
                    event.labels().stream().map(LabelId::value).toList()));
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
