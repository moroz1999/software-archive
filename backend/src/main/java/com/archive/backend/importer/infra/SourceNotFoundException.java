package com.archive.backend.importer.infra;

/** A source archive has nothing at the address asked: it answered that it is not found. */
public class SourceNotFoundException extends IllegalStateException {

    private static final long serialVersionUID = 1L;

    public SourceNotFoundException(String url) {
        super("Not found: " + url);
    }
}
