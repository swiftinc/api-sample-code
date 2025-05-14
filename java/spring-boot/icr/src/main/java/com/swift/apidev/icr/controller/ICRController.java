package com.swift.apidev.icr.controller;

import com.swift.apidev.icr.oas.ApiClient;
import com.swift.apidev.icr.oas.api.AccountInformationApi;
import com.swift.apidev.icr.oas.model.CashAccount41;
import com.swift.apidev.icr.oas.model.GetAccounts200Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/icr")
public class ICRController {

    private static final Logger LOG = LoggerFactory.getLogger(ICRController.class);


    final AccountInformationApi accountInformationApi;

    ICRController(RestTemplate restTemplate, @Value("${swift.endpoint}") String basePath) {
        ApiClient apiClient = new ApiClient(restTemplate);
        apiClient.setBasePath(basePath);
        this.accountInformationApi = new AccountInformationApi(apiClient);
    }

    @Operation(summary = "Retrieve accounts and balances")
    @GetMapping(value = "/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
    GetAccounts200Response getAccountsBalances() {
        return accountInformationApi.getAccounts("DEUTDEFF", null, null, null, null, null, null, null);
    }

    @Operation(summary = "Retrieve balances for an account", parameters = { @Parameter(name = "account-identification", content = {
            @Content(examples = { @ExampleObject(value = "DE27212318890001010223463465") }) }) })
    @GetMapping(value = "/accounts/{account-identification}", produces = MediaType.APPLICATION_JSON_VALUE)
    CashAccount41 getAccountBalances(@PathVariable("account-identification") String accountIdentification) {
        return accountInformationApi.getAccount("BNKABEBB", accountIdentification, null, null);
    }

    @ExceptionHandler(value = HttpClientErrorException.class)
    ResponseEntity<String> handleException(HttpClientErrorException exception) {
        return ResponseEntity.status(exception.getStatusCode())
                .contentType(MediaType.APPLICATION_JSON).body(exception.getResponseBodyAsString());
    }
}
