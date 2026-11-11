package com.archive.backend.author.infra;

import com.archive.backend.label.infra.LabelEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import org.hibernate.annotations.SQLRestriction;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "person")
@Data
public class AuthorEntity {
    /**
     * Assigned by the domain, never generated.
     */
    @Id
    private UUID id;

    @Column(name = "real_name")
    private @Nullable String realName;

    private @Nullable String notes;

    /**
     * Set by the database default.
     */
    @Column(name = "created_at", columnDefinition = "timestamptz", insertable = false, updatable = false)
    private Instant createdAt;

    /**
     * Never removed with the person: a label dropped from the list, or of a deleted person, loses its owner only and
     * stays in the archive with its credits and memberships (docs/domain/authors.md).
     */
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "person_id")
    private List<LabelEntity> labels = new ArrayList<>();

    /**
     * The location table is shared by several kinds of owners; the person's rows are the ones of its type.
     */
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "entity_id", nullable = false)
    @SQLRestriction("entity_type = 'PERSON'")
    private List<AuthorLocationEntity> locations = new ArrayList<>();
}
