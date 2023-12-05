package com.swift.apidev.preval.controller;

import com.swift.apidev.preval.oas.ApiClient;
import com.swift.apidev.preval.oas.api.PaymentPreValidationApi;
import com.swift.apidev.preval.oas.model.AccountVerificationRequest;
import com.swift.apidev.preval.oas.model.AccountVerificationResponse1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/preval")
public class PrevalController {

    final PaymentPreValidationApi paymentPreValidationApi;

    PrevalController(RestTemplate restTemplate, @Value("${swift.endpoint}") String basePath) {
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(basePath);
        this.paymentPreValidationApi = new PaymentPreValidationApi(apiClient);
    }

    @Operation(summary = "Verify beneficiary account",
            requestBody = @RequestBody(content = {@Content(examples = {@ExampleObject(
                    value = "{ \"correlation_identifier\": \"112211221122\", \"context\": \"BENR\", \"uetr\": \"97ed4827-7b6f-4491-a06f-b548d5a7512d\", \"creditor_account\": \"7892368367\", \"creditor_name\": \"DEF Electronics\", \"creditor_agent\": { \"bicfi\": \"AAAAUS2L\" }}")})}))
    @PostMapping(value = "/verifyBeneficiaryAccount", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    AccountVerificationResponse1 verifyBeneficiaryAccount(
            @org.springframework.web.bind.annotation.RequestBody AccountVerificationRequest accountVerificationRequest) {
        return paymentPreValidationApi.verifyBeneficiaryAccount("swhqbebb", null, null,
                accountVerificationRequest);
    }

    @ExceptionHandler(value = HttpClientErrorException.class)
    ResponseEntity<String> handleException(HttpClientErrorException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON).body(exception.getResponseBodyAsString());
    }
}
