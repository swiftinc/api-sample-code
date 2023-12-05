package com.swift.apidev.messaging;

import com.swift.apidev.messaging.oas.model.ListDistributionsResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DistributionsControllerIT {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void testDistributionList() {
        ResponseEntity<ListDistributionsResponse> response = restTemplate.getForEntity("/distributions/list",
                ListDistributionsResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
    }
}
