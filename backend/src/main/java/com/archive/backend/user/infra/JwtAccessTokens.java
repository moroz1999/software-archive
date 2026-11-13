package com.archive.backend.user.infra;

import com.archive.backend.shared.config.AuthProperties;
import com.archive.backend.shared.config.SecurityConfig;
import com.archive.backend.user.domain.User;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.session.AccessToken;
import com.archive.backend.user.domain.session.AccessTokens;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

/**
 * Access tokens are JWTs signed with HS256: the subject is the user id, {@code groups} their group codes.
 */
@Component
public class JwtAccessTokens implements AccessTokens {

    private final AuthProperties properties;
    private final JwtEncoder encoder;

    public JwtAccessTokens(AuthProperties properties) {
        this.properties = properties;
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(SecurityConfig.secretKey(properties)));
    }

    @Override
    public AccessToken issue(User user, Instant now) {
        Instant expiresAt = now.plus(properties.accessLifetime());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(user.getId().value().toString())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim(
                        "groups",
                        user.getGroups().stream().map(UserGroup::code).sorted().toList())
                .build();
        String token = encoder.encode(JwtEncoderParameters.from(
                        JwsHeader.with(MacAlgorithm.HS256).build(), claims))
                .getTokenValue();
        return new AccessToken(token, expiresAt);
    }
}
