package com.swift.apidev.gpi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import com.swift.apidev.gpi.transactiondetails.oas.model.PaymentTransaction149;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionDetailsControllerIT {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void testTransactionDetails() {
        ResponseEntity<PaymentTransaction149> response = restTemplate.getForEntity(
                "/gpi/87d77106-55e7-4efc-a166-db91ce72303e/transactions",
                PaymentTransaction149.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
    }
}
