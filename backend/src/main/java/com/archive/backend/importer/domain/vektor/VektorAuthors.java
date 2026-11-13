package com.archive.backend.importer.domain.vektor;

import com.archive.backend.label.domain.LabelName;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The authors of a program on vektor-06c.ru: one line naming them apart by commas. Every name becomes a label of
 * nobody as it is written; who stands behind it is sorted out later.
 */
public final class VektorAuthors {

    private static final Pattern SPACES = Pattern.compile("[\\s\\u00A0]+");
    /**
     * What the site writes where it does not know the author.
     */
    private static final Set<String> UNKNOWN = Set.of("не известен", "неизвестный автор", "-");

    private VektorAuthors() {}

    public static List<LabelName> labels(String line) {
        Set<String> names = new LinkedHashSet<>();
        for (String part : line.split(",", -1)) {
            String name = SPACES.matcher(part).replaceAll(" ").trim();
            if (!name.isEmpty() && !UNKNOWN.contains(name.toLowerCase(Locale.ROOT))) {
                names.add(name);
            }
        }
        return names.stream().map(LabelName::new).toList();
    }
}
