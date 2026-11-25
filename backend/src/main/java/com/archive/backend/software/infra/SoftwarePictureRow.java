package com.archive.backend.software.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A picture of a gallery of software, its place kept by the order of the list holding it: the galleries one after
 * another, each in its order.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SoftwarePictureRow {

    @Column(nullable = false)
    private UUID id;

    @Column(nullable = false)
    private String format;

    /** The name of the file the picture came in. */
    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private long size;

    /** The code of its gallery. */
    @Column(nullable = false)
    private String gallery;
}
