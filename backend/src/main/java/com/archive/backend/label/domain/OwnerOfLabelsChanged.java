package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.ddd.DomainEvent;
import java.util.Set;

/**
 * The owner of these labels was edited or deleted: where the labels' works come from, and whose they are, may have
 * changed. The labels are those the owner had before; new labels have no works yet.
 */
public record OwnerOfLabelsChanged(Set<LabelId> labels) implements DomainEvent {

    public OwnerOfLabelsChanged {
        labels = Set.copyOf(labels);
    }
}
