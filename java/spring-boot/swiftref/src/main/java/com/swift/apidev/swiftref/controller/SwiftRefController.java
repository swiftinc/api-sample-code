package com.swift.apidev.swiftref.controller;

import com.swift.apidev.swiftref.oas.ApiClient;
import com.swift.apidev.swiftref.oas.api.BicsApi;
import com.swift.apidev.swiftref.oas.api.IbansApi;
import com.swift.apidev.swiftref.oas.model.BicIsoDetails;
import com.swift.apidev.swiftref.oas.model.IbanDetailsWithIsoFlag;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/swiftref")
public class SwiftRefController {

    final BicsApi bicsApi;
    final IbansApi ibansApi;

    SwiftRefController(RestTemplate restTemplate, @Value("${swift.endpoint}") String basePath) {
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(basePath);
        this.bicsApi = new BicsApi(apiClient);
        this.ibansApi = new IbansApi(apiClient);
    }

    @Operation(summary = "BIC details", parameters = { @Parameter(name = "bic", content = {
            @Content(examples = { @ExampleObject(value = "swhqbebb") }) }) })
    @GetMapping(value = "/bic/{bic}", produces = MediaType.APPLICATION_JSON_VALUE)
    BicIsoDetails bicDetails(@PathVariable("bic") String bic) {
        return bicsApi.getBicDetails(bic);
    }

    @Operation(summary = "IBAN details", parameters = { @Parameter(name = "iban", content = {
            @Content(examples = { @ExampleObject(value = "IT60X0542811101000000123456") }) }) })
    @GetMapping(value = "/iban/{iban}", produces = MediaType.APPLICATION_JSON_VALUE)
    IbanDetailsWithIsoFlag ibanDetails(@PathVariable("iban") String iban) {
        return ibansApi.getIbanDetails(iban);
    }

    @ExceptionHandler(value = HttpClientErrorException.class)
    ResponseEntity<String> handleException(HttpClientErrorException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON).body(exception.getResponseBodyAsString());
    }
}
