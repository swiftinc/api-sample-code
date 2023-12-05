package com.swift.apidev.swiftref.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.swift.apidev.swiftref.oas.api.ApiException;
import com.swift.apidev.swiftref.oas.api.BicsApi;
import com.swift.apidev.swiftref.oas.api.IbansApi;
import com.swift.apidev.swiftref.oas.model.BicIsoDetails;
import com.swift.apidev.swiftref.oas.model.IbanDetailsWithIsoFlag;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/swiftref")
public class SwiftRefResource {

    @RestClient
    BicsApi bicsApi;

    @RestClient
    IbansApi ibansApi;

    @GET
    @Path("/bic/{bic}")
    @Operation(summary = "BIC details")
    @Produces(MediaType.APPLICATION_JSON)
    public Response bicDetails(
            @Parameter(required = true, example = "swhqbebb", schema = @Schema(type = SchemaType.STRING)) @PathParam("bic") String bic)
            throws ApiException {
        BicIsoDetails bicDetails = bicsApi.getBicDetails(bic);
        return Response.ok().entity(bicDetails).build();
    }

    @GET
    @Path("/iban/{iban}")
    @Operation(summary = "IBAN details")
    @Produces(MediaType.APPLICATION_JSON)
    public Response ibanDetails(
            @Parameter(required = true, example = "IT60X0542811101000000123456", schema = @Schema(type = SchemaType.STRING)) @PathParam("iban") String iban)
            throws ApiException {
        IbanDetailsWithIsoFlag ibanDetails = ibansApi.getIbanDetails(iban);
        return Response.ok().entity(ibanDetails).build();
    }
}
