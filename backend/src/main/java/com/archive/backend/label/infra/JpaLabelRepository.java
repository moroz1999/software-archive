package com.archive.backend.label.infra;

import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelRepository;
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
}
