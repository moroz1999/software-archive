package com.archive.backend.assistant.infra;

import com.archive.backend.assistant.read.Skill;
import com.archive.backend.assistant.read.SkillReadRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Repository;

/**
 * The skills kept with the application, one Markdown file each in {@code assistant/skills/}: {@code <name>.md}, opened
 * by a frontmatter of {@code name} (the file's name) and {@code description}; the rest is the instructions.
 */
@Repository
public class ClasspathSkillReadRepository implements SkillReadRepository {

    static final String LOCATION = "classpath:assistant/skills/*.md";

    private static final Pattern FRONTMATTER = Pattern.compile(
            "\\A---\\R(?:name: *(?<name>.+?)\\R)(?:description: *(?<description>.+?)\\R)---\\R(?<instructions>.*)\\z",
            Pattern.DOTALL);

    private final String location;

    public ClasspathSkillReadRepository() {
        this(LOCATION);
    }

    ClasspathSkillReadRepository(String location) {
        this.location = location;
    }

    @Override
    public List<Skill> all() {
        try {
            return Arrays.stream(new PathMatchingResourcePatternResolver().getResources(location))
                    .map(ClasspathSkillReadRepository::skill)
                    .sorted(Comparator.comparing(Skill::name))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Optional<Skill> find(String name) {
        return all().stream().filter(skill -> skill.name().equals(name)).findFirst();
    }

    private static Skill skill(Resource resource) {
        String file = String.valueOf(resource.getFilename());
        String text;
        try {
            text = resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        Matcher matcher = FRONTMATTER.matcher(text);
        if (!matcher.matches()) {
            throw new IllegalStateException("The skill " + file + " does not open with its name and description.");
        }
        String name = matcher.group("name").strip();
        if (!file.equals(name + ".md")) {
            throw new IllegalStateException("The skill " + file + " is named " + name + ", not as its file.");
        }
        return new Skill(
                name,
                matcher.group("description").strip(),
                matcher.group("instructions").strip());
    }
}
