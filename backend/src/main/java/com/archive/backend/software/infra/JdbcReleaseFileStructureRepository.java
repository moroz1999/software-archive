package com.archive.backend.software.infra;

import com.archive.backend.software.domain.ReleaseFile;
import com.archive.backend.software.domain.ReleaseFileHash;
import com.archive.backend.software.domain.ReleaseFileId;
import com.archive.backend.software.domain.ReleaseFileStructureRepository;
import com.archive.backend.software.domain.ReleaseId;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcReleaseFileStructureRepository implements ReleaseFileStructureRepository {

    private final JdbcTemplate jdbc;

    @Override
    public void replace(ReleaseId release, ReleaseFile root) {
        discard(release);
        insert(release, null, 0, root);
    }

    @Override
    public void discard(ReleaseId release) {
        jdbc.update("DELETE FROM release_file WHERE release_id = ?", release.value());
    }

    private void insert(ReleaseId release, @Nullable ReleaseFileId parent, int position, ReleaseFile file) {
        ReleaseFileHash hash = file.md5();
        jdbc.update(
                """
                INSERT INTO release_file
                    (id, release_id, parent_id, file_name, position, md5, directory, executable, unreadable)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                file.id().value(),
                release.value(),
                parent == null ? null : parent.value(),
                file.name().value(),
                position,
                hash == null ? null : hash.value(),
                file.directory(),
                file.executable(),
                file.unreadable());
        for (int childPosition = 0; childPosition < file.children().size(); childPosition++) {
            insert(release, file.id(), childPosition, file.children().get(childPosition));
        }
    }
}
