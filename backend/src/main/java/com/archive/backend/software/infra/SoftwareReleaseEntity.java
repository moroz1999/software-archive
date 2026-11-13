package com.archive.backend.software.infra;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import org.jspecify.annotations.Nullable;

/**
 * A release, stored with its software (docs/domain/software.md).
 */
@Entity
@Table(name = "software_release")
@Data
public class SoftwareReleaseEntity {

    /**
     * Assigned by the domain, never generated.
     */
    @Id
    private UUID id;

    private @Nullable String title;

    private @Nullable String version;

    @Column(nullable = false)
    private String type;

    @Column(name = "release_year")
    private @Nullable Integer releaseYear;

    @Column(name = "release_day")
    private @Nullable LocalDate releaseDay;

    private @Nullable String description;

    @Column(name = "archive_id")
    private @Nullable UUID archiveId;

    @Column(name = "archive_file_name")
    private @Nullable String archiveFileName;

    @Column(name = "archive_size")
    private @Nullable Long archiveSize;

    /** Who added it; the database forgets them once their account is gone, so it is never written back. */
    @Column(name = "added_by", updatable = false)
    private @Nullable UUID addedBy;

    @ElementCollection
    @CollectionTable(name = "software_release_hardware", joinColumns = @JoinColumn(name = "release_id"))
    @Column(name = "hardware_code")
    private Set<String> hardware = new HashSet<>();

    /** The screenshots in their order. */
    @ElementCollection
    @CollectionTable(name = "software_release_screenshot", joinColumns = @JoinColumn(name = "release_id"))
    @OrderColumn(name = "position")
    private List<ScreenshotRow> screenshots = new ArrayList<>();
}
