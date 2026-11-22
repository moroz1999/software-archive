package com.archive.backend.importer.infra;

import com.archive.backend.importer.domain.vektor.VektorListing;
import com.archive.backend.importer.domain.vektor.VektorProgram;
import com.archive.backend.importer.domain.vektor.VektorSection;
import com.archive.backend.importer.domain.vektor.VektorSite;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * vektor-06c.ru read page by page over HTTP. The whole catalog is a list of numbered pages; a section is read from
 * its own list. The other pages of a list are reached by the addresses its pagination gives, as the catalog and a
 * section number their pages by parameters of their own.
 */
@Component
@RequiredArgsConstructor
public class HttpVektorSite implements VektorSite {

    private static final String CATALOG = VektorPages.SITE + "/programmnoe-obespechenie/";

    private final SourceHttp http;

    @Override
    public Iterable<VektorListing> listings(@Nullable VektorSection section) {
        String base = section == null ? CATALOG : CATALOG + section.code() + "/";
        String first = section == null ? base + "?PAGEN_2=1" : base;
        String firstPage = text(first);
        List<VektorListing> listings = new ArrayList<>(VektorPages.listings(firstPage));
        for (String page : VektorPages.otherPages(firstPage)) {
            listings.addAll(VektorPages.listings(text(page)));
        }
        return listings;
    }

    @Override
    public VektorProgram program(VektorListing listing) {
        return VektorPages.program(text(listing.url()), listing);
    }

    @Override
    public byte[] download(String url) {
        return http.get(url);
    }

    private String text(String url) {
        return new String(http.get(url), StandardCharsets.UTF_8);
    }
}
