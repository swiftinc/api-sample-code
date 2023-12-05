package com.swift.apidev.messaging.resource;

import com.swift.apidev.messaging.oas.api.ApiException;
import com.swift.apidev.messaging.oas.api.DistributionsApi;
import com.swift.apidev.messaging.oas.model.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/distributions")
public class DistributionsResource {

    @RestClient
    DistributionsApi distributionsApi;

    @GET
    @Path("/list")
    @Operation(summary = "Get distribution list")
    public Response listDistributions() throws ApiException {
        ListDistributionsResponse distributions = distributionsApi.listDistributions(100, 0);
        return Response.ok(distributions).build();
    }
}
