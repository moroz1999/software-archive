package com.archive.backend.shared.config;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.oas.models.media.JsonSchema;
import io.swagger.v3.oas.models.media.Schema;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;

/**
 * Makes the OpenAPI schemas of the DTOs say what the code says.
 *
 * <ul>
 *   <li>Every component of a record is required: Jackson always writes it. One marked {@code @Nullable} may be
 *       {@code null}, as NullAway reads it; every other one never is.
 *   <li>A nested record is named after its enclosing class ({@code SoftwareDetailsDto.AddedDto}), so records of the
 *       same simple name in different DTOs stay different schemas.
 *   <li>A page always has its content and its metadata.
 *   <li>A {@code @JsonRawValue} string is written as the JSON it holds: its schema is what its {@code @Schema} says, not a
 *       string.
 * </ul>
 */
class ContractModelConverter implements ModelConverter {

    private final ObjectMapper mapper;

    ContractModelConverter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public @Nullable Schema resolve(AnnotatedType type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        if (!chain.hasNext()) {
            return null;
        }
        Class<?> raw = mapper.constructType(type.getType()).getRawClass();
        if (type.getName() == null && raw.getEnclosingClass() != null && !raw.isArray()) {
            type.name(raw.getEnclosingClass().getSimpleName() + "." + raw.getSimpleName());
        }
        Schema schema = chain.next().resolve(type, context, chain);
        if (schema != null && (raw.isRecord() || isPage(raw))) {
            Schema model = schema.get$ref() == null
                    ? schema
                    : context.getDefinedModels().get(refName(schema.get$ref()));
            if (model != null && raw.isRecord()) {
                describe(raw, model);
            } else if (model != null && model.getProperties() != null) {
                model.setRequired(new ArrayList<String>(model.getProperties().keySet()));
            }
        }
        return schema;
    }

    /** A page always has its content and its metadata (WebConfig: VIA_DTO). */
    private static boolean isPage(Class<?> raw) {
        return Page.class.isAssignableFrom(raw) || PagedModel.class.isAssignableFrom(raw);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void describe(Class<?> record, Schema model) {
        Map<String, Schema> properties = model.getProperties();
        if (properties == null) {
            return;
        }
        for (RecordComponent component : record.getRecordComponents()) {
            Schema property = properties.get(component.getName());
            if (property == null) {
                continue;
            }
            if (component.getAccessor().isAnnotationPresent(JsonRawValue.class) && property.getTypes() != null) {
                property.getTypes().remove("string");
            }
            if (component.getAnnotatedType().isAnnotationPresent(Nullable.class)) {
                properties.put(component.getName(), nullable(property));
            }
            List<String> required = model.getRequired();
            if (required == null || !required.contains(component.getName())) {
                model.addRequiredItem(component.getName());
            }
        }
    }

    /** OpenAPI 3.1 says "may be null" with the type {@code null}; a reference is offered beside it. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Schema nullable(Schema property) {
        if (property.get$ref() == null) {
            property.addType("null");
            return property;
        }
        return new JsonSchema()
                .anyOf(List.of(new JsonSchema().$ref(property.get$ref()), new JsonSchema().types(Set.of("null"))));
    }

    private static String refName(String ref) {
        return ref.substring(ref.lastIndexOf('/') + 1);
    }
}
