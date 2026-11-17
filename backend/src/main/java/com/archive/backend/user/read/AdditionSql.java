package com.archive.backend.user.read;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;

/**
 * Who added an entry and when, read by the pages of every entry: the columns to select of the entry's table under an
 * alias, and the {@link Addition} of a row that selected them.
 */
public final class AdditionSql {

    /** The user name of whoever added the entry {@code alias}, and the moment it was added. */
    public static String columns(String alias) {
        return "(SELECT u.name FROM app_user u WHERE u.id = %1$s.added_by) AS added_by_name, %1$s.created_at AS added_at"
                .formatted(alias);
    }

    /** The addition of a row that selected the {@link #columns(String)}. */
    public static Addition of(ResultSet rs) throws SQLException {
        return new Addition(
                rs.getString("added_by_name"),
                rs.getObject("added_at", OffsetDateTime.class).toInstant());
    }

    private AdditionSql() {}
}
