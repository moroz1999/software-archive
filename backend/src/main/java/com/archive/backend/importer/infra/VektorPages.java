package com.archive.backend.importer.infra;

import com.archive.backend.importer.domain.vektor.VektorListing;
import com.archive.backend.importer.domain.vektor.VektorProgram;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jspecify.annotations.Nullable;

/**
 * Reads the pages of vektor-06c.ru: the paged list of programs and the page of one program. Only what the pages show
 * is read; the debug dump some of them carry may go away any day.
 */
public final class VektorPages {

    public static final String SITE = "https://vektor-06c.ru";
    private static final String CATALOG = "/programmnoe-obespechenie/";
    private static final Pattern ELEMENT_ID = Pattern.compile("bx_\\d+_(\\d+)");
    private static final Pattern BACKGROUND = Pattern.compile("url\\('([^']+)'\\)");
    /** The picture the site shows for a program without a screenshot: "Скриншота нет". */
    private static final String NO_SCREENSHOT = "Skrinshota-net";

    private VektorPages() {}

    public static List<VektorListing> listings(String html) {
        List<VektorListing> listings = new ArrayList<>();
        for (Element card : Jsoup.parse(html).select("div.po-container[id^=bx_]")) {
            Matcher id = ELEMENT_ID.matcher(card.id());
            Element link = card.selectFirst("a[href]");
            if (id.matches() && link != null) {
                listings.add(new VektorListing(id.group(1), SITE + link.attr("href")));
            }
        }
        return listings;
    }

    /**
     * The number of the last page of a list; a list without pages has one.
     */
    public static int lastPage(String html) {
        int last = 1;
        for (Element page : Jsoup.parse(html).select(".bx-pagination li span")) {
            String number = page.text().trim();
            if (number.matches("\\d+")) {
                last = Math.max(last, Integer.parseInt(number));
            }
        }
        return last;
    }

    public static VektorProgram program(String html, VektorListing listing) {
        Document page = Jsoup.parse(html);
        Element download = page.selectFirst(".po-download-container");
        Element archiveLink = download == null ? null : download.selectFirst("a.archive-dowload-button[href]");
        Element archiveName = download == null ? null : download.selectFirst(".archive-name");
        return new VektorProgram(
                listing.id(),
                listing.url(),
                section(listing.url()),
                property(page, "НАИМЕНОВАНИЕ"),
                property(page, "АВТОР"),
                property(page, "ГОД ВЫХОДА"),
                description(page),
                screenshot(page),
                archiveLink == null ? null : SITE + archiveLink.attr("href"),
                archiveName == null ? null : archiveName.text().trim(),
                page.select("tr.archive-row > td:first-child").stream()
                        .map(cell -> cell.text().trim())
                        .toList());
    }

    private static String section(String url) {
        int start = url.indexOf(CATALOG);
        String path = start < 0 ? "" : url.substring(start + CATALOG.length());
        int end = path.indexOf('/');
        return end < 0 ? path : path.substring(0, end);
    }

    /**
     * A line of the card as it is written, only trimmed: the authors are told apart by its commas later.
     */
    private static String property(Document page, String name) {
        for (Element property : page.select(".po-property")) {
            Element title = property.selectFirst(".po-property-name");
            Element value = property.selectFirst(".po-property-value");
            if (title != null && value != null && title.text().trim().equals(name)) {
                return value.wholeText().replace(' ', ' ').trim();
            }
        }
        return "";
    }

    private static @Nullable String screenshot(Document page) {
        Element picture = page.selectFirst(".po-img-container[style]");
        if (picture == null) {
            return null;
        }
        Matcher url = BACKGROUND.matcher(picture.attr("style"));
        if (!url.find()) {
            return null;
        }
        String path = url.group(1);
        return path.substring(path.lastIndexOf('/') + 1).startsWith(NO_SCREENSHOT) ? null : SITE + path;
    }

    private static @Nullable String description(Document page) {
        for (Element block : page.select(".additional-info-container")) {
            Element title = block.selectFirst(".additional-info-title");
            Element text = block.selectFirst(".additional-info-text");
            if (title != null && text != null && title.text().trim().equals("Описание")) {
                String description = HtmlText.lines(text);
                return description.isEmpty() ? null : description;
            }
        }
        return null;
    }
}
