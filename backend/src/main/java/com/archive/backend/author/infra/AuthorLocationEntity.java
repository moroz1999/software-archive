package com.archive.backend.author.infra;

import com.archive.backend.geo.infra.LocationEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * A location row of a person.
 */
@Entity
@Table(name = "entity_location")
public class AuthorLocationEntity extends LocationEntity {

    @Override
    protected String ownerType() {
        return "PERSON";
    }
}
