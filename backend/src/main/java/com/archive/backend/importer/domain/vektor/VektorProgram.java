package com.archive.backend.importer.domain.vektor;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * A program as vektor-06c.ru shows it on its page: the lines of its card as they are written there.
 *
 * @param id the id of the program on the site
 * @param section the code of its section in the address of the page
 * @param archiveUrl the absolute address of its ZIP archive; a program may have none
 * @param archiveFiles the names of the files inside the archive
 */
@ValueObject
public record VektorProgram(
        String id,
        String section,
        String title,
        String authors,
        String year,
        @Nullable String description,
        @Nullable String screenshotUrl,
        @Nullable String archiveUrl,
        @Nullable String archiveName,
        List<String> archiveFiles) {

    public VektorProgram {
        Objects.requireNonNull(id, "id");
        archiveFiles = List.copyOf(archiveFiles);
    }
}
