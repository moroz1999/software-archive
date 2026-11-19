package com.archive.backend.importer.infra;

import com.archive.backend.importer.domain.basis.BasisAuthor;
import com.archive.backend.importer.domain.basis.BasisListing;
import com.archive.backend.importer.domain.basis.BasisProgram;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/** Reads the pages of the card index "Базис": its whole list of cards, the card of a program and an author page. */
public final class BasisPages {

    public static final String SITE = "https://caglrc.cc";
    public static final String SCALAR = SITE + "/scalar/";

    private static final Pattern WARE = Pattern.compile("/scalar/ware/(\\d+)/");
    private static final Pattern AUTHOR = Pattern.compile("/scalar/author/(\\d+)/");

    private BasisPages() {}

    public static List<BasisListing> listings(String html) {
        List<BasisListing> listings = new ArrayList<>();
        for (Element link : Jsoup.parse(html).select("table.waretable td.col1 a.wareref[href]")) {
            Matcher id = WARE.matcher(link.attr("href"));
            if (id.matches()) {
                listings.add(new BasisListing(id.group(1), SITE + link.attr("href")));
            }
        }
        return listings;
    }

    public static BasisProgram program(String html, BasisListing listing) {
        Document page = Jsoup.parse(html);
        Element description = page.selectFirst("#description");
        String text = description == null ? "" : HtmlText.lines(description);
        Element file = page.selectFirst("a#zip-file[href]");
        return new BasisProgram(
                listing.id(),
                listing.url(),
                textOf(page, "#tittit"),
                textOf(page, "#tityear"),
                page.select("#categories a.catref").stream().map(HtmlText::line).toList(),
                text.isEmpty() ? null : text,
                page.select("img.screenshot[src]").stream()
                        .map(image -> absolute(image.attr("src")))
                        .toList(),
                page.select(".authors a.authorref[href]").stream()
                        .map(link -> AUTHOR.matcher(link.attr("href")))
                        .filter(Matcher::matches)
                        .map(id -> id.group(1))
                        .toList(),
                file == null ? null : absolute(file.attr("href")));
    }

    public static BasisAuthor author(String html, String id) {
        Document page = Jsoup.parse(html);
        String nickname = textOf(page, "#nickname");
        String place = textOf(page, "#location a.kartti");
        return new BasisAuthor(
                id, textOf(page, "#tittit"), nickname.isEmpty() ? null : nickname, place.isEmpty() ? null : place);
    }

    private static String textOf(Document page, String selector) {
        Element element = page.selectFirst(selector);
        return element == null ? "" : HtmlText.line(element);
    }

    private static String absolute(String address) {
        return address.startsWith("/") ? SITE + address : address;
    }
}
