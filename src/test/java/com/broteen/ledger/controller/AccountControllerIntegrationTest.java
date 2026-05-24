package com.broteen.ledger.controller;

import com.broteen.ledger.domain.enums.EventType;
import com.broteen.ledger.dto.request.EventRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AccountControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /accounts/{id}/balance: mixed events produce correct net balance")
    void getBalance_mixedEvents_correctNetBalance() throws Exception {
        submit("evt-c1", "acct-001", EventType.CREDIT, "500.00", "2026-05-15T10:00:00Z");
        submit("evt-d1", "acct-001", EventType.DEBIT,  "200.00", "2026-05-15T12:00:00Z");
        submit("evt-c2", "acct-001", EventType.CREDIT, "100.00", "2026-05-15T14:00:00Z");

        mockMvc.perform(get("/accounts/acct-001/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value("acct-001"))
                .andExpect(jsonPath("$.balance").value(400.0))
                .andExpect(jsonPath("$.eventCount").value(3))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    @DisplayName("GET /accounts/{id}/balance: unknown account returns 404")
    void getBalance_unknownAccount_returns404() throws Exception {
        mockMvc.perform(get("/accounts/no-such-account/balance"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /accounts/{id}/balance: duplicate submissions do not double-count the balance")
    void getBalance_duplicateEvent_balanceUnaffected() throws Exception {
        submit("evt-001", "acct-002", EventType.CREDIT, "500.00", "2026-05-15T10:00:00Z");
        submit("evt-001", "acct-002", EventType.CREDIT, "500.00", "2026-05-15T10:00:00Z");

        mockMvc.perform(get("/accounts/acct-002/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(500.0))
                .andExpect(jsonPath("$.eventCount").value(1));
    }

    @Test
    @DisplayName("GET /accounts/{id}/balance: out-of-order event arrival still yields correct balance")
    void getBalance_outOfOrderArrival_balanceCorrect() throws Exception {
        submit("evt-late",  "acct-003", EventType.DEBIT,  "100.00", "2026-05-15T16:00:00Z");
        submit("evt-early", "acct-003", EventType.CREDIT, "300.00", "2026-05-15T10:00:00Z");

        mockMvc.perform(get("/accounts/acct-003/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(200.0));
    }

    @Test
    @DisplayName("GET /accounts/{id}/balance: only debits produce negative balance")
    void getBalance_onlyDebits_negativeBalance() throws Exception {
        submit("evt-d1", "acct-004", EventType.DEBIT, "300.00", "2026-05-15T10:00:00Z");

        mockMvc.perform(get("/accounts/acct-004/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(-300.0));
    }

    private void submit(String eventId, String accountId, EventType type,
                        String amount, String timestamp) throws Exception {
        EventRequest req = new EventRequest();
        req.setEventId(eventId);
        req.setAccountId(accountId);
        req.setType(type);
        req.setAmount(new BigDecimal(amount));
        req.setCurrency("USD");
        req.setEventTimestamp(Instant.parse(timestamp));

        mockMvc.perform(post("/events")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)));
    }
}
