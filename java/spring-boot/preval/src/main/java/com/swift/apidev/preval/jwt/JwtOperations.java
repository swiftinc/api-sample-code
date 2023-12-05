package com.swift.apidev.preval.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtOperations {

    final KeyStore keystore;
    final String keyAlias;
    final char[] keyPassword;

    public JwtOperations(SslBundles sslBundles) {
        SslBundle channelCertificate = sslBundles.getBundle("channel");
        this.keystore = channelCertificate.getStores().getKeyStore();
        this.keyAlias = channelCertificate.getKey().getAlias();
        this.keyPassword = channelCertificate.getKey().getPassword().toCharArray();
    }

    public Jwt createAssertion(OAuth2AuthorizationContext oAuth2AuthorizationContext)
            throws JwtException {
        final String tokenUri = oAuth2AuthorizationContext.getClientRegistration()
                .getProviderDetails().getTokenUri();
        final String audience = tokenUri.substring("https://".length());

        try {
            X509Certificate certificate = (X509Certificate) keystore.getCertificate(keyAlias);

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .x509CertChain(
                            Collections.singletonList(Base64.encode(certificate.getEncoded())))
                    .type(JOSEObjectType.JWT).build();

            Instant issuedAt = Instant.now();
            Instant expiresAt = issuedAt.plus(Duration.ofSeconds(15));

            // @formatter:off
            JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                    .subject(certificate.getSubjectX500Principal().getName())
                    .jwtID(UUID.randomUUID().toString())
                    .notBeforeTime(Date.from(issuedAt))
                    .issueTime(Date.from(issuedAt))
                    .expirationTime(Date.from(expiresAt))
                    .issuer(oAuth2AuthorizationContext.getClientRegistration().getClientId())
                    .audience(Collections.singletonList(audience))
                    .build();
            // @formatter:on

            SignedJWT signedJwt = signJwt(header, jwtClaimsSet);

            return new Jwt(signedJwt.serialize(), jwtClaimsSet.getIssueTime().toInstant(),
                    jwtClaimsSet.getExpirationTime().toInstant(), header.toJSONObject(),
                    jwtClaimsSet.getClaims());
        } catch (Exception e) {
            throw new JwtException(e);
        }
    }

    public String generateSignature(String url, byte[] body) throws JwtException {
        try {
            byte[] base64 = java.util.Base64.getEncoder().encode(body);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(base64);

            X509Certificate certificate = (X509Certificate) keystore.getCertificate(keyAlias);

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .x509CertChain(
                            Collections.singletonList(Base64.encode(certificate.getEncoded())))
                    .type(JOSEObjectType.JWT).build();

            Instant issuedAt = Instant.now();
            Instant expiresAt = issuedAt.plus(Duration.ofSeconds(15));
            
            // @formatter:off
            JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                    .subject(certificate.getSubjectX500Principal().getName())
                    .jwtID(UUID.randomUUID().toString())
                    .issueTime(Date.from(issuedAt))
                    .expirationTime(Date.from(expiresAt))
                    .audience(Collections.singletonList(url.substring("https://".length())))
                    .claim("digest", java.util.Base64.getEncoder().encodeToString(digest))
                    .build();
            // @formatter:on

            SignedJWT signedJwt = signJwt(header, jwtClaimsSet);

            return signedJwt.serialize();
        } catch (Exception e) {
            throw new JwtException(e);
        }
    }

    private SignedJWT signJwt(JWSHeader header, JWTClaimsSet jwtClaimsSet)
            throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException,
            JOSEException {
        PrivateKey privateKey = (PrivateKey) keystore.getKey(keyAlias, keyPassword);
        RSASSASigner signer = new RSASSASigner(privateKey);
        SignedJWT signedJwt = new SignedJWT(header, jwtClaimsSet);
        signedJwt.sign(signer);
        return signedJwt;
    }
}
