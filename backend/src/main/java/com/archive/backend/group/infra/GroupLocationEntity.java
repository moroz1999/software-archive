package com.archive.backend.group.infra;

import com.archive.backend.geo.infra.LocationEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * A location row of a group.
 */
@Entity
@Table(name = "entity_location")
public class GroupLocationEntity extends LocationEntity {

    @Override
    protected String ownerType() {
        return "GROUP";
    }
}
