package com.archive.backend.label.infra;

import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.label.domain.OwnLabel;
import com.archive.backend.label.domain.OwnLabels;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * The labels of an owner ↔ their rows. Labels are restored without the checks: they were checked when stored.
 */
public final class LabelRows {

    private LabelRows() {}

    /**
     * Records from before the rule may have no label or several marked: the display name is then the first marked,
     * or the label shown as the main name, the first by name (docs/domain/authors.md).
     */
    public static OwnLabels toLabels(List<LabelEntity> rows) {
        LabelId display = rows.stream()
                .filter(label -> Boolean.TRUE.equals(label.getDisplay()))
                .findFirst()
                .or(() -> rows.stream()
                        .min(Comparator.comparing(
                                        (LabelEntity label) -> label.getName().toLowerCase(Locale.ROOT))
                                .thenComparing(LabelEntity::getId)))
                .map(label -> new LabelId(label.getId()))
                .orElse(null);
        return OwnLabels.restore(
                rows.stream()
                        .map(label -> new OwnLabel(new LabelId(label.getId()), new LabelName(label.getName())))
                        .toList(),
                display);
    }

    public static List<LabelEntity> fromLabels(OwnLabels labels) {
        LabelId display = labels.display();
        return labels.all().stream()
                .map(label -> {
                    LabelEntity row = new LabelEntity();
                    row.setId(label.id().value());
                    row.setName(label.name().value());
                    row.setDisplay(label.id().equals(display));
                    return row;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }
}
