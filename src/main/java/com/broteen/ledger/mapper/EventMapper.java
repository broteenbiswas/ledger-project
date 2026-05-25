package com.broteen.ledger.mapper;

import com.broteen.ledger.domain.entity.Event;
import com.broteen.ledger.dto.request.EventRequest;
import com.broteen.ledger.dto.response.EventResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class EventMapper {

    private static final Logger log = LoggerFactory.getLogger(EventMapper.class);

    private final ObjectMapper objectMapper;

    public EventMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Event toEntity(EventRequest request) {
        Event event = new Event();
        event.setEventId(request.getEventId());
        event.setAccountId(request.getAccountId());
        event.setType(request.getType());
        event.setAmount(request.getAmount());
        event.setCurrency(request.getCurrency());
        event.setEventTimestamp(request.getEventTimestamp());
        event.setMetadata(serializeMetadata(request.getMetadata()));
        event.setReceivedAt(Instant.now());
        return event;
    }

    public EventResponse toResponse(Event event) {
        EventResponse response = new EventResponse();
        response.setEventId(event.getEventId());
        response.setAccountId(event.getAccountId());
        response.setType(event.getType());
        response.setAmount(event.getAmount());
        response.setCurrency(event.getCurrency());
        response.setEventTimestamp(event.getEventTimestamp());
        response.setMetadata(deserializeMetadata(event.getMetadata()));
        response.setReceivedAt(event.getReceivedAt());
        return response;
    }

    private String serializeMetadata(Map<String, Object> metadata) {
        if (metadata == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            log.warn("Failed to serialize metadata, storing null: {}", e.getMessage());
            return null;
        }
    }

    private Map<String, Object> deserializeMetadata(String json) {
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to deserialize metadata, returning null: {}", e.getMessage());
            return null;
        }
    }
}
