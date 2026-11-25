package com.archive.backend.remote;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

/** Another site for the tests: a server on the loopback address answering each path as it is told. */
public final class TestSite implements AutoCloseable {

    private final HttpServer server;
    private final List<Received> received = new CopyOnWriteArrayList<>();

    /** A request the site was sent: its path, its headers and its body. */
    public record Received(String path, Map<String, List<String>> headers, String body) {

        public String header(String name) {
            return headers.entrySet().stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase(name))
                    .map(entry -> entry.getValue().get(0))
                    .findFirst()
                    .orElse("");
        }
    }

    private TestSite(HttpServer server) {
        this.server = server;
    }

    public static TestSite start() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
            server.start();
            return new TestSite(server);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Answers the path with the status, the headers and the body. */
    public TestSite serve(String path, int status, Map<String, String> headers, byte[] body) {
        server.createContext(path, exchange -> {
            received.add(new Received(
                    exchange.getRequestURI().getPath(),
                    Map.copyOf(exchange.getRequestHeaders()),
                    new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8)));
            headers.forEach(exchange.getResponseHeaders()::add);
            exchange.sendResponseHeaders(status, body.length == 0 ? -1 : body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        return this;
    }

    /** Redirects the path to the address. */
    public TestSite redirect(String path, String location) {
        return serve(path, 302, Map.of("Location", location), new byte[0]);
    }

    /** Every request it was sent, in their order. */
    public List<Received> received() {
        return List.copyOf(received);
    }

    public String url(String path) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + path;
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
