package com.archive.backend.importer.domain.source;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.domain.RealName;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.label.domain.LabelName;
import java.util.List;

/**
 * The people and groups of the archive an author a source names may be (docs/domain/imports.md#authors): only the
 * candidates by name, each still to be checked against its place.
 */
public interface MatchingAuthors {

    /** The people of that full real name, whatever its case, who have a label of that nickname, whatever its case. */
    List<AuthorId> people(RealName realName, LabelName nickname);

    /** The groups whose name or one of whose labels is that name, compared by its letters and digits alone. */
    List<GroupId> groups(LabelName name);
}
