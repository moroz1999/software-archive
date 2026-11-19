package com.archive.backend.software.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** A screenshot of software or of a release, its place kept by the order of the list holding it. */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreenshotRow {

    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String format;

    /** The name of the file the picture came in. */
    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private long size;
}
