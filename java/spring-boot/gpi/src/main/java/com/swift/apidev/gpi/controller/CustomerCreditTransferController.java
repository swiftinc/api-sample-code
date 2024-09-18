package com.swift.apidev.gpi.controller;

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

import com.swift.apidev.gpi.cct.oas.ApiClient;
import com.swift.apidev.gpi.cct.oas.api.StatusConfirmationsApi;
import com.swift.apidev.gpi.cct.oas.model.PaymentStatusRequest9;

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
          @ExampleObject(value = "97ed4827-7b6f-4491-a06f-b548d5a7512d") }) }) }, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = {
              @Content(examples = {
                  @ExampleObject(value = """
                        {
                              "from": "BICAXXXXXXX",
                              "transaction_status": "ACSP",
                              "transaction_status_date": "2021-11-25T14:33:00.000Z",
                              "transaction_status_reason": "G000",
                              "tracker_informing_party": "BICAXXXXXXX",
                              "instruction_identification": " 123",
                              "service_level": "G001",
                              "payment_scenario": "CCTR",
                              "settlement_method": "INDA",
                              "instructed_agent": {
                                "bicfi": "SWHQBEBB"
                              },
                              "interbank_settlement_amount": {
                                "currency": "EUR",
                                "amount": "990"
                              },
                              "charge_bearer": "CRED",
                              "charges_information": [
                                {
                                  "amount": {
                                    "currency": "EUR",
                                    "amount": "10"
                                  },
                                  "agent": {
                                    "bicfi": "BICAXXXXXXX"
                                  }
                                }
                              ]
                            }
                      """) }) }))

  @PutMapping(value = "/{uetr}/status", produces = MediaType.APPLICATION_JSON_VALUE)
  void distributions(@PathVariable String uetr, @RequestBody PaymentStatusRequest9 paymentStatusRequest) {
    statusConfirmationsApi.statusConfirmations(uetr, paymentStatusRequest);
  }

  @ExceptionHandler(value = HttpClientErrorException.class)
  ResponseEntity<String> handleException(HttpClientErrorException exception) {
    return ResponseEntity.status(exception.getStatusCode())
        .contentType(MediaType.APPLICATION_JSON).body(exception.getResponseBodyAsString());
  }
}
