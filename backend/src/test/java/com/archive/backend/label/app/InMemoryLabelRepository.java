package com.archive.backend.label.app;

import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelRepository;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Labels of nobody kept in memory, beside the ids of labels some owner has. */
public class InMemoryLabelRepository implements LabelRepository {

    private final Map<LabelId, Label> items = new LinkedHashMap<>();
    private final Set<LabelId> owned = new HashSet<>();

    /** A label some owner has: it exists, but is nobody's no longer. */
    public void own(LabelId id) {
        owned.add(id);
    }

    int count() {
        return items.size();
    }

    Optional<Label> findById(LabelId id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public void save(Label label) {
        items.put(label.getId(), label);
    }

    @Override
    public Optional<Label> lockOfNobody(LabelId id) {
        return owned.contains(id) ? Optional.empty() : Optional.ofNullable(items.get(id));
    }

    @Override
    public boolean exists(LabelId id) {
        return items.containsKey(id) || owned.contains(id);
    }

    @Override
    public Optional<Label> lock(LabelId id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public void delete(Label label) {
        items.remove(label.getId());
        owned.remove(label.getId());
    }
}
