package com.archive.backend.software.infra;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "software")
@Data
public class SoftwareEntity {
    /**
     * Assigned by the domain, never generated.
     */
    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    private @Nullable String description;

    /**
     * Set by the database default.
     */
    @Column(name = "created_at", columnDefinition = "timestamptz", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "release_date")
    private @Nullable LocalDate releaseDate;

    @Column(name = "platform_code", nullable = false)
    private String platformCode;

    /**
     * The base hardware the software is released for.
     */
    @ElementCollection
    @CollectionTable(name = "software_hardware", joinColumns = @JoinColumn(name = "software_id"))
    @Column(name = "hardware_code")
    private Set<String> hardware = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "software_id", nullable = false)
    private List<SoftwareReleaseEntity> releases = new ArrayList<>();
}
