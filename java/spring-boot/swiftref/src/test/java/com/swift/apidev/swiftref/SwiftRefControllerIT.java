package com.swift.apidev.swiftref;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import com.swift.apidev.swiftref.oas.model.BicIsoDetails;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SwiftRefControllerIT {

  @Autowired
  TestRestTemplate restTemplate;

  @Test
  void testBicDetails() {
    ResponseEntity<BicIsoDetails> response = restTemplate.getForEntity("/swiftref/bic/swhqbebb",
        BicIsoDetails.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.hasBody());
  }

  @Test
  void testIbanDetails() {
    ResponseEntity<BicIsoDetails> response = restTemplate.getForEntity("/swiftref/iban/IT60X0542811101000000123456",
        BicIsoDetails.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.hasBody());
  }
}
