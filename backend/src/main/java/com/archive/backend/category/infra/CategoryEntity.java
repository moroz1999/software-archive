package com.archive.backend.category.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.Data;
import org.jspecify.annotations.Nullable;

/**
 * A category; its subcategories and its links to software go with it when it is deleted, by the schema.
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

    @Column(nullable = false)
    private String title;

    /**
     * The order among the children of the same parent.
     */
    @Column(nullable = false)
    private int position;
}
