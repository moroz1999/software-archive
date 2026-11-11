package com.archive.backend.author.infra;

import com.archive.backend.author.domain.Author;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.domain.AuthorNotes;
import com.archive.backend.author.domain.RealName;
import com.archive.backend.geo.infra.LocationRows;
import com.archive.backend.label.infra.LabelRows;
import org.mapstruct.Mapper;

/**
 * Author ↔ its JPA entity. Labels and places are restored without the checks: they were checked when stored.
 */
@Mapper(componentModel = "spring")
public interface AuthorMapper {

    default Author toDomain(AuthorEntity entity) {
        String realName = entity.getRealName();
        String notes = entity.getNotes();
        return new Author(
                new AuthorId(entity.getId()),
                realName == null ? null : new RealName(realName),
                notes == null ? null : new AuthorNotes(notes),
                LabelRows.toLabels(entity.getLabels()),
                LocationRows.toLocations(entity.getLocations()));
    }

    default AuthorEntity toEntity(Author author) {
        RealName realName = author.getRealName();
        AuthorNotes notes = author.getNotes();
        AuthorEntity entity = new AuthorEntity();
        entity.setId(author.getId().value());
        entity.setRealName(realName == null ? null : realName.value());
        entity.setNotes(notes == null ? null : notes.value());
        entity.setLabels(LabelRows.fromLabels(author.getLabels()));
        entity.setLocations(LocationRows.fromLocations(author.getLocations(), AuthorLocationEntity::new));
        return entity;
    }
}
