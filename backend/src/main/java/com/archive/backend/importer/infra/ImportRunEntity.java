package com.archive.backend.importer.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "import_run")
@Data
public class ImportRunEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String archive;

    private @Nullable String section;

    @Column(nullable = false)
    private String state;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "ended_at")
    private @Nullable Instant endedAt;

    @Column(nullable = false)
    private int added;

    @Column(nullable = false)
    private int known;

    @Column(nullable = false)
    private int skipped;

    @Column(nullable = false)
    private int failed;

    @Column(name = "progress_position", nullable = false)
    private int progressPosition;

    @Column(name = "progress_total")
    private @Nullable Integer progressTotal;

    @Column(name = "progress_url")
    private @Nullable String progressUrl;
}
