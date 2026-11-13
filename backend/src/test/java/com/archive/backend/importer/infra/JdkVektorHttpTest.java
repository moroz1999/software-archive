package com.archive.backend.importer.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Against a local server that answers with the statuses a test lines up.
 */
class JdkVektorHttpTest {

    private final Deque<Integer> statuses = new ArrayDeque<>();
    private final AtomicInteger requests = new AtomicInteger();
    private final JdkVektorHttp http = new JdkVektorHttp(Duration.ZERO, 3);
    private HttpServer server;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            Integer status = statuses.poll();
            int code = status == null ? 200 : status;
            byte[] body = ("answer " + code).getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(code, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    @Test
    void readsWhatTheSiteServes() {
        assertThat(new String(http.get(url()), StandardCharsets.UTF_8)).isEqualTo("answer 200");
        assertThat(requests).hasValue(1);
    }

    @Test
    void triesAgainWhenTheSiteFails() {
        statuses.add(503);
        statuses.add(502);

        assertThat(new String(http.get(url()), StandardCharsets.UTF_8)).isEqualTo("answer 200");
        assertThat(requests).hasValue(3);
    }

    @Test
    void givesUpAfterItsAttempts() {
        statuses.add(503);
        statuses.add(503);
        statuses.add(503);

        assertThatThrownBy(() -> http.get(url())).isInstanceOf(IllegalStateException.class);
        assertThat(requests).hasValue(3);
    }

    @Test
    void neverTriesAgainForAMissingPage() {
        statuses.add(404);

        assertThatThrownBy(() -> http.get(url())).isInstanceOf(IllegalStateException.class);
        assertThat(requests).hasValue(1);
    }

    private String url() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/page";
    }
}
