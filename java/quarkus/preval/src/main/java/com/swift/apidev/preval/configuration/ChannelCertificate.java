package com.swift.apidev.preval.configuration;

import io.smallrye.jwt.build.impl.JwtBuildUtils;
import io.smallrye.jwt.util.KeyUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.jose4j.base64url.Base64;

import jakarta.enterprise.context.ApplicationScoped;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.Optional;

@ApplicationScoped
public class ChannelCertificate {
    final String encoded;

    final String principal;

    public ChannelCertificate() throws Exception {
        KeyStore keyStore = KeyUtils.loadKeyStore(
                getOptionalStringConfigProperty(JwtBuildUtils.SIGN_KEY_LOCATION_PROPERTY)
                        .orElseThrow(),
                getOptionalStringConfigProperty(JwtBuildUtils.KEYSTORE_PASSWORD).orElseThrow(),
                getOptionalStringConfigProperty(JwtBuildUtils.KEYSTORE_TYPE),
                getOptionalStringConfigProperty(JwtBuildUtils.KEYSTORE_PROVIDER));

        X509Certificate signCertificate = (X509Certificate) keyStore.getCertificate(
                getOptionalStringConfigProperty(JwtBuildUtils.SIGN_KEYSTORE_KEY_ALIAS)
                        .orElseThrow());

        this.encoded = Base64.encode(signCertificate.getEncoded());
        this.principal = signCertificate.getSubjectX500Principal().getName();
    }

    public String getEncoded() {
        return encoded;
    }

    public String getPrincipal() {
        return principal;
    }

    private Optional<String> getOptionalStringConfigProperty(String name) {
        return ConfigProvider.getConfig().getOptionalValue(name, String.class);
    }
}
