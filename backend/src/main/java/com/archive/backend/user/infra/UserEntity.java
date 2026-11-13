package com.archive.backend.user.infra;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "app_user")
@Data
public class UserEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt;

    @Column(nullable = false)
    private String status;

    @Column(name = "confirmation_digest")
    private @Nullable String confirmationDigest;

    @Column(name = "confirmation_expires_at")
    private @Nullable Instant confirmationExpiresAt;

    /** Group codes. */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "app_user_group", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "user_group")
    private Set<String> groups = new HashSet<>();
}
