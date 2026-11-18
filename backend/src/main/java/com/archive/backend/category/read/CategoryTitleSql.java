package com.archive.backend.category.read;

import com.archive.backend.shared.read.NameInLanguageSql;

/** SQL of the title of a category in the language of the {@code :language} parameter, else in Russian. */
public final class CategoryTitleSql {

    private CategoryTitleSql() {}

    /** The title of the category with the id {@code id}, an SQL expression such as {@code c.id}. */
    public static String of(String id) {
        return NameInLanguageSql.of("category_title", "category_id", "title", id);
    }
}
