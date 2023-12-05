package com.swift.apidev.preval;

import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import com.swift.apidev.preval.resource.PreValResource;

import static io.restassured.RestAssured.given;
import java.util.HashMap;
import java.util.Map;

@QuarkusIntegrationTest
@TestHTTPEndpoint(PreValResource.class)
public class PreValResourceIT {

    @Test
    void testVerifyBeneficiaryAccount() {
        Map<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("correlation_identifier", "112211221122");
        requestParams.put("context", "BENR");
        requestParams.put("uetr", "97ed4827-7b6f-4491-a06f-b548d5a7512d");
        requestParams.put("creditor_account", "7892368367");
        requestParams.put("creditor_name", "DEF Electronics");
        requestParams.put("creditor_agent", Map.of("bicfi", "AAAAUS2L"));
        given().contentType(ContentType.JSON).accept(ContentType.JSON).body(requestParams).when()
                .post("/verifyBeneficiaryAccount").then().statusCode(200);
    }
}
