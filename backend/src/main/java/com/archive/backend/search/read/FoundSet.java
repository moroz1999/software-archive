package com.archive.backend.search.read;

import java.util.List;

/** The matches of one kind on a page of the full search, with the count of all matches of the kind. */
public sealed interface FoundSet {

    FoundType kind();

    long total();

    record Authors(long total, List<FoundAuthor> items) implements FoundSet {
        public Authors {
            items = List.copyOf(items);
        }

        @Override
        public FoundType kind() {
            return FoundType.AUTHOR;
        }
    }

    record Groups(long total, List<FoundGroup> items) implements FoundSet {
        public Groups {
            items = List.copyOf(items);
        }

        @Override
        public FoundType kind() {
            return FoundType.GROUP;
        }
    }

    record Software(long total, List<SoftwareCatalogItem> items) implements FoundSet {
        public Software {
            items = List.copyOf(items);
        }

        @Override
        public FoundType kind() {
            return FoundType.SOFTWARE;
        }
    }
}
