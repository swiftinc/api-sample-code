package com.swift.apidev.gpi.configuration;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.endpoint.DefaultJwtBearerTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.JwtBearerGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.util.MultiValueMap;
import com.swift.apidev.gpi.jwt.JwtOperations;

import java.io.IOException;
import java.util.Arrays;

/**
 * Spring Boot OAuth 2.0 client
 * <a href="https://datatracker.ietf.org/doc/html/rfc7523#section-2.1">using
 * JWTs as Authorization
 * Grants</a>
 *
 * @see <a href=
 *      "https://docs.spring.io/spring-security/reference/reactive/oauth2/client/index.html">Spring
 *      Boot OAuth 2.0 client</a>
 */
@Configuration
public class OAuth2Configuration {

        @Bean
        OAuth2AuthorizedClientManager authorizedClientManager(
                        ClientRegistrationRepository clientRegistrations,
                        OAuth2AuthorizedClientService authorizedClientRepository,
                        OAuth2AuthorizedClientProvider authorizedClientProvider) {
                var clientManager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                                clientRegistrations, authorizedClientRepository);
                clientManager.setAuthorizedClientProvider(authorizedClientProvider);
                return clientManager;
        }

        @Bean
        OAuth2AuthorizedClientProvider oAuth2AuthorizedClientProvider(
                        OAuth2AccessTokenResponseClient<JwtBearerGrantRequest> tokenResponseClient,
                        JwtOperations jwtOperations) {
                var authorizedClientProvider = new JwtBearerOAuth2AuthorizedClientProvider();
                authorizedClientProvider.setAccessTokenResponseClient(tokenResponseClient);
                authorizedClientProvider.setJwtAssertionResolver(jwtOperations::createAssertion);
                return authorizedClientProvider;
        }

        @Bean
        OAuth2AccessTokenResponseClient<JwtBearerGrantRequest> tokenResponseClient(
                        CloseableHttpClient httpClient) {
                // Swift API gateway does not allow x-www-form-urlencoded;charset=utf-8
                // (content-type+encoding) content type
                // Override the content type to not send the charset parameter
                var formHttpMessageConverter = new FormHttpMessageConverter() {
                        @Override
                        public void write(@NonNull MultiValueMap<String, ?> map, @Nullable MediaType contentType,
                                        @NonNull HttpOutputMessage outputMessage)
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

                var client = new DefaultJwtBearerTokenResponseClient();
                client.setRestOperations(oAuth2RestTemplate);
                return client;
        }
}
