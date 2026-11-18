package com.archive.backend.shared.web;

import com.archive.backend.shared.domain.Language;
import java.util.Locale;
import java.util.Optional;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Gives a controller the {@link Language} the request asks its content in: the most wanted language of its
 * {@code Accept-Language} the site speaks, else English, as the interface falls back to.
 */
public class LanguageArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(Language.class);
    }

    @Override
    public Language resolveArgument(
            MethodParameter parameter,
            @Nullable ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            @Nullable WebDataBinderFactory binderFactory) {
        String header = webRequest.getHeader(HttpHeaders.ACCEPT_LANGUAGE);
        return header == null ? Language.EN : wanted(header);
    }

    private static Language wanted(String header) {
        try {
            return Locale.LanguageRange.parse(header).stream()
                    .map(range -> Language.find(
                            Locale.forLanguageTag(range.getRange()).getLanguage()))
                    .flatMap(Optional::stream)
                    .findFirst()
                    .orElse(Language.EN);
        } catch (IllegalArgumentException e) {
            // A header that is no list of languages asks for none.
            return Language.EN;
        }
    }
}
