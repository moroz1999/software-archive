package com.archive.backend.software.infra;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.software.domain.ExecutableFiles;
import com.archive.backend.software.domain.ReleaseArchiveUpload;
import com.archive.backend.software.domain.ReleaseFile;
import com.archive.backend.software.domain.ReleaseFileHash;
import com.archive.backend.software.domain.ReleaseFileId;
import com.archive.backend.software.domain.ReleaseFileName;
import com.archive.backend.software.domain.ReleaseFileStructureParser;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/** The general release file parser, with ZIP as its first traversable container. */
@Component
public class ZipReleaseFileStructureParser implements ReleaseFileStructureParser {

    private static final int BUFFER_SIZE = 8192;

    private static final int SIGNATURE_SIZE = 4;

    private static final int END_OF_CENTRAL_DIRECTORY_SIZE = 22;

    private static final int MAX_ZIP_COMMENT_SIZE = 0xFFFF;

    private final ReleaseFileParserProperties properties;

    public ZipReleaseFileStructureParser(ReleaseFileParserProperties properties) {
        this.properties = properties;
    }

    /**
     * Only a root that is a container is held in memory; any other file is hashed as it streams by. A tree is never
     * partial: a container inside that cannot be read, or the depth or size limit reached, leaves the root unreadable
     * with no known contents.
     */
    @Override
    public ReleaseFile parse(PlatformCode platform, ReleaseArchiveUpload upload) {
        ReleaseFileName name = new ReleaseFileName(upload.fileName());
        boolean executable = ExecutableFiles.isExecutable(platform, name);
        try (InputStream input = new BufferedInputStream(upload.content().open())) {
            input.mark(SIGNATURE_SIZE);
            byte[] signature = input.readNBytes(SIGNATURE_SIZE);
            input.reset();
            if (executable || !isZip(signature)) {
                return new ReleaseFile(newId(), name, md5(input), false, executable, false, List.of());
            }
            byte[] content = input.readAllBytes();
            try {
                return parseFile(platform, name, content, true, 0, new ExpandedBytes(properties.maxExpandedBytes()));
            } catch (UnreadableContainerException e) {
                return file(name, content, false, true, List.of());
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read release file " + upload.fileName(), e);
        }
    }

    private ReleaseFile parseFile(
            PlatformCode platform,
            ReleaseFileName name,
            byte[] content,
            boolean executablesAllowed,
            int containerDepth,
            ExpandedBytes expanded) {
        boolean executable = executablesAllowed && ExecutableFiles.isExecutable(platform, name);
        if (executable || !isZip(content)) {
            return file(name, content, executable, false, List.of());
        }
        if (containerDepth >= properties.maxContainerDepth()) {
            throw new UnreadableContainerException();
        }
        if (!hasEndOfCentralDirectory(content)) {
            throw new UnreadableContainerException();
        }
        List<ReleaseFile> children;
        try {
            children = zipChildren(platform, content, executablesAllowed, containerDepth, expanded);
        } catch (UnreadableContainerException e) {
            throw e;
        } catch (IOException | RuntimeException e) {
            // Entry names outside UTF-8 included.
            throw new UnreadableContainerException();
        }
        return file(name, content, false, false, children);
    }

    private List<ReleaseFile> zipChildren(
            PlatformCode platform,
            byte[] content,
            boolean executablesAllowed,
            int containerDepth,
            ExpandedBytes expanded)
            throws IOException {
        Map<String, ParsedNode> roots = new LinkedHashMap<>();
        try (ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(content))) {
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                List<String> parts = pathParts(entry.getName());
                if (parts.isEmpty()) {
                    zip.closeEntry();
                    continue;
                }
                byte[] entryContent = entry.isDirectory() ? new byte[0] : readEntry(zip, expanded);
                add(roots, parts, entry.isDirectory(), entryContent);
                zip.closeEntry();
            }
        }
        List<ReleaseFile> result = new ArrayList<>();
        for (ParsedNode node : roots.values()) {
            result.add(toFile(platform, node, executablesAllowed, containerDepth + 1, expanded));
        }
        return List.copyOf(result);
    }

    private ReleaseFile toFile(
            PlatformCode platform,
            ParsedNode node,
            boolean executablesAllowed,
            int containerDepth,
            ExpandedBytes expanded) {
        ReleaseFileName name = new ReleaseFileName(node.name);
        if (!node.directory) {
            return parseFile(
                    platform, name, Objects.requireNonNull(node.content), executablesAllowed, containerDepth, expanded);
        }
        List<ReleaseFile> children = new ArrayList<>();
        for (ParsedNode child : node.children.values()) {
            children.add(toFile(platform, child, executablesAllowed, containerDepth, expanded));
        }
        return new ReleaseFile(newId(), name, null, true, false, false, children);
    }

    private static void add(Map<String, ParsedNode> roots, List<String> parts, boolean directory, byte[] content) {
        Map<String, ParsedNode> level = roots;
        for (int index = 0; index < parts.size(); index++) {
            String part = parts.get(index);
            boolean last = index == parts.size() - 1;
            boolean nodeDirectory = !last || directory;
            ParsedNode node = level.get(part);
            if (node == null) {
                node = new ParsedNode(part, nodeDirectory, nodeDirectory ? null : content);
                level.put(part, node);
            }
            if (!node.directory && !last) {
                return;
            }
            level = node.children;
        }
    }

    private static List<String> pathParts(String path) {
        return Arrays.stream(path.replace('\\', '/').split("/"))
                .filter(part -> !part.isEmpty() && !part.equals(".") && !part.equals(".."))
                .toList();
    }

    private static byte[] readEntry(ZipInputStream zip, ExpandedBytes expanded) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        while ((read = zip.read(buffer)) >= 0) {
            if (read > 0) {
                expanded.add(read);
                output.write(buffer, 0, read);
            }
        }
        return output.toByteArray();
    }

    private static boolean isZip(byte[] content) {
        // Local file header, empty archive, spanned archive.
        return content.length >= 4
                && content[0] == 'P'
                && content[1] == 'K'
                && ((content[2] == 3 && content[3] == 4)
                        || (content[2] == 5 && content[3] == 6)
                        || (content[2] == 7 && content[3] == 8));
    }

    /**
     * A streamed ZIP that is cut short or broken after its signature reads as if it had no more entries; a whole one
     * ends with its end of central directory record, within the last 22 bytes and the longest comment.
     */
    private static boolean hasEndOfCentralDirectory(byte[] content) {
        int last = content.length - END_OF_CENTRAL_DIRECTORY_SIZE;
        int first = Math.max(0, last - MAX_ZIP_COMMENT_SIZE);
        for (int index = last; index >= first; index--) {
            if (content[index] == 'P'
                    && content[index + 1] == 'K'
                    && content[index + 2] == 5
                    && content[index + 3] == 6) {
                return true;
            }
        }
        return false;
    }

    private static ReleaseFile file(
            ReleaseFileName name, byte[] content, boolean executable, boolean unreadable, List<ReleaseFile> children) {
        MessageDigest digest = md5();
        digest.update(content);
        return new ReleaseFile(newId(), name, hash(digest), false, executable, unreadable, children);
    }

    private static ReleaseFileHash md5(InputStream input) throws IOException {
        MessageDigest digest = md5();
        byte[] buffer = new byte[BUFFER_SIZE];
        int read;
        while ((read = input.read(buffer)) >= 0) {
            digest.update(buffer, 0, read);
        }
        return hash(digest);
    }

    private static MessageDigest md5() {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("The Java runtime has no MD5 digest", e);
        }
    }

    private static ReleaseFileHash hash(MessageDigest digest) {
        return new ReleaseFileHash(HexFormat.of().formatHex(digest.digest()));
    }

    private static ReleaseFileId newId() {
        return new ReleaseFileId(UUID.randomUUID());
    }

    private static final class ParsedNode {

        private final String name;
        private final boolean directory;
        private final byte @Nullable [] content;
        private final Map<String, ParsedNode> children = new LinkedHashMap<>();

        private ParsedNode(String name, boolean directory, byte @Nullable [] content) {
            this.name = name;
            this.directory = directory;
            this.content = content;
        }
    }

    private static final class ExpandedBytes {

        private final long maximum;
        private long used;

        private ExpandedBytes(long maximum) {
            this.maximum = maximum;
        }

        private void add(int bytes) {
            used += bytes;
            if (used > maximum) {
                throw new UnreadableContainerException();
            }
        }
    }

    /** A container that cannot be read whole: broken, too deep, or expanding beyond the limit. */
    private static final class UnreadableContainerException extends RuntimeException {

        private static final long serialVersionUID = 1L;
    }
}
