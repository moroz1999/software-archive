package com.archive.backend.importer.infra;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Fetches from vektor-06c.ru politely: one request at a time with a pause before each, a few attempts for a failure
 * the site may get over, a single one for a page it does not have.
 */
public class JdkVektorHttp implements VektorHttp {

    private static final Duration TIMEOUT = Duration.ofSeconds(60);

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(TIMEOUT)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private final Duration pause;
    private final int attempts;

    public JdkVektorHttp(Duration pause, int attempts) {
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
                        HttpRequest.newBuilder(URI.create(url))
                                .timeout(TIMEOUT)
                                .GET()
                                .build(),
                        HttpResponse.BodyHandlers.ofByteArray());
                int status = response.statusCode();
                if (status == 200) {
                    return response.body();
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
