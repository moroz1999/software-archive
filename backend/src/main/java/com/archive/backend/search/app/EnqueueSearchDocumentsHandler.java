package com.archive.backend.search.app;

import com.archive.backend.category.domain.CategoryDeleted;
import com.archive.backend.category.domain.CategoryMoved;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.OwnerOfLabelsChanged;
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

    @EventListener
    public void on(SoftwareChanged event) {
        enqueueSoftware(List.of(event.software().value()));
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
        if (!software.isEmpty()) {
            outbox.enqueue(software.stream()
                    .map(id -> new SearchEntity(SearchEntityType.SOFTWARE, id))
                    .toList());
        }
    }
}
