package com.broteen.ledger.exception;

import com.broteen.ledger.dto.response.EventResponse;

public class DuplicateEventException extends RuntimeException {

    private final EventResponse existingEvent;

    public DuplicateEventException(EventResponse existingEvent) {
        super("Event with id '" + existingEvent.getEventId() + "' already exists");
        this.existingEvent = existingEvent;
    }

    public EventResponse getExistingEvent() {
        return existingEvent;
    }
}
