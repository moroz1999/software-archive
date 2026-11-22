package com.archive.backend.importer.infra;

import com.archive.backend.importer.domain.basis.BasisAuthor;
import com.archive.backend.importer.domain.basis.BasisListing;
import com.archive.backend.importer.domain.basis.BasisProgram;
import com.archive.backend.importer.domain.basis.BasisSite;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * "Базис" read page by page over HTTP. The whole index is one list; the page of an author is read once and kept for
 * the programs that credit them, until the next run.
 */
@Component
@RequiredArgsConstructor
public class HttpBasisSite implements BasisSite {

    private final SourceHttp http;
    private final Map<String, BasisAuthor> authors = new ConcurrentHashMap<>();

    @Override
    public List<BasisListing> listings() {
        authors.clear();
        return BasisPages.listings(text(BasisSite.LIST));
    }

    @Override
    public BasisProgram program(BasisListing listing) {
        return BasisPages.program(text(listing.url()), listing);
    }

    @Override
    public BasisAuthor author(String id) {
        return authors.computeIfAbsent(
                id, key -> BasisPages.author(text(BasisPages.SCALAR + "author/" + key + "/"), key));
    }

    @Override
    public byte[] download(String url) {
        return http.get(url);
    }

    private String text(String url) {
        return new String(http.get(url), StandardCharsets.UTF_8);
    }
}
