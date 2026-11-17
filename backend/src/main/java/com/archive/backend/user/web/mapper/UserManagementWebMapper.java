package com.archive.backend.user.web.mapper;

import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.user.app.EditUserCommand;
import com.archive.backend.user.app.GetUserQuery;
import com.archive.backend.user.app.ListUsersQuery;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.GrantedGroups;
import com.archive.backend.user.domain.UserId;
import com.archive.backend.user.domain.UserName;
import com.archive.backend.user.domain.UserSearch;
import com.archive.backend.user.read.UserEntry;
import com.archive.backend.user.web.dto.UserEntryDto;
import com.archive.backend.user.web.dto.UserRequestDto;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(componentModel = "spring")
public interface UserManagementWebMapper {

    UserEntryDto toDto(UserEntry entry);

    default ListUsersQuery toListQuery(Actor actor, @Nullable String query, Pageable pageable) {
        return new ListUsersQuery(actor, UserSearch.of(query), pageable);
    }

    default GetUserQuery toGetQuery(Actor actor, UUID id) {
        return new GetUserQuery(actor, new UserId(id));
    }

    default EditUserCommand toEdit(Actor actor, UUID id, UserRequestDto request) {
        String name = request.name();
        String email = request.email();
        List<String> groups = request.groups();
        if (name == null) {
            throw new InvalidValueException("user.name-required", "A user name must not be blank.");
        }
        if (email == null) {
            throw new InvalidValueException("user.email-required", "An email must not be blank.");
        }
        if (groups == null) {
            throw new InvalidValueException("user.groups-required", "The groups are missing.");
        }
        return new EditUserCommand(
                actor, new UserId(id), new UserName(name), new Email(email), GrantedGroups.ofCodes(groups));
    }
}
