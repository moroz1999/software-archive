package com.archive.backend.search.infra;

/** The search engine did not confirm a call: unreachable, or it refused it. */
public class SearchIndexException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SearchIndexException(String message) {
        super(message);
    }

    public SearchIndexException(String message, Throwable cause) {
        super(message, cause);
    }
}
