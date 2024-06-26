package com.swift.apidev.swiftref.configuration;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.endpoint.DefaultPasswordTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2PasswordGrantRequest;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.DefaultMapOAuth2AccessTokenResponseConverter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Boot OAuth 2.0 client configuration
 *
 * @see <a href=
 *      "https://docs.spring.io/spring-security/reference/reactive/oauth2/client/index.html">Spring
 *      Boot OAuth 2.0 client</a>
 */
@Configuration
@SuppressWarnings("deprecation")
public class OAuth2Configuration {

    @Bean
    OAuth2AuthorizedClientManager authorizedClientManager(
            ClientRegistrationRepository clientRegistrations,
            OAuth2AuthorizedClientService authorizedClientRepository,
            OAuth2AuthorizedClientProvider authorizedClientProvider,
            @Value("${swift.oauth2.username}") String username,
            @Value("${swift.oauth2.password}") String password) {
        var clientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                clientRegistrations, authorizedClientRepository);
        clientManager.setAuthorizedClientProvider(authorizedClientProvider);

        // username and password
        clientManager.setContextAttributesMapper(authorizeRequest -> {
            Map<String, Object> contextAttributes = new HashMap<>();
            contextAttributes.put(OAuth2AuthorizationContext.USERNAME_ATTRIBUTE_NAME, username);
            contextAttributes.put(OAuth2AuthorizationContext.PASSWORD_ATTRIBUTE_NAME, password);
            return contextAttributes;
        });

        return clientManager;
    }

    @Bean
    OAuth2AuthorizedClientProvider oAuth2AuthorizedClientProvider(
            OAuth2AccessTokenResponseClient<OAuth2PasswordGrantRequest> passwordTokenResponseClient) {
        var passwordClientProvider = new PasswordOAuth2AuthorizedClientProvider();
        passwordClientProvider.setAccessTokenResponseClient(passwordTokenResponseClient);
        return passwordClientProvider;
    }

    @Bean
    DefaultPasswordTokenResponseClient passwordClient(CloseableHttpClient httpClient) {
        OAuth2AccessTokenResponseHttpMessageConverter tokenResponseConverter = new OAuth2AccessTokenResponseHttpMessageConverter();
        tokenResponseConverter.setAccessTokenResponseConverter(new Converter<>() {
            final DefaultMapOAuth2AccessTokenResponseConverter delegate = new DefaultMapOAuth2AccessTokenResponseConverter();

            @Override
            public OAuth2AccessTokenResponse convert(Map<String, Object> source) {
                OAuth2AccessTokenResponse tokenResponse = delegate.convert(source);
                // Remove the refresh token because default implementation does not take into
                // account refresh token expiration
                return OAuth2AccessTokenResponse.withResponse(tokenResponse)
                        .refreshToken(null)
                        .build();
            }
        });

        // Swift API gateway does not allow x-www-form-urlencoded;charset=utf-8
        // (content-type+encoding) content type
        // Override the content type to not send the charset parameter
        var formHttpMessageConverter = new FormHttpMessageConverter() {
            @Override
            public void write(MultiValueMap<String, ?> map, @Nullable MediaType contentType,
                    HttpOutputMessage outputMessage)
                    throws IOException, HttpMessageNotWritableException {
                super.write(map, contentType, outputMessage);
                outputMessage.getHeaders().setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            }
        };

        // RestTemplate used to retrieve the OAuth token
        var oAuth2RestTemplate = new RestTemplateBuilder()
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory(httpClient))
                .messageConverters(Arrays.asList(formHttpMessageConverter,
                        new OAuth2AccessTokenResponseHttpMessageConverter()))
                .errorHandler(new OAuth2ErrorResponseErrorHandler()).build();

        var client = new DefaultPasswordTokenResponseClient();
        client.setRestOperations(oAuth2RestTemplate);
        return client;
    }
}
