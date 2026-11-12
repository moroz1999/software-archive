package com.archive.backend.search.infra;

import org.jspecify.annotations.Nullable;
import org.typesense.model.MultiSearchCollectionParameters;
import org.typesense.model.MultiSearchResultItem;

/** A section of the search: its collection, what the keyword is matched over, the name order and the platform field. */
enum TypesenseSection {
    SOFTWARE("software", "title,alternative_titles", "title_sort", "platform"),
    AUTHORS("authors", "name,labels,real_name", "name_sort", "platforms"),
    GROUPS("groups", "name,labels,group_name", "name_sort", "platforms");

    private final String collection;
    private final String queryBy;
    private final String sortField;
    private final String platformField;

    TypesenseSection(String collection, String queryBy, String sortField, String platformField) {
        this.collection = collection;
        this.queryBy = queryBy;
        this.sortField = sortField;
        this.platformField = platformField;
    }

    /** A page of the matches of the keyword, of the platform when one is given; the best ones first, equal ones by name. */
    MultiSearchCollectionParameters search(String keyword, @Nullable String platform, int page, int perPage) {
        MultiSearchCollectionParameters search = new MultiSearchCollectionParameters().collection(collection);
        search.setQ(keyword);
        search.setQueryBy(queryBy);
        search.setSortBy("_text_match:desc," + sortField + ":asc");
        search.setPage(page + 1);
        search.setPerPage(perPage);
        if (platform != null) {
            search.setFilterBy(platformField + ":=`" + platform + "`");
        }
        return search;
    }

    /** {@code limit} matches from {@code offset} on, in the order of {@link #search}. */
    MultiSearchCollectionParameters window(String keyword, @Nullable String platform, int offset, int limit) {
        MultiSearchCollectionParameters search = search(keyword, platform, 0, limit);
        search.setPage(null);
        search.setPerPage(null);
        search.setOffset(offset);
        search.setLimit(limit);
        return search;
    }

    /** The answer of a search of the section; fails when Typesense refused it. */
    static MultiSearchResultItem answered(MultiSearchResultItem result) {
        if (result.getError() != null) {
            throw new SearchIndexException("Typesense refused a search: " + result.getError());
        }
        return result;
    }
}
