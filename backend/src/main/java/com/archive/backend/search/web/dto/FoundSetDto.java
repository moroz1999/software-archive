package com.archive.backend.search.web.dto;

import io.swagger.v3.oas.annotations.media.DiscriminatorMapping;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** The matches of one kind on a page of the full search: {@code type} is the code of the kind. */
@Schema(
        oneOf = {FoundSetDto.Authors.class, FoundSetDto.Groups.class, FoundSetDto.Software.class},
        discriminatorProperty = "type",
        discriminatorMapping = {
            @DiscriminatorMapping(value = "author", schema = FoundSetDto.Authors.class),
            @DiscriminatorMapping(value = "group", schema = FoundSetDto.Groups.class),
            @DiscriminatorMapping(value = "software", schema = FoundSetDto.Software.class)
        })
public sealed interface FoundSetDto {

    record Authors(@Schema(allowableValues = "author") String type, long total, List<FoundAuthorDto> items)
            implements FoundSetDto {
        public Authors(long total, List<FoundAuthorDto> items) {
            this("author", total, items);
        }
    }

    record Groups(@Schema(allowableValues = "group") String type, long total, List<FoundGroupDto> items)
            implements FoundSetDto {
        public Groups(long total, List<FoundGroupDto> items) {
            this("group", total, items);
        }
    }

    record Software(@Schema(allowableValues = "software") String type, long total, List<SoftwareCatalogItemDto> items)
            implements FoundSetDto {
        public Software(long total, List<SoftwareCatalogItemDto> items) {
            this("software", total, items);
        }
    }
}
