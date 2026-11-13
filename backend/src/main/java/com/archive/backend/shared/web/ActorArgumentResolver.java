package com.archive.backend.shared.web;

import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.UserId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * Gives a controller the {@link Actor} of the request: the signed-in user of its access token, or a public visitor.
 */
public class ActorArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().equals(Actor.class);
    }

    @Override
    public Actor resolveArgument(
            MethodParameter parameter,
            @Nullable ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            @Nullable WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return Actor.anonymous();
        }
        List<String> groups = jwt.getClaimAsStringList("groups");
        Set<UserGroup> userGroups = groups == null
                ? Set.of()
                : groups.stream().map(UserGroup::ofCode).collect(Collectors.toSet());
        return new Actor(new UserId(UUID.fromString(jwt.getSubject())), userGroups);
    }
}
