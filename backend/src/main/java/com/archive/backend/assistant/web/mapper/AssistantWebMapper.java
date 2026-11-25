package com.archive.backend.assistant.web.mapper;

import com.archive.backend.assistant.domain.chat.PageContext;
import com.archive.backend.assistant.read.chat.ChatRun;
import com.archive.backend.assistant.read.chat.ConversationListEntry;
import com.archive.backend.assistant.read.chat.ConversationState;
import com.archive.backend.assistant.web.dto.ChatRunDto;
import com.archive.backend.assistant.web.dto.ConversationListEntryDto;
import com.archive.backend.assistant.web.dto.ConversationStateDto;
import com.archive.backend.assistant.web.dto.PageContextRequestDto;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AssistantWebMapper {

    List<ConversationListEntryDto> toList(List<ConversationListEntry> entries);

    ConversationStateDto toDto(ConversationState state);

    ChatRunDto toDto(ChatRun run);

    default PageContext toPage(@Nullable PageContextRequestDto page) {
        return page == null
                ? PageContext.NONE
                : new PageContext(page.path(), page.title(), page.entityType(), page.entityId());
    }
}
