package com.archive.backend.shared.read;

/**
 * SQL of a name kept in several languages: the one in the language of the {@code :language} parameter, else the
 * Russian one, which every such name has (docs/domain/localization.md).
 */
public final class NameInLanguageSql {

    private NameInLanguageSql() {}

    /**
     * The name in {@code table}, whose rows belong to their owner by {@code ownerColumn}, of the owner {@code owner} (an
     * SQL expression such as {@code c.id}); NULL for no owner.
     */
    public static String of(String table, String ownerColumn, String nameColumn, String owner) {
        return """
                COALESCE(\
                (SELECT nl.%3$s FROM %1$s nl WHERE nl.%2$s = %4$s AND nl.language = :language), \
                (SELECT nl.%3$s FROM %1$s nl WHERE nl.%2$s = %4$s AND nl.language = 'ru'))""".formatted(table, ownerColumn, nameColumn, owner);
    }
}
