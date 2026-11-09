package com.archive.backend.label.read;

import java.util.Map;

/**
 * The SQL condition of the platform switch on a catalog of names (docs/domain/hardware.md#platform-switch): an owner
 * is kept when one of its labels is credited on software of the platform.
 */
public final class PlatformFilterSql {

    private PlatformFilterSql() {}

    /**
     * The condition on the owner column {@code ownerColumn} of the entries, matched against the label column
     * {@code labelOwnerColumn} ({@code person_id} or {@code group_id}), adding the parameters it needs to {@code params}.
     */
    public static String condition(
            String ownerColumn, String labelOwnerColumn, String platform, Map<String, Object> params) {
        params.put("platform", platform);
        return """
                EXISTS (
                    SELECT 1 FROM software_contribution sc
                    JOIN label cl ON cl.id = sc.label_id
                    JOIN software s ON s.id = sc.software_id
                    WHERE cl.%s = %s AND s.platform_code = :platform
                )""".formatted(labelOwnerColumn, ownerColumn);
    }
}
