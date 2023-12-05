package com.swift.apidev.preval.security;

import io.quarkus.oidc.client.OidcClient;
import io.quarkus.oidc.client.Tokens;
import io.smallrye.jwt.build.Jwt;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.Claims;
import org.jboss.logging.Logger;

import com.swift.apidev.preval.configuration.ChannelCertificate;

import jakarta.annotation.PreDestroy;
import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.ext.Provider;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthorizationRequestFilter implements ClientRequestFilter {

    @Inject
    Logger log;

    @Inject
    OidcClient client;

    @Inject
    private ChannelCertificate channelCertificate;

    @ConfigProperty(name = "quarkus.oidc-client.client-id")
    Instance<String> issuer;

    @ConfigProperty(name = "quarkus.oidc-client.token-path")
    Instance<String> tokenUri;

    Tokens currentTokens;

    @Override
    public void filter(ClientRequestContext requestContext) {
        Tokens tokens = currentTokens;
        if (tokens == null || tokens.isAccessTokenExpired()) {
            synchronized (this) {
                if (tokens != currentTokens) {
                    tokens = currentTokens; // Token already refreshed by another thread
                } else {
                    log.info("Retrieving new token");
                    String audience = requestContext.getUri().getHost() + tokenUri.get();
                    tokens = retrieveNewToken(audience);
                }
            }
        }
        requestContext.getHeaders().add(HttpHeaders.AUTHORIZATION,
                "Bearer " + tokens.getAccessToken());
    }

    private Tokens retrieveNewToken(String audience) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(Duration.ofSeconds(15));

        String jwt = Jwt.audience(audience).subject(channelCertificate.getPrincipal())
                .issuer(issuer.get()).claim(Claims.jti.name(), UUID.randomUUID().toString())
                .issuedAt(issuedAt).expiresAt(expiresAt).jws()
                .header("x5c", Collections.singletonList(channelCertificate.getEncoded())).sign();

        Map<String, String> params = Collections.singletonMap("assertion", jwt);
        return client.getTokens(params).await().indefinitely();
    }

    @PreDestroy
    void destroy() {
        log.info("Revoking access token");
        if (currentTokens != null) {
            client.revokeAccessToken(currentTokens.getAccessToken()).await().indefinitely();
        }
    }
}
