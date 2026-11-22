package com.archive.backend.group.infra;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "group_member")
@Data
public class GroupMemberEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "person_id")
    private @Nullable UUID personId;

    @Column(name = "label_id")
    private @Nullable UUID labelId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "role_codes", columnDefinition = "text[]", nullable = false)
    private List<String> roleCodes = new ArrayList<>();
}
