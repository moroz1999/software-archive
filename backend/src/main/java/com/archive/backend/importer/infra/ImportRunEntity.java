package com.archive.backend.importer.infra;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

    /** The programs added, in the order the run took them. */
    @ElementCollection
    @CollectionTable(name = "import_run_added", joinColumns = @JoinColumn(name = "run_id"))
    @OrderColumn(name = "position")
    private List<ImportRunAddedRow> addedPrograms = new ArrayList<>();

    /** The programs failed, in the order the run met them. */
    @ElementCollection
    @CollectionTable(name = "import_run_failed", joinColumns = @JoinColumn(name = "run_id"))
    @OrderColumn(name = "position")
    private List<ImportRunFailedRow> failedPrograms = new ArrayList<>();

    /** The programs found in the archive, in the order the run met them. */
    @ElementCollection
    @CollectionTable(name = "import_run_known", joinColumns = @JoinColumn(name = "run_id"))
    @OrderColumn(name = "position")
    private List<ImportRunKnownRow> knownPrograms = new ArrayList<>();

    /** The programs left out, in the order the run met them. */
    @ElementCollection
    @CollectionTable(name = "import_run_skipped", joinColumns = @JoinColumn(name = "run_id"))
    @OrderColumn(name = "position")
    private List<ImportRunSkippedRow> skippedPrograms = new ArrayList<>();

    /** The authors taken for the first time, in the order the run took them. */
    @ElementCollection
    @CollectionTable(name = "import_run_author", joinColumns = @JoinColumn(name = "run_id"))
    @OrderColumn(name = "position")
    private List<ImportRunAuthorRow> authors = new ArrayList<>();

    /** The places added, in the order the run added them. */
    @ElementCollection
    @CollectionTable(name = "import_run_place", joinColumns = @JoinColumn(name = "run_id"))
    @OrderColumn(name = "position")
    private List<ImportRunPlaceRow> places = new ArrayList<>();

    @Column(name = "progress_position", nullable = false)
    private int progressPosition;

    @Column(name = "progress_total")
    private @Nullable Integer progressTotal;

    @Column(name = "progress_url")
    private @Nullable String progressUrl;
}
