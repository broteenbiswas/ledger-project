package com.broteen.ledger.controller;

import com.broteen.ledger.domain.enums.EventType;
import com.broteen.ledger.dto.request.EventRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private EventRequest baseRequest;

    @BeforeEach
    void setUp() {
        baseRequest = buildRequest("evt-001", "acct-123", EventType.CREDIT, "150.00", "2026-05-15T14:02:11Z");
        baseRequest.setMetadata(Map.of("source", "mainframe-batch", "batchId", "B-9042"));
    }

    @Test
    @DisplayName("POST /events: valid request returns 201 with event body")
    void submitEvent_validRequest_returns201() throws Exception {
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventId").value("evt-001"))
                .andExpect(jsonPath("$.accountId").value("acct-123"))
                .andExpect(jsonPath("$.type").value("CREDIT"))
                .andExpect(jsonPath("$.amount").value(150.00))
                .andExpect(jsonPath("$.currency").value("USD"))
                .andExpect(jsonPath("$.receivedAt").exists());
    }

    @Test
    @DisplayName("POST /events: duplicate eventId returns 200 with original event (idempotency)")
    void submitEvent_duplicateEventId_returns200WithOriginal() throws Exception {
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value("evt-001"));
    }

    @Test
    @DisplayName("POST /events: missing eventId returns 400 with field details")
    void submitEvent_missingEventId_returns400() throws Exception {
        baseRequest.setEventId(null);

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    @DisplayName("POST /events: zero amount returns 400")
    void submitEvent_zeroAmount_returns400() throws Exception {
        baseRequest.setAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /events: negative amount returns 400")
    void submitEvent_negativeAmount_returns400() throws Exception {
        baseRequest.setAmount(new BigDecimal("-50.00"));

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /events: invalid type value returns 400")
    void submitEvent_invalidType_returns400() throws Exception {
        String badBody = objectMapper.writeValueAsString(baseRequest)
                .replace("\"CREDIT\"", "\"TRANSFER\"");

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /events/{id}: existing event returns 200")
    void getEventById_exists_returns200() throws Exception {
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(baseRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/events/evt-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value("evt-001"));
    }

    @Test
    @DisplayName("GET /events/{id}: unknown id returns 404")
    void getEventById_notFound_returns404() throws Exception {
        mockMvc.perform(get("/events/does-not-exist"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("GET /events: missing account param returns 400")
    void getEventsByAccount_missingAccountParam_returns400() throws Exception {
        mockMvc.perform(get("/events"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Required parameter missing: account"));
    }

    @Test
    @DisplayName("GET /events?account=: events returned in chronological order regardless of submission order")
    void getEventsByAccount_outOfOrderSubmission_returnsChronologicalOrder() throws Exception {
        EventRequest later  = buildRequest("evt-002", "acct-456", EventType.CREDIT, "100.00", "2026-05-15T16:00:00Z");
        EventRequest earlier = buildRequest("evt-001", "acct-456", EventType.DEBIT,  "50.00", "2026-05-15T10:00:00Z");

        mockMvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(later))).andExpect(status().isCreated());
        mockMvc.perform(post("/events").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(earlier))).andExpect(status().isCreated());

        mockMvc.perform(get("/events").param("account", "acct-456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].eventId").value("evt-001"))
                .andExpect(jsonPath("$.content[1].eventId").value("evt-002"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.last").value(true));
    }

    private EventRequest buildRequest(String eventId, String accountId, EventType type,
                                       String amount, String timestamp) {
        EventRequest req = new EventRequest();
        req.setEventId(eventId);
        req.setAccountId(accountId);
        req.setType(type);
        req.setAmount(new BigDecimal(amount));
        req.setCurrency("USD");
        req.setEventTimestamp(Instant.parse(timestamp));
        return req;
    }
}
