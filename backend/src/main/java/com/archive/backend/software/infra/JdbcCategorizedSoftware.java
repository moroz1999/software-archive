package com.archive.backend.software.infra;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.software.domain.CategorizedSoftware;
import com.archive.backend.software.domain.SoftwareId;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcCategorizedSoftware implements CategorizedSoftware {

    private final JdbcClient jdbc;

    @Override
    public List<SoftwareId> inCategoriesAboveOthersAfterMoving(CategoryId moved) {
        return jdbc.sql("""
                        WITH RECURSIVE below (id) AS (
                            SELECT CAST(:moved AS uuid)
                            UNION
                            SELECT c.id FROM category c JOIN below b ON c.parent_id = b.id
                        ),
                        above (id) AS (
                            SELECT parent_id FROM category WHERE id = :moved AND parent_id IS NOT NULL
                            UNION
                            SELECT c.parent_id FROM category c JOIN above a ON c.id = a.id WHERE c.parent_id IS NOT NULL
                        )
                        SELECT DISTINCT low.software_id
                        FROM software_category low
                        JOIN software_category high ON high.software_id = low.software_id
                        WHERE low.category_id IN (SELECT id FROM below)
                          AND high.category_id IN (SELECT id FROM above)
                        ORDER BY low.software_id
                        """).param("moved", moved.value()).query(UUID.class).list().stream()
                .map(SoftwareId::new)
                .toList();
    }
}
