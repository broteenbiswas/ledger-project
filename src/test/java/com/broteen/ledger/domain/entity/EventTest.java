package com.broteen.ledger.domain.entity;

import com.broteen.ledger.domain.enums.EventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventTest {

    @Test
    @DisplayName("Event getId/setId round-trip works correctly")
    void idGetterSetter_roundTrip() {
        Event event = new Event();
        event.setId(99L);
        assertEquals(99L, event.getId());
    }

    @Test
    @DisplayName("All Event getters and setters work correctly")
    void allFields_gettersSetters_roundTrip() {
        Event event = new Event();
        Instant now = Instant.now();

        event.setEventId("evt-test");
        event.setAccountId("acct-test");
        event.setType(EventType.DEBIT);
        event.setAmount(new BigDecimal("250.00"));
        event.setCurrency("EUR");
        event.setEventTimestamp(now);
        event.setMetadata("{\"ref\":\"abc\"}");
        event.setReceivedAt(now);

        assertEquals("evt-test", event.getEventId());
        assertEquals("acct-test", event.getAccountId());
        assertEquals(EventType.DEBIT, event.getType());
        assertEquals(new BigDecimal("250.00"), event.getAmount());
        assertEquals("EUR", event.getCurrency());
        assertEquals(now, event.getEventTimestamp());
        assertEquals("{\"ref\":\"abc\"}", event.getMetadata());
        assertEquals(now, event.getReceivedAt());
    }
}
