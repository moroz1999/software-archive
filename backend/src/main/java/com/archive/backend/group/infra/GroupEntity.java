package com.archive.backend.group.infra;

import com.archive.backend.label.infra.LabelEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;
import org.jspecify.annotations.Nullable;

/**
 * A group with its details; its memberships are not part of it and go with it when it is deleted.
 */
@Entity
@Table(name = "\"group\"")
@Data
public class GroupEntity {
    /**
     * Assigned by the domain, never generated.
     */
    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    private @Nullable String notes;

    /**
     * Set by the database default.
     */
    @Column(name = "created_at", columnDefinition = "timestamptz", insertable = false, updatable = false)
    private Instant createdAt;

    @ElementCollection
    @CollectionTable(name = "group_group_type", joinColumns = @JoinColumn(name = "group_id"))
    @Column(name = "group_type_code")
    private Set<String> types = new HashSet<>();

    /**
     * Never removed with the group: a label dropped from the list, or of a deleted group, loses its owner only and
     * stays in the archive with its credits and memberships (docs/domain/authors.md).
     */
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "group_id")
    private List<LabelEntity> labels = new ArrayList<>();

    /**
     * The location table is shared by several kinds of owners; the group's rows are the ones of its type.
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "entity_id", nullable = false)
    @SQLRestriction("entity_type = 'GROUP'")
    private List<GroupLocationEntity> locations = new ArrayList<>();
}
