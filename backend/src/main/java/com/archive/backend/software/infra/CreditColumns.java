package com.archive.backend.software.infra;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

/** One credit as software and its releases store it: exactly one of a person, a group or a label, with the roles. */
@MappedSuperclass
@Data
public abstract class CreditColumns {

    @Id
    @GeneratedValue
    private @Nullable UUID id;

    @Column(name = "person_id")
    private @Nullable UUID personId;

    @Column(name = "group_id")
    private @Nullable UUID groupId;

    @Column(name = "label_id")
    private @Nullable UUID labelId;

    /** Whether the credited label is of a group: a label is credited the same way whoever owns it. */
    @Formula("(SELECT l.group_id IS NOT NULL FROM label l WHERE l.id = label_id)")
    private @Nullable Boolean labelOfGroup;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "role_codes", columnDefinition = "text[]", nullable = false)
    private List<String> roleCodes = new ArrayList<>();
}
