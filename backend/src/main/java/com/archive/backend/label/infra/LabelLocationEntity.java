package com.archive.backend.label.infra;

import com.archive.backend.geo.infra.LocationEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

/**
 * A location row of a label. The label's repository writes them by the label's id alone, so that saving an owner,
 * which maps the label's row too, never touches them.
 */
@Entity
@Table(name = "entity_location")
@Getter
@Setter
public class LabelLocationEntity extends LocationEntity {

    static final String TYPE = "LABEL";

    @Column(name = "entity_id", nullable = false, updatable = false)
    private UUID labelId;

    @Override
    protected String ownerType() {
        return TYPE;
    }
}
