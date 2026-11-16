package com.archive.backend.user.infra;

import com.archive.backend.user.domain.AddedBy;
import com.archive.backend.user.domain.UserId;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/**
 * Who added an entry ↔ the {@code added_by} column of its row: empty once the adder's account was deleted.
 */
public final class AddedByRows {

    private AddedByRows() {}

    public static AddedBy toAddedBy(@Nullable UUID user) {
        return user == null ? AddedBy.ACCOUNT_DELETED : new AddedBy(new UserId(user));
    }

    public static @Nullable UUID fromAddedBy(AddedBy addedBy) {
        UserId user = addedBy.user();
        return user == null ? null : user.value();
    }
}
