package com.archive.backend.importer.infra;

import com.archive.backend.importer.domain.bkcatalog.BkCatalogEntry;
import com.archive.backend.importer.domain.bkcatalog.BkCatalogSection;
import com.archive.backend.importer.domain.bkcatalog.BkCatalogSite;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * bk-catalog read through the polite client: its lists and the files of its repository on GitHub
 * (docs/features/bk-catalog-import.md).
 */
@Component
@RequiredArgsConstructor
public class HttpBkCatalogSite implements BkCatalogSite {

    /** The marks a path keeps as they are: those an address reads as part of a path. */
    private static final String KEPT = "/-._~!$&'()*+,;=:@";

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    private final SourceHttp http;

    @Override
    public List<BkCatalogEntry> entries(BkCatalogSection section) {
        String list = new String(http.get(BkCatalogSite.listUrl(section)), StandardCharsets.UTF_8);
        return CsvRows.parse(list).stream()
                .map(columns -> new BkCatalogEntry(section, columns))
                .toList();
    }

    /** A file the repository lacks is none; any other failure to read it is a failure. */
    @Override
    public Optional<byte[]> download(String path) {
        try {
            return Optional.of(http.get(FILES + encoded(path)));
        } catch (SourceNotFoundException e) {
            return Optional.empty();
        }
    }

    /**
     * The path with every character an address would not read as part of it percent-encoded: names in the repository
     * hold spaces, Cyrillic and marks such as {@code %21} that are part of the name, not escapes.
     */
    private static String encoded(String path) {
        StringBuilder address = new StringBuilder();
        for (byte b : path.getBytes(StandardCharsets.UTF_8)) {
            int c = b & 0xFF;
            if (c < 0x7F && (Character.isLetterOrDigit(c) || KEPT.indexOf(c) >= 0)) {
                address.append((char) c);
            } else {
                address.append('%').append(HEX[c >> 4]).append(HEX[c & 0xF]);
            }
        }
        return address.toString();
    }
}
