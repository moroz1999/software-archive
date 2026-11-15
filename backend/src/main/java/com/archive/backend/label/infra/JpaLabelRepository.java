package com.archive.backend.label.infra;

import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.label.domain.LabelRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Stores a label of nobody: its row has no owner, and so no display name to mark.
 */
@Repository
@RequiredArgsConstructor
public class JpaLabelRepository implements LabelRepository {

    private final SpringDataLabelRepository springDataLabelRepository;

    @Override
    public void save(Label label) {
        LabelEntity row = new LabelEntity();
        row.setId(label.getId().value());
        row.setName(label.getName().value());
        springDataLabelRepository.saveAndFlush(row);
    }

    @Override
    public Optional<Label> lockOfNobody(LabelId id) {
        return springDataLabelRepository
                .lockOfNobody(id.value())
                .map(row -> Label.restore(new LabelId(row.getId()), new LabelName(row.getName())));
    }

    @Override
    public boolean exists(LabelId id) {
        return springDataLabelRepository.existsById(id.value());
    }
}
