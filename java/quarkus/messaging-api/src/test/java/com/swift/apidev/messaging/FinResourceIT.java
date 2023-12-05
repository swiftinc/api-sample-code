package com.swift.apidev.messaging;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import com.swift.apidev.messaging.resource.FinResource;
import static io.restassured.RestAssured.given;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@QuarkusIntegrationTest
@TestHTTPEndpoint(FinResource.class)
class FinResourceIT {

    @Test
    void testDownloadFinMessage() {
        given().when().get("/44984189500/download").then().statusCode(200);
    }

    @Test
    void testSendFinMessage() {
        Map<String, Object> finMessage = new HashMap<String, Object>();
        finMessage.put("sender_reference", "1234");
        finMessage.put("sender", "ABCD1234XXXX");
        finMessage.put("receiver", "ABCD1234XXXX");
        finMessage.put("message_type", "fin.199");
        finMessage.put("payload",
                Base64.getEncoder().encodeToString(":20:1234\r\n:79:Test".getBytes()));

        Map<String, String> networkInfo = new HashMap<String, String>();
        networkInfo.put("network_priority", "normal");
        networkInfo.put("uetr", UUID.randomUUID().toString());
        networkInfo.put("delivery_monitoring", "NonDelivery");

        given().contentType(ContentType.JSON).accept(ContentType.JSON).body(finMessage).when()
                .post("/send").then().statusCode(200);
    }
}
