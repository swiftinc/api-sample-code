package com.swift.apidev.preval;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import com.swift.apidev.preval.oas.model.AccountValidationCheckContext1Code;
import com.swift.apidev.preval.oas.model.AccountVerificationRequest;
import com.swift.apidev.preval.oas.model.AccountVerificationResponse1;
import com.swift.apidev.preval.oas.model.FinancialInstitutionIdentification20;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PrevalControllerIT {

  @Autowired
  TestRestTemplate restTemplate;

  @Test
  void testVerifyBeneficiaryAccount() {
    var accountVerificationRequest = new AccountVerificationRequest();
    accountVerificationRequest.setCorrelationIdentifier("112211221122");
    accountVerificationRequest.setContext(AccountValidationCheckContext1Code.BENR);
    accountVerificationRequest.setUetr("97ed4827-7b6f-4491-a06f-b548d5a7512d");
    accountVerificationRequest.setCreditorAccount("7892368367");
    accountVerificationRequest.setCreditorName("DEF Electronics");
    var financialInstitutionIdentification20 = new FinancialInstitutionIdentification20();
    financialInstitutionIdentification20.setBicfi("AAAAUS2L");
    accountVerificationRequest.setCreditorAgent(financialInstitutionIdentification20);

    ResponseEntity<AccountVerificationResponse1> response =
        restTemplate.postForEntity("/preval/verifyBeneficiaryAccount", accountVerificationRequest,
            AccountVerificationResponse1.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.hasBody());
  }
}
