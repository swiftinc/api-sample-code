package com.swift.apidev.messaging.interceptor;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import com.swift.apidev.messaging.jwt.JwtOperations;
import java.io.IOException;

@Component
public class IntegritySignatureInterceptor implements ClientHttpRequestInterceptor {
    
    private static final String INTEGRITY_HEADER = "X-SWIFT-Integrity";

    private final JwtOperations jwtOperations;

    public IntegritySignatureInterceptor(JwtOperations jwtOperations) {
        this.jwtOperations = jwtOperations;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        ClientHttpResponse response =  execution.execute(request, body);

        // Verify the signature for responses that contain the integrity header
        if (response.getHeaders().containsKey(INTEGRITY_HEADER)) {
            byte[] responseBody = response.getBody().readAllBytes();
            String signature = response.getHeaders().getFirst(INTEGRITY_HEADER);
            jwtOperations.verifySignature(signature, responseBody);
        }

        return response;
    }
}
