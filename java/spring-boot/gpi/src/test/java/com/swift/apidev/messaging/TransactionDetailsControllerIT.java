package com.swift.apidev.messaging;

import com.swift.apidev.messaging.transactiondetails.oas.model.ReadPaymentTransactionDetailsResponse1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TransactionDetailsControllerIT {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void testTransactionDetails() {
        ResponseEntity<ReadPaymentTransactionDetailsResponse1> response = restTemplate.getForEntity(
                "/gpi/97ed4827-7b6f-4491-a06f-b548d5a7512d/transactions",
                ReadPaymentTransactionDetailsResponse1.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
    }
}
