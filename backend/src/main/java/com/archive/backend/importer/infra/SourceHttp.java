package com.archive.backend.importer.infra;

/**
 * Fetches what a source archive, such as vektor-06c.ru or caglrc.cc, serves at an absolute address.
 */
public interface SourceHttp {

    byte[] get(String url);
}
