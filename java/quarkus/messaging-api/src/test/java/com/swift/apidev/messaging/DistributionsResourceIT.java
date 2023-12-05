package com.swift.apidev.messaging;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;
import com.swift.apidev.messaging.resource.DistributionsResource;
import static io.restassured.RestAssured.given;

@QuarkusIntegrationTest
@TestHTTPEndpoint(DistributionsResource.class)
class DistributionsResourceIT {

    @Test
    void testDistributionList() {
        given().when().get("/list").then().statusCode(200);
    }
}
