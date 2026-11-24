package com.archive.backend.assistant.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.assistant.read.Skill;
import org.junit.jupiter.api.Test;

/** The skills kept with the application: every one of them is readable, so a broken one fails here. */
class ClasspathSkillReadRepositoryTest {

    @Test
    void readsEverySkillOfTheArchive() {
        ClasspathSkillReadRepository skills = new ClasspathSkillReadRepository();

        assertThat(skills.all())
                .extracting(Skill::name)
                .containsExactlyInAnyOrder(
                        "find-author", "find-software", "add-authors", "import-software", "sort-out-duplicates");
        assertThat(skills.all()).allSatisfy(skill -> {
            assertThat(skill.description()).as(skill.name()).isNotBlank();
            assertThat(skill.instructions()).as(skill.name()).isNotBlank().doesNotStartWith("---");
        });
    }

    @Test
    void findsASkillByItsName() {
        ClasspathSkillReadRepository skills = new ClasspathSkillReadRepository();

        assertThat(skills.find("find-author"))
                .hasValueSatisfying(skill -> assertThat(skill.instructions()).contains("find_names"));
        assertThat(skills.find("fly")).isEmpty();
    }

    @Test
    void refusesASkillWithoutItsFrontmatter() {
        assertThatThrownBy(() ->
                        new ClasspathSkillReadRepository("classpath:assistant/skills-without-frontmatter/*.md").all())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("plain.md");
    }

    @Test
    void refusesASkillNamedNotAsItsFile() {
        assertThatThrownBy(() -> new ClasspathSkillReadRepository("classpath:assistant/skills-misnamed/*.md").all())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("misnamed.md");
    }
}
