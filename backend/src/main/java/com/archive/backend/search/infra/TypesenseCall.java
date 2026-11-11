package com.archive.backend.search.infra;

/** A call to Typesense; any failure of it, unreachable or refused, becomes a {@link SearchIndexException}. */
@FunctionalInterface
interface TypesenseCall<T> {

    T run() throws Exception;

    static <T> T call(String what, TypesenseCall<T> call) {
        try {
            return call.run();
        } catch (SearchIndexException e) {
            throw e;
        } catch (Exception e) {
            throw new SearchIndexException("Typesense failed to " + what, e);
        }
    }
}
