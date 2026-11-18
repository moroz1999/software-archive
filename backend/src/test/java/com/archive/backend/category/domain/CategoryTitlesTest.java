package com.archive.backend.category.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.Language;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CategoryTitlesTest {

    @Test
    void needsARussianTitle() {
        assertThatThrownBy(() -> new CategoryTitles(Map.of(Language.EN, new CategoryTitle("Games"))))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("title.required"));
        assertThatThrownBy(() -> new CategoryTitles(Map.of()))
                .isInstanceOfSatisfying(
                        InvalidValueException.class, e -> assertThat(e.code()).isEqualTo("title.required"));
    }

    @Test
    void mayHaveNoTitleInAnotherLanguage() {
        CategoryTitles titles = new CategoryTitles(Map.of(Language.RU, new CategoryTitle("Игры")));

        assertThat(titles.in(Language.RU)).hasValue(new CategoryTitle("Игры"));
        assertThat(titles.in(Language.EN)).isEmpty();
    }

    @Test
    void keepsATitleInEveryLanguageGiven() {
        CategoryTitles titles = new CategoryTitles(
                Map.of(Language.RU, new CategoryTitle("Игры"), Language.EN, new CategoryTitle("Games")));

        assertThat(titles.in(Language.EN)).hasValue(new CategoryTitle("Games"));
    }
}
