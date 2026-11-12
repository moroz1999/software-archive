package com.archive.backend.search.infra;

import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/** Reads SQL arrays of the documents' queries. */
final class SqlArrays {

    private SqlArrays() {}

    static <T> List<T> list(Array array, Class<T> type) throws SQLException {
        return Arrays.stream((Object[]) array.getArray()).map(type::cast).toList();
    }
}
