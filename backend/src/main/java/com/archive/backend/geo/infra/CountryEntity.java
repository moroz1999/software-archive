package com.archive.backend.geo.infra;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.jspecify.annotations.Nullable;

/** A country with its names by the codes of their languages. */
@Entity
@Table(name = "country")
@Data
public class CountryEntity {

    /** Assigned by the domain, never generated. */
    @Id
    private UUID id;

    @ElementCollection
    @CollectionTable(name = "country_name", joinColumns = @JoinColumn(name = "country_id"))
    @MapKeyColumn(name = "language")
    @Column(name = "name", nullable = false)
    private Map<String, String> names = new HashMap<>();

    private @Nullable Double latitude;

    private @Nullable Double longitude;
}
