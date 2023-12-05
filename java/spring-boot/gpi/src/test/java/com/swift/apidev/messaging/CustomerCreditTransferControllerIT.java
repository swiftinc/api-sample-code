package com.swift.apidev.messaging;

import com.swift.apidev.messaging.cct.oas.model.ActiveCurrencyAndAmount;
import com.swift.apidev.messaging.cct.oas.model.BusinessService12Code;
import com.swift.apidev.messaging.cct.oas.model.ChargeBearerType3Code;
import com.swift.apidev.messaging.cct.oas.model.ClearingSystemMemberIdentification3;
import com.swift.apidev.messaging.cct.oas.model.FinancialInstitutionIdentification11Choice;
import com.swift.apidev.messaging.cct.oas.model.PaymentScenario6Code;
import com.swift.apidev.messaging.cct.oas.model.PaymentStatusReason10Code;
import com.swift.apidev.messaging.cct.oas.model.PaymentStatusRequest2;
import com.swift.apidev.messaging.cct.oas.model.SettlementMethod1Code;
import com.swift.apidev.messaging.cct.oas.model.TransactionIndividualStatus5Code;

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
        PaymentStatusRequest2 paymentStatusRequest = new PaymentStatusRequest2();
        paymentStatusRequest.setFrom("SWHQBEBBXXX");
        paymentStatusRequest.setTransactionStatus(TransactionIndividualStatus5Code.ACSP);
        paymentStatusRequest.setTransactionStatusDate("2021-11-25T14:50:00.000Z");
        paymentStatusRequest.setTransactionStatusReason(PaymentStatusReason10Code.G000);
        paymentStatusRequest.setTrackerInformingParty("SWHQBEBBXXX");
        paymentStatusRequest.setInstructionIdentification("567");
        paymentStatusRequest.setServiceLevel(BusinessService12Code.G001);
        paymentStatusRequest.setPaymentScenario(PaymentScenario6Code.CCTR);
        paymentStatusRequest.setSettlementMethod(SettlementMethod1Code.CLRG);
        paymentStatusRequest.setClearingSystem("FDW");
        ClearingSystemMemberIdentification3 clearingSystemMemberIdentification = new ClearingSystemMemberIdentification3();
        clearingSystemMemberIdentification.setClearingSystemIdentification("USABA");
        clearingSystemMemberIdentification.setMemberIdentification("'123456789'");
        FinancialInstitutionIdentification11Choice instructedAgent = new FinancialInstitutionIdentification11Choice();
        instructedAgent.setClearingSystemMemberIdentification(clearingSystemMemberIdentification);
        paymentStatusRequest.setInstructedAgent(instructedAgent);
        ActiveCurrencyAndAmount interbankSettlementAmount = new ActiveCurrencyAndAmount();
        interbankSettlementAmount.setCurrency("USD");
        interbankSettlementAmount.setAmount("1000");
        paymentStatusRequest.setInterbankSettlementAmount(interbankSettlementAmount);
        paymentStatusRequest.setChargeBearer(ChargeBearerType3Code.SHAR);

        HttpEntity<PaymentStatusRequest2> entity = new HttpEntity<PaymentStatusRequest2>(paymentStatusRequest);
        ResponseEntity<Void> response = restTemplate.exchange("/gpi/97ed4827-7b6f-4491-a06f-b548d5a7512d/status",
                HttpMethod.PUT, entity, void.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
