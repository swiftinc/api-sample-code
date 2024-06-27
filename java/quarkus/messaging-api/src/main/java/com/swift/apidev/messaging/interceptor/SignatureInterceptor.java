package com.swift.apidev.messaging.interceptor;

import io.smallrye.jwt.build.Jwt;

import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.jwt.Claims;
import org.jboss.resteasy.reactive.server.jsonb.JsonbMessageBodyWriter;

import com.swift.apidev.messaging.configuration.ChannelCertificate;

@Provider
@ConstrainedTo(RuntimeType.CLIENT)
public class SignatureInterceptor extends JsonbMessageBodyWriter implements ClientRequestFilter {

    private static final String PATH_HEADER = "X-Audience";

    final ChannelCertificate signingCertificate;

    @Inject
    SignatureInterceptor(Jsonb json, ChannelCertificate signingCertificate) {
        super(json);
        this.signingCertificate = signingCertificate;
    }

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        URI uri = requestContext.getUri();
        String audience = uri.getHost() + uri.getPath();
        requestContext.getHeaders().add(PATH_HEADER, audience);
    }

    @Override
    public void writeTo(Object o, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {

        ByteArrayOutputStream serializedBody = new ByteArrayOutputStream();
        
        super.writeTo(o, type, genericType, annotations, mediaType, httpHeaders, serializedBody);

        byte[] body = serializedBody.toByteArray();
        String signature = sign(body, (String) httpHeaders.getFirst(PATH_HEADER));
        httpHeaders.replace("X-SWIFT-Signature", List.of(signature));
        httpHeaders.remove(PATH_HEADER);
     
        entityStream.write(serializedBody.toByteArray());
    }

    public String sign(byte[] body, String audience) {
        byte[] digest;
        try {
            byte[] base64 = Base64.getEncoder().encode(body);
            digest = MessageDigest.getInstance("SHA-256").digest(base64);
        } catch (NoSuchAlgorithmException e) {
            throw new WebApplicationException(e);
        }

        String base64Digest = Base64.getEncoder().encodeToString(digest);
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(Duration.ofSeconds(20));

        return Jwt.audience(audience)
                .subject(signingCertificate.getPrincipal())
                .claim(Claims.jti.name(), UUID.randomUUID().toString())
                .issuedAt(issuedAt)
                .issuedAt(issuedAt).expiresAt(expiresAt)
                .claim("digest", base64Digest)
                .jws()
                .header("x5c", Collections.singletonList(signingCertificate.getEncoded()))
                .sign();
    }
}
