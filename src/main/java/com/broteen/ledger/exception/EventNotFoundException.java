package com.broteen.ledger.exception;

public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(String eventId) {
        super("Event not found with id: '" + eventId + "'");
    }
}
