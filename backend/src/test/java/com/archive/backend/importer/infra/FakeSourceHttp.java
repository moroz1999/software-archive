package com.archive.backend.importer.infra;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * A source archive as a test lines it up: answers by address, and remembers what was asked.
 */
public class FakeSourceHttp implements SourceHttp {

    /** The first bytes of a picture of each format, enough for the archive to know it. */
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 0};

    private static final byte[] GIF = "GIF89a......".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final byte[] ZIP = {'P', 'K', 5, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    private final Map<String, byte[]> answers = new HashMap<>();
    private final Set<String> failing = new HashSet<>();
    private final List<String> asked = new ArrayList<>();

    public void serve(String url, byte[] body) {
        answers.put(url, body);
    }

    public void serve(String url, String html) {
        serve(url, html.getBytes(StandardCharsets.UTF_8));
    }

    /** The site fails to serve this address. */
    public void fail(String url) {
        failing.add(url);
    }

    public void clear() {
        answers.clear();
        failing.clear();
        asked.clear();
    }

    public List<String> asked() {
        return List.copyOf(asked);
    }

    @Override
    public byte[] get(String url) {
        asked.add(url);
        if (failing.contains(url)) {
            throw new IllegalStateException("Failed: " + url);
        }
        byte[] body = answers.get(url);
        if (body != null) {
            return body;
        }
        if (url.contains("/upload/") || url.contains("/scalar/media/")) {
            return upload(url);
        }
        throw new IllegalStateException("Not found: " + url);
    }

    /** Any file the site keeps among its uploads: a picture or an archive by the extension of its name. */
    private static byte[] upload(String url) {
        String name = url.toLowerCase(Locale.ROOT);
        if (name.endsWith(".png")) {
            return PNG;
        }
        if (name.endsWith(".gif")) {
            return GIF;
        }
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            return JPEG;
        }
        if (name.endsWith(".zip")) {
            return ZIP;
        }
        if (name.endsWith(".rom") || name.endsWith(".com")) {
            return ("program " + name).getBytes(StandardCharsets.UTF_8);
        }
        throw new IllegalStateException("Not found: " + url);
    }

    /** A page as vektor-06c.ru served it, kept among the test resources. */
    public static String page(String name) {
        return resource("/importer/vektor/" + name);
    }

    /** A page as the card index "Базис" at caglrc.cc served it, kept among the test resources. */
    public static String basisPage(String name) {
        return resource("/importer/basis/" + name);
    }

    private static String resource(String path) {
        try (InputStream in = FakeSourceHttp.class.getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("No test page " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /** A list page of the site with the cards of these programs, by id and address, and no further pages. */
    public static String list(String... idsAndUrls) {
        StringBuilder html = new StringBuilder("<html><body>");
        for (int i = 0; i < idsAndUrls.length; i += 2) {
            html.append("<div class=\"po-container\" id=\"bx_3218110189_")
                    .append(idsAndUrls[i])
                    .append("\"><a href=\"")
                    .append(idsAndUrls[i + 1])
                    .append("\">card</a></div>");
        }
        return html.append("</body></html>").toString();
    }
}
