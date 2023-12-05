package com.swift.apidev.messaging.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

import com.swift.apidev.messaging.cct.oas.ApiClient;
import com.swift.apidev.messaging.cct.oas.api.StatusConfirmationsApi;
import com.swift.apidev.messaging.cct.oas.model.PaymentStatusRequest2;

@RestController
@RequestMapping("/gpi")
public class CustomerCreditTransferController {

    final StatusConfirmationsApi statusConfirmationsApi;

    CustomerCreditTransferController(RestTemplate restTemplate,
            @Value("${swift.endpoint-cct}") String basePath) {
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(basePath);
        this.statusConfirmationsApi = new StatusConfirmationsApi(apiClient);
    }

    @Operation(summary = "Status confirmations", parameters = {
            @Parameter(name = "uetr", content = { @Content(examples = {
                    @ExampleObject(value = "97ed4827-7b6f-4491-a06f-b548d5a7512d")})})}, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
                            @Content(examples = {
                                    @ExampleObject(value = """
                                                    {
                                                        "from": "SWHQBEBBXXX",
                                                        "transaction_status": "ACSP",
                                                        "transaction_status_date": "2021-11-25T14:50:00.000Z",
                                                        "transaction_status_reason": "G000",
                                                        "tracker_informing_party": "SWHQBEBBXXX",
                                                        "instruction_identification": "567",
                                                        "service_level": "G001",
                                                        "payment_scenario": "CCTR",
                                                        "settlement_method": "CLRG",
                                                        "clearing_system": "FDW",
                                                        "instructed_agent": {
                                                          "clearing_system_member_identification": {
                                                            "clearing_system_identification": "USABA",
                                                            "member_identification": "'123456789'"
                                                          }
                                                        },
                                                        "interbank_settlement_amount": {
                                                          "currency": "USD",
                                                          "amount": "1000"
                                                        },
                                                        "charge_bearer": "SHAR"
                                                      }
                                            """) }) }))

    @PutMapping(value = "/{uetr}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    void distributions(@PathVariable String uetr, @RequestBody PaymentStatusRequest2 paymentStatusRequest) {
        statusConfirmationsApi.statusConfirmations(uetr, paymentStatusRequest);
    }

    @ExceptionHandler(value = HttpClientErrorException.class)
    ResponseEntity<String> handleException(HttpClientErrorException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON).body(exception.getResponseBodyAsString());
    }
}
