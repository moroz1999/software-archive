package com.archive.backend.category.domain;

import com.archive.backend.shared.domain.Language;
import java.util.Map;

/** Titles of categories for tests. */
public final class CategoryTitleFixtures {

    private CategoryTitleFixtures() {}

    /** A category titled in Russian only, the one language every category has a title in. */
    public static CategoryTitles titles(String russian) {
        return new CategoryTitles(Map.of(Language.RU, new CategoryTitle(russian)));
    }

    public static CategoryTitles titles(String russian, String english) {
        return new CategoryTitles(
                Map.of(Language.RU, new CategoryTitle(russian), Language.EN, new CategoryTitle(english)));
    }
}
