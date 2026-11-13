package com.archive.backend.label.app;

import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

class InMemoryLabelRepository implements LabelRepository {

    private final Map<LabelId, Label> items = new LinkedHashMap<>();

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
}
