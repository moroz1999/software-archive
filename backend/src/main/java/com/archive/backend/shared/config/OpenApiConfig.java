package com.archive.backend.shared.config;

import static java.util.Map.entry;

import com.archive.backend.assistant.domain.chat.MessageKind;
import com.archive.backend.assistant.domain.chat.MessageRole;
import com.archive.backend.assistant.domain.chat.PlanActionStatus;
import com.archive.backend.assistant.domain.chat.PlanStatus;
import com.archive.backend.assistant.domain.chat.RunStatus;
import com.archive.backend.assistant.domain.chat.RunType;
import com.archive.backend.author.domain.PersonReferenceType;
import com.archive.backend.group.domain.GroupMemberDraft;
import com.archive.backend.importer.domain.ExternalKind;
import com.archive.backend.importer.domain.run.AddedPlace;
import com.archive.backend.importer.domain.run.ImportLogEntry;
import com.archive.backend.importer.domain.run.ImportRunState;
import com.archive.backend.importer.domain.run.SkipReason;
import com.archive.backend.importer.domain.run.TakenAuthor;
import com.archive.backend.search.domain.CatalogOrder;
import com.archive.backend.search.domain.FoundKind;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.software.domain.PreservationStatus;
import com.archive.backend.software.domain.ReleaseType;
import com.archive.backend.software.domain.SoftwareCreditDraft;
import com.archive.backend.software.domain.SoftwareLinkType;
import com.archive.backend.software.domain.SoftwareReferenceType;
import com.archive.backend.software.domain.SoftwareSupplement;
import com.archive.backend.user.domain.Ability;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.UserGroup;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.ParameterCustomizer;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** The OpenAPI document of the REST API (/v3/api-docs), from which the frontend is typed (docs/backend.md). */
@Configuration
class OpenApiConfig {

    static {
        // Resolved from the request itself (the access token, Accept-Language), not parameters the client sends.
        SpringDocUtils.getConfig().addRequestWrapperToIgnore(Actor.class, Language.class);
    }

    /** A parameter the call may leave out may be given as null too: the client leaves it out then. */
    @Bean
    ParameterCustomizer optionalParametersMayBeNull() {
        return (parameter, methodParameter) -> {
            // Null for an argument that is no parameter of the call (Actor, Language).
            if (parameter == null) {
                return null;
            }
            Schema<?> schema = parameter.getSchema();
            if (!Boolean.TRUE.equals(parameter.getRequired()) && schema != null && schema.get$ref() == null) {
                schema.addType("null");
            }
            return parameter;
        };
    }

    /**
     * Every fixed set of values the contract names, as the code of the domain holds it: a field refers to its set by
     * name ({@code @Schema(ref = "ReleaseType")}), and the frontend takes the set from there. Sorted by name, so the
     * document is the same on every run.
     */
    static final SortedMap<String, List<String>> VALUE_SETS = new TreeMap<>(Map.ofEntries(
            entry("ReleaseType", codes(ReleaseType.values(), ReleaseType::code)),
            entry("PreservationStatus", codes(PreservationStatus.values(), PreservationStatus::code)),
            entry("SoftwareReferenceType", codes(SoftwareReferenceType.values(), SoftwareReferenceType::code)),
            entry("SoftwareLinkType", codes(SoftwareLinkType.values(), SoftwareLinkType::code)),
            entry("CreditRole", SoftwareCreditDraft.ALLOWED_ROLES),
            entry("GroupMemberRole", GroupMemberDraft.ALLOWED_ROLES),
            entry("PersonReferenceType", codes(PersonReferenceType.values(), PersonReferenceType::code)),
            entry("UserGroup", codes(UserGroup.values(), UserGroup::code)),
            entry("Ability", codes(Ability.values(), Ability::code)),
            entry("Language", codes(Language.values(), Language::code)),
            entry("CatalogOrder", codes(CatalogOrder.values(), CatalogOrder::code)),
            entry("FoundKind", codes(FoundKind.values(), FoundKind::code)),
            entry("ImportRunState", codes(ImportRunState.values(), ImportRunState::code)),
            entry("SoftwareSupplement", codes(SoftwareSupplement.values(), SoftwareSupplement::code)),
            entry("DownloadedFile", codes(ImportLogEntry.DownloadedFile.values(), ImportLogEntry.DownloadedFile::code)),
            entry("SkipReason", codes(SkipReason.values(), SkipReason::code)),
            entry(
                    "TakenAuthorKind",
                    TakenAuthor.KINDS.stream().map(ExternalKind::code).toList()),
            entry("PlaceKind", codes(AddedPlace.Kind.values(), AddedPlace.Kind::code)),
            entry("MessageRole", codes(MessageRole.values(), MessageRole::code)),
            entry("MessageKind", codes(MessageKind.values(), MessageKind::code)),
            entry("PlanStatus", codes(PlanStatus.values(), PlanStatus::code)),
            entry("PlanActionStatus", codes(PlanActionStatus.values(), PlanActionStatus::code)),
            entry("RunType", codes(RunType.values(), RunType::code)),
            entry("RunStatus", codes(RunStatus.values(), RunStatus::code))));

    private static <E> List<String> codes(E[] values, Function<E, String> code) {
        return Arrays.stream(values).map(code).toList();
    }

    @Bean
    OpenApiCustomizer valueSets() {
        return openApi -> VALUE_SETS.forEach((name, values) -> {
            JsonSchema set = new JsonSchema();
            set.setTypes(Set.of("string"));
            set.setEnum(new ArrayList<Object>(values));
            openApi.getComponents().addSchemas(name, set);
        });
    }

    @Bean
    ModelConverter contractModelConverter(ObjectMapperProvider mappers) {
        return new ContractModelConverter(mappers.jsonMapper());
    }
}
