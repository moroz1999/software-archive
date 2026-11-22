package com.archive.backend.importer.infra;

import com.archive.backend.importer.domain.run.ImportLogEntry;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A line of the log as it is stored: its kind, and what it tells as the JSON of that kind. Codes are kept as the names
 * of their constants; the read side turns them into the codes the frontend reads.
 */
final class ImportLogRows {

    private ImportLogRows() {}

    static String kind(ImportLogEntry entry) {
        return switch (entry) {
            case ImportLogEntry.ListRead l -> "LIST";
            case ImportLogEntry.Downloaded d -> "DOWNLOAD";
            case ImportLogEntry.Added a -> "ADDED";
            case ImportLogEntry.Known k -> "KNOWN";
            case ImportLogEntry.Skipped s -> "SKIPPED";
            case ImportLogEntry.Failed f -> "FAILED";
            case ImportLogEntry.Author a -> "AUTHOR";
            case ImportLogEntry.Place p -> "PLACE";
        };
    }

    static Map<String, Object> entry(ImportLogEntry entry) {
        Map<String, Object> json = new LinkedHashMap<>();
        switch (entry) {
            case ImportLogEntry.ListRead l -> {
                json.put("url", l.page().url());
                json.put("number", l.number());
                if (l.pages() != null) {
                    json.put("pages", l.pages());
                }
                json.put("programs", l.programs());
            }
            case ImportLogEntry.Downloaded d -> {
                json.put("externalId", d.externalId());
                json.put("title", d.title());
                json.put("file", d.file().name());
                json.put("name", d.name());
                json.put("size", d.size());
            }
            case ImportLogEntry.Added a -> {
                json.put("externalId", a.program().externalId());
                json.put("softwareId", a.program().software().value());
                json.put("title", a.program().title());
            }
            case ImportLogEntry.Known k -> {
                json.put("externalId", k.program().externalId());
                json.put("softwareId", k.program().software().value());
                json.put("title", k.program().title());
                json.put(
                        "supplements",
                        k.program().supplements().stream()
                                .map(Enum::name)
                                .sorted()
                                .toList());
            }
            case ImportLogEntry.Skipped s -> {
                json.put("externalId", s.program().externalId());
                json.put("url", s.program().page().url());
                json.put("title", s.program().title());
                json.put("reason", s.program().reason().name());
                json.put("detail", s.program().detail());
            }
            case ImportLogEntry.Failed f -> {
                json.put("externalId", f.program().externalId());
                json.put("url", f.program().page().url());
                json.put("reason", f.program().reason());
            }
            case ImportLogEntry.Author a -> {
                json.put("kind", a.author().kind().name());
                json.put("entityId", a.author().entity().value());
                json.put("name", a.author().name());
                json.put("created", a.author().created());
            }
            case ImportLogEntry.Place p -> {
                json.put("kind", p.place().kind().name());
                json.put("name", p.place().name());
            }
        }
        return json;
    }
}
