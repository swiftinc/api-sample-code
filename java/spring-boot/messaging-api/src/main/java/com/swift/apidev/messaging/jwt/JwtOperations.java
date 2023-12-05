package com.swift.apidev.messaging.jwt;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.util.Base64;
import com.nimbusds.jose.util.X509CertChainUtils;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.apache.hc.client5.http.utils.Hex;
import org.erdtman.jcs.JsonCanonicalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ssl.NoSuchSslBundleException;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtOperations {
    private static final Logger LOG = LoggerFactory.getLogger(JwtOperations.class);

    final KeyStore keystore;
    final String keyAlias;
    final char[] keyPassword;
    final KeyStore trustStore;

    public JwtOperations(SslBundles sslBundles) {
        final SslBundle channelCertificate = sslBundles.getBundle("channel");
        this.keystore = channelCertificate.getStores().getKeyStore();
        this.keyAlias = channelCertificate.getKey().getAlias();
        this.keyPassword = channelCertificate.getKey().getPassword().toCharArray();

        SslBundle clientSslBundle = null;
        try {
            clientSslBundle = sslBundles.getBundle("client");
        } catch (NoSuchSslBundleException e) {
            LOG.warn("Client SSL bundle not found");
        }

        if (clientSslBundle != null) {
            this.trustStore = clientSslBundle.getStores().getTrustStore();
        } else {
            // In production environment, you must verify the trust chain against the Swift Root CA
            // In sandbox this verification is skipped
            LOG.warn("No trust store found, skipping trust verification");
            this.trustStore = null;
        }
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

    public void verifySignature(String signature, byte[] body) throws JwtException {
        try {
            JWSObject jws = JWSObject.parse(signature);

            if (jws.getHeader().getAlgorithm() != JWSAlgorithm.RS256) {
                throw new JwtException("Invalid algorithm");
            }

            String digestClaim = (String) jws.getPayload().toJSONObject().get("digest");

            JsonCanonicalizer jsonCanonicalizer = new JsonCanonicalizer(body);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(jsonCanonicalizer.getEncodedUTF8());
            String digestEncodedInHex = Hex.encodeHexString(digest);

            if (!digestClaim.equals(digestEncodedInHex)) {
                throw new JwtException("Invalid payload digest");
            }

            List<X509Certificate> certChain = X509CertChainUtils.parse(jws.getHeader().getX509CertChain());
            verifyTrust(certChain);
            RSAPublicKey publicKey = (RSAPublicKey) certChain.get(0).getPublicKey();
            RSASSAVerifier verifier = new RSASSAVerifier(publicKey);
            if (!jws.verify(verifier)) {
                throw new JwtException("Invalid signature");
            }
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

    private void verifyTrust(final List<X509Certificate> certChain) {
        try {
            if (trustStore == null) {
                return;
            }

            final Date now = new Date();

            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            final CertPath certPath = certificateFactory.generateCertPath(certChain);

            PKIXParameters pkixParameters = new PKIXParameters(trustStore);
            pkixParameters.setRevocationEnabled(false);
            pkixParameters.setDate(now);

            CertPathValidator certPathValidator = CertPathValidator.getInstance("PKIX");
            PKIXCertPathBuilderResult result = (PKIXCertPathBuilderResult) certPathValidator.validate(certPath,
                    pkixParameters);

            final TrustAnchor trustAnchor = result.getTrustAnchor();
            if (trustAnchor == null) {
                throw new JwtException("No trust anchor found");
            }
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | KeyStoreException
                | CertPathValidatorException | CertificateException e) {
            throw new JwtException(e);
        }

    }
}
