package com.archive.backend.label.infra;

import com.archive.backend.geo.infra.LocationRows;
import com.archive.backend.label.domain.Label;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.label.domain.LabelName;
import com.archive.backend.label.domain.LabelRepository;
import com.archive.backend.user.infra.AddedByRows;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * Stores the name, the locations and the adder of a label; the owner columns and the display name are its owner's to
 * write, so the row of a label in the archive is changed, never replaced.
 */
@Repository
@RequiredArgsConstructor
public class JpaLabelRepository implements LabelRepository {

    private final SpringDataLabelRepository springDataLabelRepository;
    private final SpringDataLabelLocationRepository springDataLabelLocationRepository;

    @Override
    public void save(Label label) {
        LabelEntity row = springDataLabelRepository
                .findById(label.getId().value())
                .orElseGet(() -> {
                    LabelEntity created = new LabelEntity();
                    created.setId(label.getId().value());
                    created.setAddedBy(AddedByRows.fromAddedBy(label.getAddedBy()));
                    return created;
                });
        row.setName(label.getName().value());
        springDataLabelRepository.saveAndFlush(row);
        springDataLabelLocationRepository.deleteOfLabel(label.getId().value());
        springDataLabelLocationRepository.saveAllAndFlush(
                LocationRows.fromLocations(label.getLocations(), () -> located(label)));
    }

    private static LabelLocationEntity located(Label label) {
        LabelLocationEntity row = new LabelLocationEntity();
        row.setLabelId(label.getId().value());
        return row;
    }

    @Override
    public Optional<Label> lock(LabelId id) {
        return springDataLabelRepository.lock(id.value()).map(this::toDomain);
    }

    @Override
    public Optional<Label> lockOfNobody(LabelId id) {
        return springDataLabelRepository.lockOfNobody(id.value()).map(this::toDomain);
    }

    @Override
    public boolean exists(LabelId id) {
        return springDataLabelRepository.existsById(id.value());
    }

    @Override
    public void delete(Label label) {
        springDataLabelLocationRepository.deleteOfLabel(label.getId().value());
        springDataLabelRepository.deleteById(label.getId().value());
        springDataLabelRepository.flush();
    }

    private Label toDomain(LabelEntity row) {
        return Label.restore(
                new LabelId(row.getId()),
                new LabelName(row.getName()),
                LocationRows.toLocations(springDataLabelLocationRepository.findOfLabel(row.getId())),
                AddedByRows.toAddedBy(row.getAddedBy()));
    }
}
