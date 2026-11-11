package com.archive.backend.search.infra;

import static com.archive.backend.search.infra.TypesenseCall.call;

import com.archive.backend.search.domain.SearchCollection;
import com.archive.backend.search.domain.SearchEntity;
import com.archive.backend.search.domain.SearchEntityType;
import com.archive.backend.search.domain.SearchIndex;
import com.archive.backend.search.domain.SoftwareDocument;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.typesense.api.Client;
import org.typesense.api.FieldTypes;
import org.typesense.api.exceptions.ObjectNotFound;
import org.typesense.model.CollectionAliasSchema;
import org.typesense.model.CollectionSchema;
import org.typesense.model.Field;
import org.typesense.model.ImportDocumentsParameters;
import org.typesense.model.IndexAction;

/**
 * The search collections in Typesense. Each kind of entities is read through an alias named after the kind, pointing to
 * the collection of its last full build (docs/features/search.md#collections).
 */
@Component
@RequiredArgsConstructor
public class TypesenseSearchIndex implements SearchIndex {

    private final Client client;
    private final ObjectMapper json;

    private static String alias(SearchEntityType type) {
        return switch (type) {
            case SOFTWARE -> "software";
        };
    }

    private static List<Field> fields(SearchEntityType type) {
        return switch (type) {
            case SOFTWARE ->
                List.of(
                        new Field().name("title").type(FieldTypes.STRING),
                        // The catalog's order: by title, whatever the case.
                        new Field().name("title_sort").type(FieldTypes.STRING).sort(true),
                        // Shown on the card only.
                        new Field()
                                .name("release_date")
                                .type(FieldTypes.STRING)
                                .index(false)
                                .optional(true),
                        new Field().name("alternative_titles").type(FieldTypes.STRING_ARRAY),
                        new Field().name("platform").type(FieldTypes.STRING).facet(true),
                        new Field()
                                .name("year")
                                .type(FieldTypes.INT32)
                                .facet(true)
                                .optional(true),
                        new Field()
                                .name("categories")
                                .type(FieldTypes.STRING_ARRAY)
                                .facet(true),
                        new Field()
                                .name("countries")
                                .type(FieldTypes.STRING_ARRAY)
                                .facet(true));
        };
    }

    private static Map<String, Object> toMap(SoftwareDocument d) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("id", d.id().toString());
        doc.put("title", d.title());
        doc.put("title_sort", d.title().toLowerCase(Locale.ROOT));
        if (d.releaseDate() != null) {
            doc.put("release_date", d.releaseDate().toString());
        }
        doc.put("alternative_titles", d.alternativeTitles());
        doc.put("platform", d.platform());
        if (d.year() != null) {
            doc.put("year", d.year());
        }
        doc.put("categories", d.categories().stream().map(UUID::toString).toList());
        doc.put("countries", d.countries().stream().map(UUID::toString).toList());
        return doc;
    }

    @Override
    public void upsert(SoftwareDocument document) {
        call(
                "upsert software " + document.id(),
                () -> client.collections(alias(SearchEntityType.SOFTWARE))
                        .documents()
                        .upsert(toMap(document)));
    }

    @Override
    public void delete(SearchEntity entity) {
        call("delete " + entity, () -> {
            try {
                return client.collections(alias(entity.type()))
                        .documents(entity.id().toString())
                        .delete();
            } catch (ObjectNotFound e) {
                return Map.of();
            }
        });
    }

    @Override
    public boolean isCurrent(SearchEntityType type) {
        Optional<String> collection = target(type);
        if (collection.isEmpty()) {
            return false;
        }
        Set<String> present = call(
                        "read collection " + collection.get(),
                        () -> client.collections(collection.get()).retrieve().getFields())
                .stream()
                .map(Field::getName)
                .collect(Collectors.toSet());
        return fields(type).stream().map(Field::getName).allMatch(present::contains);
    }

    private Optional<String> target(SearchEntityType type) {
        return call("look up alias " + alias(type), () -> {
            try {
                return Optional.of(client.aliases(alias(type)).retrieve().getCollectionName());
            } catch (ObjectNotFound e) {
                return Optional.<String>empty();
            }
        });
    }

    @Override
    public SearchCollection create(SearchEntityType type) {
        String name = alias(type) + "_" + UUID.randomUUID().toString().replace("-", "");
        call(
                "create collection " + name,
                () -> client.collections()
                        .create(new CollectionSchema().name(name).fields(fields(type))));
        return new SearchCollection(type, name);
    }

    @Override
    public void load(SearchCollection collection, List<SoftwareDocument> documents) {
        String answer = call(
                "load into " + collection.name(),
                () -> client.collections(collection.name())
                        .documents()
                        .import_(
                                documents.stream()
                                        .map(TypesenseSearchIndex::toMap)
                                        .toList(),
                                new ImportDocumentsParameters().action(IndexAction.CREATE)));
        // One line per document, each telling whether it was taken.
        for (String line : answer.lines().filter(l -> !l.isBlank()).toList()) {
            JsonNode result = call("read the import result", () -> json.readTree(line));
            if (!result.path("success").asBoolean(false)) {
                throw new SearchIndexException("Document not loaded into " + collection.name() + ": " + line);
            }
        }
    }

    @Override
    public void switchTo(SearchCollection collection) {
        Optional<String> previous = target(collection.type());
        call(
                "switch alias " + alias(collection.type()),
                () -> client.aliases()
                        .upsert(
                                alias(collection.type()),
                                new CollectionAliasSchema().collectionName(collection.name())));
        previous.filter(name -> !name.equals(collection.name()))
                .ifPresent(name -> drop(new SearchCollection(collection.type(), name)));
    }

    @Override
    public void drop(SearchCollection collection) {
        call("drop collection " + collection.name(), () -> {
            try {
                client.collections(collection.name()).delete();
            } catch (ObjectNotFound e) {
                // Already gone.
            }
            return collection;
        });
    }
}
