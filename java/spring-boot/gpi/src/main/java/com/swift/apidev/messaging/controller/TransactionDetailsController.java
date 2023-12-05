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

import com.swift.apidev.messaging.transactiondetails.oas.ApiClient;
import com.swift.apidev.messaging.transactiondetails.oas.api.GetPaymentTransactionDetailsApi;
import com.swift.apidev.messaging.transactiondetails.oas.model.ReadPaymentTransactionDetailsResponse1;

@RestController
@RequestMapping("/gpi")
public class TransactionDetailsController {

    final GetPaymentTransactionDetailsApi getPaymentTransactionDetailsApi;

    TransactionDetailsController(RestTemplate restTemplate,
            @Value("${swift.endpoint}") String basePath) {
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(basePath);
        this.getPaymentTransactionDetailsApi = new GetPaymentTransactionDetailsApi(apiClient);
    }

    @Operation(summary = "Get payment transaction details", parameters = {
        @Parameter(name = "uetr", content = { @Content(examples = {
                @ExampleObject(value = "97ed4827-7b6f-4491-a06f-b548d5a7512d") })})})
    @GetMapping(value = "/{uetr}/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
    ReadPaymentTransactionDetailsResponse1 distributions(@PathVariable String uetr) {
        return getPaymentTransactionDetailsApi.getPaymentTransactionDetails(uetr);
    }

    @ExceptionHandler(value = HttpClientErrorException.class)
    ResponseEntity<String> handleException(HttpClientErrorException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON).body(exception.getResponseBodyAsString());
    }
}
