package com.swift.apidev.messaging.security;

import io.smallrye.jwt.build.Jwt;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.ext.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;
import org.eclipse.microprofile.jwt.Claims;
import org.jboss.logging.Logger;
import com.swift.apidev.messaging.configuration.ChannelCertificate;

@Provider
@ConstrainedTo(RuntimeType.CLIENT)
@Priority(Priorities.HEADER_DECORATOR)
public class SignatureInterceptor implements WriterInterceptor, ClientRequestFilter {

    private static final String PATH_PROPERTY = "URI";

    @Inject
    Logger log;

    @Inject
    ChannelCertificate signingCertificate;

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        requestContext.setProperty(PATH_PROPERTY, requestContext.getUri());
    }

    @Override
    public void aroundWriteTo(WriterInterceptorContext context)
            throws IOException, WebApplicationException {
        OutputStream old = context.getOutputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        context.setOutputStream(buffer);
        context.proceed(); // let MessageBodyWriter do it's job
        byte[] body = buffer.toByteArray();
        String signature = sign(body, (URI) context.getProperty(PATH_PROPERTY));
        context.getHeaders().putSingle("X-SWIFT-Signature", signature);
        old.write(body);
        context.setOutputStream(old);
    }

    public String sign(byte[] body, URI uri) {
        byte[] digest;
        try {
            byte[] base64 = Base64.getEncoder().encode(body);
            digest = MessageDigest.getInstance("SHA-256").digest(base64);
        } catch (NoSuchAlgorithmException e) {
            throw new WebApplicationException(e);
        }

        String base64Digest = Base64.getEncoder().encodeToString(digest);
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(Duration.ofSeconds(900));

        return Jwt.audience(uri.getHost() + uri.getPath())
                .subject(signingCertificate.getPrincipal())
                .claim(Claims.jti.name(), UUID.randomUUID().toString()).issuedAt(issuedAt)
                .issuedAt(issuedAt).expiresAt(expiresAt).claim("digest", base64Digest).jws()
                .header("x5c", Collections.singletonList(signingCertificate.getEncoded())).sign();
    }
}
