package com.swift.apidev.icr;

import com.swift.apidev.icr.oas.model.GetAccounts200Response;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ICRControllerIT {

  @Autowired
  TestRestTemplate restTemplate;

  @Test
  void testGetAccountBalances() {

    HttpHeaders headers = new HttpHeaders();
    headers.set("X-BIC", "BNKABEBB");
    HttpEntity<String> request = new HttpEntity<>(headers);
    ResponseEntity<GetAccounts200Response> response = restTemplate.exchange(
            "/icr/accounts/DE27212318890001010223463465",
            HttpMethod.GET,
            request,
            GetAccounts200Response.class
    );

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.hasBody());
  }

}
