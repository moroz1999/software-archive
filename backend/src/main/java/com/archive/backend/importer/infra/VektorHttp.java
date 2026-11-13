package com.archive.backend.importer.infra;

/**
 * Fetches what vektor-06c.ru serves at an absolute address.
 */
public interface VektorHttp {

    byte[] get(String url);
}
