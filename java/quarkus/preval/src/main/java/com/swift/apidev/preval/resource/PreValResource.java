package com.swift.apidev.preval.resource;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import com.swift.apidev.preval.oas.api.ApiException;
import com.swift.apidev.preval.oas.api.PaymentPreValidationApi;
import com.swift.apidev.preval.oas.model.AccountVerificationRequest;
import com.swift.apidev.preval.oas.model.AccountVerificationResponse1;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/preval")
public class PreValResource {

    @RestClient
    PaymentPreValidationApi preValidationApi;

    @POST
    @Path("/verifyBeneficiaryAccount")
    @Operation(summary = "Verify beneficiary account")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response verifyBeneficiaryAccount(
            @RequestBody(required = true,
                    content = {@Content(
                            schema = @Schema(implementation = AccountVerificationRequest.class),
                            example = "{ \"correlation_identifier\": \"112211221122\", \"context\": \"BENR\", \"uetr\": \"97ed4827-7b6f-4491-a06f-b548d5a7512d\", \"creditor_account\": \"7892368367\", \"creditor_name\": \"DEF Electronics\", \"creditor_agent\": { \"bicfi\": \"AAAAUS2L\" }}")}) AccountVerificationRequest accountVerificationRequest)
            throws ApiException {
        AccountVerificationResponse1 finMessage = preValidationApi
                .verifyBeneficiaryAccount("swhqbebb", null, null, accountVerificationRequest);
        return Response.ok().entity(finMessage).build();
    }
}
