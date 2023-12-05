package com.swift.apidev.swiftref.configuration;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.impl.routing.DefaultProxyRoutePlanner;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactoryBuilder;
import org.apache.hc.core5.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ssl.NoSuchSslBundleException;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLContext;

import static java.lang.Integer.parseInt;

@Configuration
public class HttpClientConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(HttpClientConfiguration.class);

    final String proxyHost;

    final String proxyPort;

    public HttpClientConfiguration(@Value("${swift.proxy.host:}") String proxyHost, @Value("${swift.proxy.port:}") String proxyPort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
    }

    @Bean
    CloseableHttpClient httpClient(SslBundles sslBundles) {
        PoolingHttpClientConnectionManagerBuilder cmBuilder =
                PoolingHttpClientConnectionManagerBuilder.create();

        try {
            // Before deploying to production, set Swift Root CA to create a SSLContext with Swift Root CA
            SslBundle clientSslBundle = sslBundles.getBundle("client");
            final SSLContext sslContext = clientSslBundle.createSslContext();
            final SSLConnectionSocketFactory sslSocketFactory =
                    SSLConnectionSocketFactoryBuilder.create().setSslContext(sslContext).build();
            cmBuilder.setSSLSocketFactory(sslSocketFactory);
        } catch (NoSuchSslBundleException e) {
            LOG.warn(
                    "Client SSL configuration not found, using default SSL context");
        }

        HttpClientBuilder httpClientBuilder = HttpClients.custom().setConnectionManager(cmBuilder.build());

        if (!proxyHost.isEmpty() && !proxyPort.isEmpty()) {
            LOG.info("Using proxy {}:{} for HTTP client", proxyHost, proxyPort);
            HttpHost proxy = new HttpHost(proxyHost, parseInt(proxyPort));
            DefaultProxyRoutePlanner proxyRoutePlanner = new DefaultProxyRoutePlanner(proxy);
            httpClientBuilder.setRoutePlanner(proxyRoutePlanner);
        }

        return httpClientBuilder.build();
    }
}
