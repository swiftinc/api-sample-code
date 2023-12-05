package com.swift.apidev.messaging.interceptor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;

import java.io.IOException;
import java.util.Objects;

@Component
public class AuthorizationInterceptor implements ClientHttpRequestInterceptor {

    private static final String BEARER_WITH_SPACE = "Bearer ";

    final OAuth2AuthorizedClientManager clientManager;

    AuthorizationInterceptor(OAuth2AuthorizedClientManager clientManager) {
        this.clientManager = clientManager;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        // Attempt to authorize or re-authorize (if required)
        var authorizeRequest = OAuth2AuthorizeRequest.withClientRegistrationId("swift")
                .principal("swift")
                .build();

        // Retrieve the access token or reuse the existing one
        var authorizedClient = clientManager.authorize(authorizeRequest);
        Objects.requireNonNull(authorizedClient, "Client credentials failed, client is null");

        final String token = authorizedClient.getAccessToken().getTokenValue();

        // Set the Bearer token in 'Authorization' header
        request.getHeaders().add(HttpHeaders.AUTHORIZATION, BEARER_WITH_SPACE.concat(token));

        return execution.execute(request, body);
    }
}
