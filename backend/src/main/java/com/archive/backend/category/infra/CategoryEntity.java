package com.archive.backend.category.infra;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.jspecify.annotations.Nullable;

/**
 * A category with its titles by the codes of their languages; its subcategories and its links to software go with it
 * when it is deleted, by the schema.
 */
@Entity
@Table(name = "category")
@Data
public class CategoryEntity {
    /**
     * Assigned by the domain, never generated.
     */
    @Id
    private UUID id;

    @Column(name = "parent_id")
    private @Nullable UUID parentId;

    @ElementCollection
    @CollectionTable(name = "category_title", joinColumns = @JoinColumn(name = "category_id"))
    @MapKeyColumn(name = "language")
    @Column(name = "title", nullable = false)
    private Map<String, String> titles = new HashMap<>();

    /**
     * The order among the children of the same parent.
     */
    @Column(nullable = false)
    private int position;
}
