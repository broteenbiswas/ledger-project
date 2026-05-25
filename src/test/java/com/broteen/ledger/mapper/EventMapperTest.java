package com.broteen.ledger.mapper;

import com.broteen.ledger.domain.entity.Event;
import com.broteen.ledger.domain.enums.EventType;
import com.broteen.ledger.dto.request.EventRequest;
import com.broteen.ledger.dto.response.EventResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventMapperTest {

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private EventMapper eventMapper;

    @Test
    @DisplayName("serializeMetadata: Jackson exception results in null metadata on entity")
    void toEntity_metadataSerializationFailure_storesNull() throws Exception {
        when(objectMapper.writeValueAsString(any()))
                .thenThrow(new JsonProcessingException("forced failure") {});

        EventRequest request = new EventRequest();
        request.setEventId("evt-001");
        request.setAccountId("acct-123");
        request.setType(EventType.CREDIT);
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setEventTimestamp(Instant.now());
        request.setMetadata(Map.of("key", "value"));

        Event result = eventMapper.toEntity(request);

        assertNull(result.getMetadata());
    }

    @Test
    @DisplayName("deserializeMetadata: Jackson exception results in null metadata on response")
    @SuppressWarnings("unchecked")
    void toResponse_metadataDeserializationFailure_returnsNull() throws Exception {
        when(objectMapper.readValue(any(String.class), any(TypeReference.class)))
                .thenThrow(new JsonProcessingException("forced failure") {});

        Event event = new Event();
        event.setEventId("evt-001");
        event.setAccountId("acct-123");
        event.setType(EventType.CREDIT);
        event.setAmount(new BigDecimal("100.00"));
        event.setCurrency("USD");
        event.setEventTimestamp(Instant.now());
        event.setMetadata("{\"key\":\"value\"}");
        event.setReceivedAt(Instant.now());

        EventResponse result = eventMapper.toResponse(event);

        assertNull(result.getMetadata());
    }
}
