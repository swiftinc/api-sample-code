package com.swift.apidev.swiftref.filter;

import java.io.IOException;
import java.util.Collections;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class JsonMediaTypeFilter implements ClientRequestFilter {

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        // OpenAPI spec file contains both 'application/json' and 'application/xml' media types.
        // Override 'Accept' header to only accept 'application/json' media type
        requestContext.getHeaders().replace("Accept", Collections.singletonList("application/json"));
    }
    
}
