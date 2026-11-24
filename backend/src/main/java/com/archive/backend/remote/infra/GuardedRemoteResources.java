package com.archive.backend.remote.infra;

import com.archive.backend.remote.domain.RemoteRequestRefusedException;
import com.archive.backend.remote.read.DownloadedFile;
import com.archive.backend.remote.read.FetchedPage;
import com.archive.backend.remote.read.RemoteResources;
import com.archive.backend.remote.read.ViewedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Requests to other sites through OkHttp, every address passing the {@link AddressPolicy}: the first and each
 * redirect, at most five, by every address its name resolves to, the connection made to the addresses checked, so a
 * second answer of the name server cannot lead it elsewhere. No proxy, no cookies, a neutral User-Agent.
 */
@Component
public class GuardedRemoteResources implements RemoteResources {

    /** Where downloads lie until they are stored. */
    public static final Path TEMPORARY_FOLDER = Path.of(System.getProperty("java.io.tmpdir"), "archive-downloads");

    private static final int MAX_REDIRECTS = 5;
    private static final int CHARSET_SNIFF_BYTES = 2048;
    private static final String USER_AGENT = "Mozilla/5.0 (compatible; ArchiveFetcher/1.0)";
    private static final List<String> TEXT_TYPES = List.of(
            "application/json",
            "application/xml",
            "application/xhtml+xml",
            "application/rss+xml",
            "application/atom+xml",
            "application/javascript");
    private static final Pattern CHARSET =
            Pattern.compile("charset\\s*=\\s*[\"']?([\\w.:-]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern META_CHARSET =
            Pattern.compile("<meta[^>]+charset\\s*=\\s*[\"']?([\\w.:-]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern EXTENDED_NAME =
            Pattern.compile("filename\\*\\s*=\\s*([\\w-]+)'[^']*'([^;]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PLAIN_NAME =
            Pattern.compile("filename\\s*=\\s*(\"([^\"]*)\"|[^;]+)", Pattern.CASE_INSENSITIVE);

    private final AddressPolicy policy;
    private final OkHttpClient client = new OkHttpClient.Builder()
            .followRedirects(false)
            .followSslRedirects(false)
            .proxy(Proxy.NO_PROXY)
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(30))
            .callTimeout(Duration.ofSeconds(60))
            .build();

    public GuardedRemoteResources(AddressPolicy policy) {
        this.policy = policy;
    }

    @Override
    public FetchedPage fetchPage(String url, int maxBytes) {
        try (Response response = get(url)) {
            byte[] bytes = read(body(response), maxBytes);
            boolean truncated = bytes.length > maxBytes;
            byte[] kept = truncated ? Arrays.copyOf(bytes, maxBytes) : bytes;
            String contentType = response.header("Content-Type", "");
            String mediaType = mediaType(contentType);
            return new FetchedPage(
                    response.request().url().toString(),
                    response.code(),
                    contentType,
                    isText(mediaType) ? decode(kept, charset(contentType, mediaType, kept), truncated) : null,
                    truncated,
                    kept.length);
        }
    }

    @Override
    public ViewedImage viewImage(String url, int maxBytes) {
        try (Response response = get(url)) {
            requireSuccess(response);
            byte[] bytes = read(body(response), maxBytes);
            if (bytes.length > maxBytes) {
                throw new RemoteRequestRefusedException("The picture is larger than " + maxBytes + " bytes: " + url);
            }
            String type = imageType(bytes);
            if (type == null) {
                throw new RemoteRequestRefusedException("Not a PNG, JPEG, GIF or WebP picture: " + url);
            }
            return new ViewedImage(type, Base64.getEncoder().encodeToString(bytes));
        }
    }

    @Override
    public DownloadedFile download(String url, long maxBytes) {
        try (Response response = get(url)) {
            requireSuccess(response);
            Path file = temporaryFile();
            long size = 0;
            try (InputStream in = body(response).byteStream();
                    OutputStream out = Files.newOutputStream(file)) {
                byte[] buffer = new byte[64 * 1024];
                for (int read = in.read(buffer); read >= 0; read = in.read(buffer)) {
                    size += read;
                    if (size > maxBytes) {
                        throw new RemoteRequestRefusedException(
                                "The file is larger than " + maxBytes + " bytes: " + url);
                    }
                    out.write(buffer, 0, read);
                }
            } catch (IOException | RuntimeException e) {
                deleteQuietly(file);
                if (e instanceof RuntimeException runtime) {
                    throw runtime;
                }
                throw new RemoteRequestRefusedException("The download failed: " + e.getMessage() + " (" + url + ")");
            }
            return new DownloadedFile(
                    file,
                    fileName(
                            response.header("Content-Disposition"),
                            response.request().url()),
                    size,
                    response.header("Content-Type", ""));
        }
    }

    /** The last response after the redirects, every address checked before it is asked. */
    private Response get(String url) {
        HttpUrl current = HttpUrl.parse(url);
        if (current == null) {
            throw new RemoteRequestRefusedException("Not an http or https address: " + url);
        }
        for (int redirects = 0; ; redirects++) {
            String host = current.host();
            List<InetAddress> addresses = checked(current);
            OkHttpClient pinned = client.newBuilder()
                    .dns(asked -> {
                        if (!asked.equalsIgnoreCase(host)) {
                            throw new UnknownHostException("Not the host checked: " + asked);
                        }
                        return addresses;
                    })
                    .build();
            Response response = execute(pinned, current);
            String location = response.header("Location");
            if (!response.isRedirect() || location == null) {
                return response;
            }
            response.close();
            if (redirects == MAX_REDIRECTS) {
                throw new RemoteRequestRefusedException("Too many redirects: " + url);
            }
            HttpUrl next = current.resolve(location);
            if (next == null) {
                throw new RemoteRequestRefusedException("A redirect to no http or https address: " + location);
            }
            current = next;
        }
    }

    private List<InetAddress> checked(HttpUrl url) {
        List<InetAddress> addresses;
        try {
            addresses = List.of(InetAddress.getAllByName(url.host()));
        } catch (UnknownHostException e) {
            throw new RemoteRequestRefusedException("Unknown host: " + url.host());
        }
        for (InetAddress address : addresses) {
            if (!policy.allows(address)) {
                throw new RemoteRequestRefusedException("Refused an address inside the archive's network: " + url.host()
                        + " is " + address.getHostAddress() + " (" + url + ")");
            }
        }
        return addresses;
    }

    private static Response execute(OkHttpClient client, HttpUrl url) {
        Request request = new Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .get()
                .build();
        try {
            return client.newCall(request).execute();
        } catch (IOException e) {
            throw new RemoteRequestRefusedException("The request failed: " + e.getMessage() + " (" + url + ")");
        }
    }

    private static ResponseBody body(Response response) {
        ResponseBody body = response.body();
        if (body == null) {
            throw new RemoteRequestRefusedException(
                    "No body: " + response.request().url());
        }
        return body;
    }

    private static void requireSuccess(Response response) {
        if (response.code() != 200) {
            throw new RemoteRequestRefusedException(
                    "HTTP " + response.code() + ": " + response.request().url());
        }
    }

    /** At most one byte past the limit, which tells that there is more. */
    private static byte[] read(ResponseBody body, long maxBytes) {
        try (InputStream in = body.byteStream()) {
            return in.readNBytes((int) Math.min(Integer.MAX_VALUE - 8, maxBytes + 1));
        } catch (IOException e) {
            throw new RemoteRequestRefusedException("Reading failed: " + e.getMessage());
        }
    }

    private static String mediaType(String contentType) {
        return contentType.split(";", 2)[0].strip().toLowerCase(Locale.ROOT);
    }

    private static boolean isText(String mediaType) {
        return mediaType.isEmpty() || mediaType.startsWith("text/") || TEXT_TYPES.contains(mediaType);
    }

    private static Charset charset(String contentType, String mediaType, byte[] body) {
        Matcher header = CHARSET.matcher(contentType);
        if (header.find()) {
            return known(header.group(1));
        }
        if (mediaType.equals("text/html") || mediaType.equals("application/xhtml+xml")) {
            String head = new String(body, 0, Math.min(body.length, CHARSET_SNIFF_BYTES), StandardCharsets.ISO_8859_1);
            Matcher meta = META_CHARSET.matcher(head);
            if (meta.find()) {
                return known(meta.group(1));
            }
        }
        return StandardCharsets.UTF_8;
    }

    private static Charset known(String name) {
        try {
            return Charset.forName(name);
        } catch (IllegalArgumentException e) {
            return StandardCharsets.UTF_8;
        }
    }

    /** The bytes as text; a character the cut falls inside is left out. */
    private static String decode(byte[] bytes, Charset charset, boolean truncated) {
        CharsetDecoder decoder = charset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPLACE)
                .onUnmappableCharacter(CodingErrorAction.REPLACE);
        CharBuffer out = CharBuffer.allocate((int) Math.ceil(bytes.length * (double) decoder.maxCharsPerByte()) + 1);
        decoder.decode(ByteBuffer.wrap(bytes), out, !truncated);
        if (!truncated) {
            decoder.flush(out);
        }
        return out.flip().toString();
    }

    private static @Nullable String imageType(byte[] b) {
        if (starts(b, 0x89, 'P', 'N', 'G')) {
            return "image/png";
        }
        if (starts(b, 0xFF, 0xD8, 0xFF)) {
            return "image/jpeg";
        }
        if (starts(b, 'G', 'I', 'F', '8')) {
            return "image/gif";
        }
        if (starts(b, 'R', 'I', 'F', 'F')
                && b.length >= 12
                && b[8] == 'W'
                && b[9] == 'E'
                && b[10] == 'B'
                && b[11] == 'P') {
            return "image/webp";
        }
        return null;
    }

    private static boolean starts(byte[] bytes, int... signature) {
        if (bytes.length < signature.length) {
            return false;
        }
        for (int i = 0; i < signature.length; i++) {
            if ((bytes[i] & 0xFF) != signature[i]) {
                return false;
            }
        }
        return true;
    }

    /** The name of {@code Content-Disposition}, else the last part of the address; never a path. */
    static String fileName(@Nullable String disposition, HttpUrl url) {
        String name = null;
        if (disposition != null) {
            Matcher extended = EXTENDED_NAME.matcher(disposition);
            Matcher plain = PLAIN_NAME.matcher(disposition);
            if (extended.find()) {
                name = URLDecoder.decode(extended.group(2).strip().replace("+", "%2B"), known(extended.group(1)));
            } else if (plain.find()) {
                name = plain.group(2) != null ? plain.group(2) : plain.group(1).strip();
            }
        }
        if (name == null || name.isBlank()) {
            List<String> segments = url.pathSegments();
            name = segments.isEmpty() ? "" : segments.get(segments.size() - 1);
        }
        name = name.substring(Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\')) + 1)
                .strip();
        return name.isEmpty() ? "download" : name;
    }

    private static Path temporaryFile() {
        try {
            Files.createDirectories(TEMPORARY_FOLDER);
            return Files.createTempFile(TEMPORARY_FOLDER, "download-", ".tmp");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void deleteQuietly(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // the folder is a temporary one: what is left is cleaned with it
        }
    }
}
