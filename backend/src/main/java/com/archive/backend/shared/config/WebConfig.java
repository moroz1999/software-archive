package com.archive.backend.shared.config;

import com.archive.backend.shared.web.ActorArgumentResolver;
import com.archive.backend.shared.web.LanguageArgumentResolver;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Pages are serialized as {@code {content: [...], page: {size, number, totalElements, totalPages}}}:
 * a stable REST contract instead of Spring Data's internal PageImpl structure.
 * Controllers get the {@link com.archive.backend.user.domain.Actor} of the request and the
 * {@link com.archive.backend.shared.domain.Language} it asks its content in as arguments.
 */
@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new ActorArgumentResolver());
        resolvers.add(new LanguageArgumentResolver());
    }
}
