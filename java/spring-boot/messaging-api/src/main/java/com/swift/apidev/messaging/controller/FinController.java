package com.swift.apidev.messaging.controller;

import com.swift.apidev.messaging.oas.api.FinApi;
import com.swift.apidev.messaging.oas.ApiClient;
import com.swift.apidev.messaging.oas.model.*;
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

@RestController
@RequestMapping("/fin")
public class FinController {

    final FinApi finApi;

    FinController(RestTemplate restTemplate, @Value("${swift.endpoint}") String basePath) {
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(basePath);
        this.finApi = new FinApi(apiClient);
    }

    @Operation(summary = "Download FIN message",
            parameters = {@Parameter(name = "distributionId",
                    content = {@Content(examples = {@ExampleObject(value = "44984189500")})})})
    @GetMapping(value = "/message/{distributionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    FinMessageDownloadResponse downloadFinMessage(@PathVariable Long distributionId) {
        return finApi.downloadFinMessage(distributionId);
    }

    @Operation(summary = "Download FIN transmission report",
            parameters = {@Parameter(name = "distributionId",
                    content = {@Content(examples = {@ExampleObject(value = "44984189500")})})})
    @GetMapping(value = "/transmission-report/{distributionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    FinTransmissionReportDownloadResponse downloadFinTransmissionReport(@PathVariable Long distributionId) {
        return finApi.downloadFinTransmissionReport(distributionId);
    }

    @Operation(summary = "Send FIN message",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = {@Content(examples = {@ExampleObject(
                            value = "{ \"sender_reference\": \"1234\", \"message_type\": \"fin.999\", \"sender\": \"ABCD1234XXXX\", \"receiver\": \"ABCD1234XXXX\", \"payload\": \"DQo6MjA6MTIzNA0KOjc5OlRlc3Q=\", \"network_info\": { \"network_priority\": \"Normal\", \"uetr\": \"099d4eb3-3d68-45fa-9afd-30b19af48728\", \"delivery_monitoring\": \"NonDelivery\", \"possible_duplicate\": false }}")})}))
    @PostMapping(value = "/send", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    SendMessageResponse sendFinMessage(@RequestBody FinMessageEmission finMessageEmission) {
        // Signature is calculated during the encoding by the interceptor
        return finApi.sendFinMessage("", finMessageEmission);
    }

    @ExceptionHandler(value = HttpClientErrorException.class)
    ResponseEntity<String> handleException(HttpClientErrorException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON).body(exception.getResponseBodyAsString());
    }
}
