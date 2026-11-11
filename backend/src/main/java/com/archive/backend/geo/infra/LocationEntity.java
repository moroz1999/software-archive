package com.archive.backend.geo.infra;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.util.UUID;
import lombok.Data;
import org.hibernate.annotations.Formula;
import org.jspecify.annotations.Nullable;

/**
 * A location row of an owner. The table is shared by several kinds of owners, told apart by the type; every kind maps
 * its rows with an entity of its own, as one entity cannot be the rows of two owners. A location has no identity in
 * the domain: the rows are replaced on every save.
 */
@MappedSuperclass
@Data
public abstract class LocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "entity_type", nullable = false, updatable = false)
    private String entityType;

    @Column(name = "country_id")
    private @Nullable UUID countryId;

    @Column(name = "city_id")
    private @Nullable UUID cityId;

    /**
     * The country of the place: the one given, else the one of its city.
     */
    @Formula("COALESCE(country_id, (SELECT c.country_id FROM city c WHERE c.id = city_id))")
    private @Nullable UUID placeCountryId;

    @Column(name = "is_primary")
    private @Nullable Boolean primary;

    /**
     * The type the owner's rows have in the shared table.
     */
    protected abstract String ownerType();

    @PrePersist
    void markOwnerType() {
        entityType = ownerType();
    }
}
