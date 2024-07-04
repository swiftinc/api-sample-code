package com.swift.apidev.messaging;

import com.swift.apidev.messaging.oas.model.FinMessageDownloadResponse;
import com.swift.apidev.messaging.oas.model.FinMessageEmission;
import com.swift.apidev.messaging.oas.model.FinMessageNetworkInfoEmission;
import com.swift.apidev.messaging.oas.model.FinTransmissionReportDownloadResponse;
import com.swift.apidev.messaging.oas.model.SendMessageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Base64;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FinControllerIT {

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void testDownloadFinMessage() {
        ResponseEntity<FinMessageDownloadResponse> response = restTemplate.getForEntity("/fin/message/44984189500",
                FinMessageDownloadResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
    }

    @Test
    void testDownloadFinTransmissionReport() {
        ResponseEntity<FinTransmissionReportDownloadResponse> response = restTemplate.getForEntity("/fin/transmission-report/44984189500",
        FinTransmissionReportDownloadResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
    }

    @Test
    void testSendFinMessage() {
        FinMessageEmission finMessageEmission = new FinMessageEmission();
        finMessageEmission.senderReference("1234");
        finMessageEmission.sender("ABCD1234XXXX");
        finMessageEmission.receiver("ABCD1234XXXX");
        finMessageEmission.setMessageType("fin.999");
        finMessageEmission.setPayload(Base64.getEncoder().encode("\r\n:20:1234\r\n:79:Test".getBytes()));

        FinMessageNetworkInfoEmission networkInfo = new FinMessageNetworkInfoEmission();
        networkInfo.networkPriority(FinMessageNetworkInfoEmission.NetworkPriorityEnum.NORMAL);
        networkInfo.setUetr(UUID.randomUUID());
        networkInfo.deliveryMonitoring(
                FinMessageNetworkInfoEmission.DeliveryMonitoringEnum.NONDELIVERY);
        networkInfo.possibleDuplicate(false);
        finMessageEmission.setNetworkInfo(networkInfo);

        ResponseEntity<SendMessageResponse> response = restTemplate.postForEntity("/fin/send",
                finMessageEmission,
                SendMessageResponse.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.hasBody());
    }
}
