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
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Against a local server that answers with the statuses a test lines up.
 */
class JdkSourceHttpTest {

    private final Deque<Integer> statuses = new ArrayDeque<>();
    private final AtomicInteger requests = new AtomicInteger();
    private final AtomicReference<String> requestedPath = new AtomicReference<>();
    private final JdkSourceHttp http = new JdkSourceHttp(Duration.ZERO, 3);
    private HttpServer server;

    @BeforeEach
    void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            requestedPath.set(exchange.getRequestURI().getRawPath());
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
    void fetchesAnAddressWrittenWithSpacesAndCyrillic() {
        String address = "http://127.0.0.1:" + server.getAddress().getPort() + "/upload/Скриншот 09-11-2026.png";

        assertThat(new String(http.get(address), StandardCharsets.UTF_8)).isEqualTo("answer 200");
        assertThat(requestedPath).hasValue("/upload/%D0%A1%D0%BA%D1%80%D0%B8%D0%BD%D1%88%D0%BE%D1%82%2009-11-2026.png");
    }

    @Test
    void keepsAnAddressAlreadyEncoded() {
        http.get("http://127.0.0.1:" + server.getAddress().getPort() + "/upload/Bruce%20Lee.zip");

        assertThat(requestedPath).hasValue("/upload/Bruce%20Lee.zip");
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
