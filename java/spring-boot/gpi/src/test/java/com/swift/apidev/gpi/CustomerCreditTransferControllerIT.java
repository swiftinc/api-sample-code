package com.swift.apidev.gpi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CustomerCreditTransferControllerIT {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void testStatusUpdateRequest() {
        final String json = """
                  {
                        "from": "BICAXXXXXXX",
                        "transaction_status": "ACSP",
                        "transaction_status_date": "2021-11-25T14:33:00.000Z",
                        "transaction_status_reason": "G000",
                        "tracker_informing_party": "BICAXXXXXXX",
                        "instruction_identification": " 123",
                        "service_level": "G001",
                        "payment_scenario": "CCTR",
                        "settlement_method": "INDA",
                        "instructed_agent": {
                          "bicfi": "SWHQBEBB"
                        },
                        "interbank_settlement_amount": {
                          "currency": "EUR",
                          "amount": "990"
                        },
                        "charge_bearer": "CRED",
                        "charges_information": [
                          {
                            "amount": {
                              "currency": "EUR",
                              "amount": "10"
                            },
                            "agent": {
                              "bicfi": "BICAXXXXXXX"
                            }
                          }
                        ]
                      }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<String> entity = new HttpEntity<>(json, headers);
        ResponseEntity<Void> response = restTemplate.exchange("/gpi/b86979f0-ad83-45a1-bd1d-8696f6010b0d/status",
                HttpMethod.PUT, entity, void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
