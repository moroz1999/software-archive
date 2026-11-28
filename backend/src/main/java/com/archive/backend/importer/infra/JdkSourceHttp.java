package com.archive.backend.importer.infra;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Fetches from a source archive politely: one request at a time with a pause before each, a few attempts for a failure
 * the site may get over, a single one for a page it does not have.
 */
public class JdkSourceHttp implements SourceHttp {

    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    /** The punctuation an address may hold as it is, besides ASCII letters and digits. */
    private static final String ALLOWED = "-._~:/?#[]@!$&'()*+,;=%";

    private static final char[] HEX = "0123456789ABCDEF".toCharArray();

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private final Duration pause;
    private final int attempts;

    public JdkSourceHttp(Duration pause, int attempts) {
        this.pause = pause;
        this.attempts = attempts;
    }

    @Override
    public synchronized byte[] get(String url) {
        String failure = "no attempt";
        for (int attempt = 1; attempt <= attempts; attempt++) {
            rest(pause.multipliedBy(attempt));
            try {
                HttpResponse<byte[]> response = client.send(
                        HttpRequest.newBuilder(URI.create(encoded(url)))
                                .timeout(TIMEOUT)
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofByteArray());
                int status = response.statusCode();
                if (status == 200) {
                    return response.body();
                }
                if (status == 404) {
                    throw new SourceNotFoundException(url);
                }
                failure = "status " + status;
                if (status < 500 && status != 429) {
                    break;
                }
            } catch (IOException e) {
                failure = e.toString();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while fetching " + url, e);
            }
        }
        throw new IllegalStateException("Could not fetch " + url + ": " + failure);
    }

    /**
     * The site writes file addresses as they are named, with spaces and Cyrillic: every character an address may not
     * hold is percent-encoded, while the escapes already there are kept.
     */
    private static String encoded(String url) {
        StringBuilder address = new StringBuilder();
        for (byte b : url.getBytes(StandardCharsets.UTF_8)) {
            int c = b & 0xFF;
            if (c < 0x7F && (Character.isLetterOrDigit(c) || ALLOWED.indexOf(c) >= 0)) {
                address.append((char) c);
            } else {
                address.append('%').append(HEX[c >> 4]).append(HEX[c & 0xF]);
            }
        }
        return address.toString();
    }

    private static void rest(Duration duration) {
        if (duration.isZero()) {
            return;
        }
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while pausing", e);
        }
    }
}
