package com.archive.backend.assistant.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.assistant.domain.skill.SkillName;
import com.archive.backend.assistant.domain.skill.SkillNotFoundException;
import com.archive.backend.assistant.read.Skill;
import com.archive.backend.assistant.read.SkillReadRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

/** The skill the model asks for. */
class LoadSkillHandlerTest {

    private static final Skill FIND = new Skill("find-author", "Finding a person.", "- Search.");
    private static final Skill ADD = new Skill("add-authors", "Adding people.", "- Add.");

    private final LoadSkillHandler handler = new LoadSkillHandler(new SkillReadRepository() {
        @Override
        public List<Skill> all() {
            return List.of(FIND, ADD);
        }

        @Override
        public Optional<Skill> find(String name) {
            return all().stream().filter(skill -> skill.name().equals(name)).findFirst();
        }
    });

    @Test
    void givesTheSkillAsked() {
        assertThat(handler.handle(new LoadSkillQuery(new SkillName("add-authors"))))
                .isEqualTo(ADD);
    }

    @Test
    void refusesANameOfNoSkillNamingThoseThereAre() {
        assertThatThrownBy(() -> handler.handle(new LoadSkillQuery(new SkillName("fly"))))
                .isInstanceOf(SkillNotFoundException.class)
                .hasMessageContaining("fly")
                .hasMessageContaining("find-author")
                .hasMessageContaining("add-authors");
    }
}
