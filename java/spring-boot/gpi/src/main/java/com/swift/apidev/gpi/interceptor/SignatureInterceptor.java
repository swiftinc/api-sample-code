package com.swift.apidev.gpi.interceptor;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import com.swift.apidev.gpi.jwt.JwtOperations;
import java.io.IOException;
import java.util.Collections;

@Component
public class SignatureInterceptor implements ClientHttpRequestInterceptor {

    private static final String SIGNATURE_HEADER = "X-SWIFT-Signature";

    private final JwtOperations jwtOperations;

    SignatureInterceptor(JwtOperations jwtOperations) {
        this.jwtOperations = jwtOperations;
    }

    @Override
    public @NonNull ClientHttpResponse intercept(@NonNull HttpRequest request, @NonNull byte[] body,
            @NonNull ClientHttpRequestExecution execution)
            throws IOException {
        // Calculate the signature for requests that contain the signature header
        if (request.getHeaders().containsKey(SIGNATURE_HEADER)) {
            String signature = jwtOperations.generateSignature(request.getURI().toString(), body);
            request.getHeaders().replace(SIGNATURE_HEADER, Collections.singletonList(signature));
        }
        return execution.execute(request, body);
    }
}
