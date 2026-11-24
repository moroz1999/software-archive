package com.archive.backend.assistant.web.search;

import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.ToolPage;
import com.archive.backend.assistant.web.tool.ToolPages;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.search.app.SearchReleasesHandler;
import com.archive.backend.search.app.SearchReleasesQuery;
import com.archive.backend.search.domain.CatalogKeyword;
import com.archive.backend.search.domain.ReleaseYear;
import com.archive.backend.software.domain.ReleaseFileHash;
import com.archive.backend.software.domain.ReleaseType;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareReferenceId;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/** Releases found apart from their software (docs/domain/ai-assistant.md#release-search). */
@Component
@RequiredArgsConstructor
public class SearchReleasesTool implements ReadTool<SearchReleasesArguments> {

    private final SearchReleasesHandler handler;

    @Override
    public String name() {
        return "search_releases";
    }

    @Override
    public String description() {
        return "Releases found apart from their software, ordered by the title of their software, then by date."
                + " Every condition given narrows the result; within a list any value is enough. Each with its"
                + " software, platform, type, date, hardware (its own and its software's in the groups it does not"
                + " state), archive name and size, and credits. creditedIds finds a person or a group credited by"
                + " themselves or under any of their labels, a label only where it is credited itself. fileName and"
                + " md5 find a file the archive already keeps: the archive or any file inside it.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "word": {"type": ["string", "null"], "description": "A word of the title, the version or the title of the software."},
                    "softwareIds": {"type": ["array", "null"], "items": {"type": "string", "format": "uuid"}},
                    "types": {
                      "type": ["array", "null"],
                      "items": {"enum": [%s]}
                    },
                    "yearFrom": {"type": ["integer", "null"]},
                    "yearTo": {"type": ["integer", "null"]},
                    "platform": {"type": ["string", "null"], "description": "The platform code of the software."},
                    "hardware": {"type": ["array", "null"], "items": {"type": "string"}, "description": "Codes, any of them."},
                    "creditedIds": {
                      "type": ["array", "null"],
                      "items": {"type": "string", "format": "uuid"},
                      "description": "Ids of people, groups or labels, any of them."
                    },
                    "roles": {
                      "type": ["array", "null"],
                      "items": {"type": "string"},
                      "description": "Credit roles (list_options), any of them; of the credited when given, else of anybody."
                    },
                    "fileName": {"type": ["string", "null"], "description": "A part of the name of the archive or of a file inside."},
                    "md5": {"type": ["string", "null"], "pattern": "^[0-9a-fA-F]{32}$", "description": "The MD5 digest of a file inside."},
                    %s
                  },
                  "additionalProperties": false
                }
                """.formatted(
                        java.util.Arrays.stream(ReleaseType.values())
                                .map(type -> "\"" + type.code() + "\"")
                                .collect(Collectors.joining(", ")),
                        ToolPages.SCHEMA);
    }

    @Override
    public Class<SearchReleasesArguments> argumentsType() {
        return SearchReleasesArguments.class;
    }

    @Override
    public Object read(ToolContext context, SearchReleasesArguments a) {
        return ToolPage.of(handler.handle(new SearchReleasesQuery(
                CatalogKeyword.of(a.word()),
                set(a.softwareIds(), SoftwareId::new),
                set(a.types(), ReleaseType::fromCode),
                a.yearFrom() == null ? null : new ReleaseYear(a.yearFrom()),
                a.yearTo() == null ? null : new ReleaseYear(a.yearTo()),
                a.platform() == null ? null : new PlatformCode(a.platform()),
                set(a.hardware(), HardwareCode::new),
                set(a.creditedIds(), SoftwareReferenceId::new),
                set(a.roles(), Function.identity()),
                CatalogKeyword.of(a.fileName()),
                a.md5() == null ? null : new ReleaseFileHash(a.md5().toLowerCase(Locale.ROOT)),
                ToolPages.of(a.page(), a.size()))));
    }

    private static <T, V> Set<V> set(@Nullable List<T> values, Function<T, V> make) {
        return values == null ? Set.of() : values.stream().map(make).collect(Collectors.toSet());
    }
}
