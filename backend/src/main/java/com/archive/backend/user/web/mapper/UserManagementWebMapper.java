package com.archive.backend.user.web.mapper;

import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.user.app.ChangeUserGroupsCommand;
import com.archive.backend.user.app.ListUsersQuery;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.GrantedGroups;
import com.archive.backend.user.domain.UserId;
import com.archive.backend.user.domain.UserSearch;
import com.archive.backend.user.read.UserEntry;
import com.archive.backend.user.web.dto.UserEntryDto;
import com.archive.backend.user.web.dto.UserGroupsRequestDto;
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

    default ChangeUserGroupsCommand toChangeGroups(Actor actor, UUID id, UserGroupsRequestDto request) {
        List<String> groups = request.groups();
        if (groups == null) {
            throw new InvalidValueException("user.groups-required", "The groups are missing.");
        }
        return new ChangeUserGroupsCommand(actor, new UserId(id), GrantedGroups.ofCodes(groups));
    }
}
