package com.swift.apidev.swiftref;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;

import com.swift.apidev.swiftref.resource.SwiftRefResource;

import static io.restassured.RestAssured.given;

@QuarkusIntegrationTest
@TestHTTPEndpoint(SwiftRefResource.class)
public class SwiftRefResourceIT {

    @Test
    void testBicDetails() {
        given().when().get("/bic/swhqbebb").then().statusCode(200);

    }

    @Test
    void testIbanDetails() {
        given().when().get("/iban/IT60X0542811101000000123456").then().statusCode(200);

    }
}
