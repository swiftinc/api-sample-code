package com.swift.apidev.messaging.controller;

import com.swift.apidev.messaging.oas.api.DistributionsApi;
import com.swift.apidev.messaging.oas.ApiClient;
import com.swift.apidev.messaging.oas.model.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/distributions")
public class DistributionsController {

    final DistributionsApi distributionsApi;

    DistributionsController(RestTemplate restTemplate,
            @Value("${swift.endpoint}") String basePath) {
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(basePath);
        this.distributionsApi = new DistributionsApi(apiClient);
    }

    @Operation(summary = "Get distribution list")
    @GetMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    ListDistributionsResponse distributions() {
        return distributionsApi.listDistributions(100, 0);
    }

    @ExceptionHandler(value = HttpClientErrorException.class)
    ResponseEntity<String> handleException(HttpClientErrorException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON).body(exception.getResponseBodyAsString());
    }
}
