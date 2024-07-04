package com.swift.apidev.messaging.resource;

import com.swift.apidev.messaging.oas.api.ApiException;
import com.swift.apidev.messaging.oas.api.FinApi;
import com.swift.apidev.messaging.oas.model.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path("/fin")
public class FinResource {

    @RestClient
    FinApi finApi;

    @GET
    @Path("/message/{distributionId}")
    @Operation(summary = "Download FIN message")
    @Produces(MediaType.APPLICATION_JSON)
    public Response downloadFinMessage(
            @Parameter(required = true, example = "44984189500", schema = @Schema(type = SchemaType.STRING)) @PathParam("distributionId") Long distributionId)
            throws ApiException {
        FinMessageDownloadResponse finMessageDownloadResponse = finApi.downloadFinMessage(distributionId);
        return Response.ok(finMessageDownloadResponse).build();
    }

    @GET
    @Path("/transmission-report/{distributionId}")
    @Operation(summary = "Download FIN message")
    @Produces(MediaType.APPLICATION_JSON)
    public Response downloadFinTransmissionReport(
            @Parameter(required = true, example = "44984189500", schema = @Schema(type = SchemaType.STRING)) @PathParam("distributionId") Long distributionId)
            throws ApiException {
        FinTransmissionReportDownloadResponse response = finApi.downloadFinTransmissionReport(distributionId);
        return Response.ok(response).build();
    }

    @POST
    @Path("/send")
    @Operation(summary = "Send FIN message")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response findSend(@RequestBody(required = true, content = {
            @Content(schema = @Schema(implementation = FinMessageEmission.class), example = "{\"sender_reference\": \"1234\", \"message_type\": \"fin.999\", \"sender\": \"ABCD1234XXXX\", \"receiver\": \"ABCD1234XXXX\", \"payload\": \"DQo6MjA6MTIzNA0KOjc5OlRlc3Q=\", \"network_info\": { \"network_priority\": \"Normal\", \"uetr\": \"099d4eb3-3d68-45fa-9afd-30b19af48728\", \"delivery_monitoring\": \"NonDelivery\", \"possible_duplicate\": false }}") }) FinMessageEmission finMessageEmission)
            throws ApiException {
        SendMessageResponse finMessage = finApi.sendFinMessage("", finMessageEmission);
        return Response.ok().entity(finMessage).build();
    }
}
