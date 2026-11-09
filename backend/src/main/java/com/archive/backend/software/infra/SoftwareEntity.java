package com.archive.backend.software.infra;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;

@Entity
@Table(name = "software")
@Data
public class SoftwareEntity {
    /** Assigned by the domain, never generated. */
    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;

    /** Set by the database default. */
    @Column(name = "created_at", columnDefinition = "timestamptz", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "platform_code", nullable = false)
    private String platformCode;
}
