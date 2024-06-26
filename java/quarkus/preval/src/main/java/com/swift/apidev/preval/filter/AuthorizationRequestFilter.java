package com.swift.apidev.preval.filter;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;
import org.jboss.resteasy.reactive.client.spi.ResteasyReactiveClientRequestContext;

import com.swift.apidev.preval.configuration.ChannelCertificate;

import io.quarkus.oidc.client.reactive.filter.runtime.AbstractOidcClientRequestReactiveFilter;
import io.smallrye.jwt.build.Jwt;
import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthorizationRequestFilter extends AbstractOidcClientRequestReactiveFilter {

    @ConfigProperty(name = "quarkus.oidc-client.client-id")
    Instance<String> issuer;

    @ConfigProperty(name = "quarkus.oidc-client.token-path")
    Instance<String> tokenUri;

    final ChannelCertificate channelCertificate;

    String audience;

    @Inject
    AuthorizationRequestFilter(ChannelCertificate channelCertificate) {
        this.channelCertificate = channelCertificate;
    }

    @Override
    public void filter(ResteasyReactiveClientRequestContext requestContext) {
        this.audience = requestContext.getUri().getHost() + tokenUri.get();
        super.filter(requestContext);
    }

    @Override
    protected Map<String, String> additionalParameters() {
        return Map.of("assertion", assertion(audience));
    }

    private String assertion(String audience) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(Duration.ofSeconds(15));

        return Jwt.audience(audience)
                .subject(channelCertificate.getPrincipal())
                .issuer(issuer.get())
                .claim(Claims.jti.name(), UUID.randomUUID().toString())
                .issuedAt(issuedAt).expiresAt(expiresAt)
                .jws()
                .header("x5c", Collections.singletonList(channelCertificate.getEncoded()))
                .sign();
    }
}
