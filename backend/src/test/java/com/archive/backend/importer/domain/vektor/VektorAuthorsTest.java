package com.archive.backend.importer.domain.vektor;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.label.domain.LabelName;
import java.util.List;
import org.junit.jupiter.api.Test;

class VektorAuthorsTest {

    @Test
    void takesANameWithItsNicknameAsOneLabel() {
        assertThat(VektorAuthors.labels("Городецкий И. /ivagor")).containsExactly(label("Городецкий И. /ivagor"));
    }

    @Test
    void splitsTheNamesByCommas() {
        assertThat(VektorAuthors.labels("Центр «Тень»,  Третьяков А.Л."))
                .containsExactly(label("Центр «Тень»"), label("Третьяков А.Л."));
        assertThat(VektorAuthors.labels("Brick Software, Элита, Белкин И."))
                .containsExactly(label("Brick Software"), label("Элита"), label("Белкин И."));
    }

    @Test
    void trimsTheNamesAndJoinsTheirWordsByOneSpace() {
        assertThat(VektorAuthors.labels("  Нагаев А.А.   /  Sunami ,Голубев Е. "))
                .containsExactly(label("Нагаев А.А. / Sunami"), label("Голубев Е."));
    }

    @Test
    void leavesOutTheMarksOfAnUnknownAuthor() {
        assertThat(VektorAuthors.labels("не известен")).isEmpty();
        assertThat(VektorAuthors.labels("-")).isEmpty();
        assertThat(VektorAuthors.labels("")).isEmpty();
        assertThat(VektorAuthors.labels(" Неизвестный Автор,  Филиппов Е.В. ")).containsExactly(label("Филиппов Е.В."));
    }

    @Test
    void keepsANameThatOnlyLooksUnknown() {
        assertThat(VektorAuthors.labels("Неизвестный Патриот (Калуга)"))
                .containsExactly(label("Неизвестный Патриот (Калуга)"));
    }

    @Test
    void namesEveryLabelOnce() {
        assertThat(VektorAuthors.labels("Элита,  Сергеев А., Элита"))
                .isEqualTo(List.of(label("Элита"), label("Сергеев А.")));
    }

    @Test
    void leavesOutEmptyPartsBetweenCommas() {
        assertThat(VektorAuthors.labels("Элита,, ,Сергеев А.,")).containsExactly(label("Элита"), label("Сергеев А."));
    }

    private static LabelName label(String name) {
        return new LabelName(name);
    }
}
