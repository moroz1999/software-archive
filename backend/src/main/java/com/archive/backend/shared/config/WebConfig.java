package com.archive.backend.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

/**
 * Pages are serialized as {@code {content: [...], page: {size, number, totalElements, totalPages}}}:
 * a stable REST contract instead of Spring Data's internal PageImpl structure.
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebConfig {
}
