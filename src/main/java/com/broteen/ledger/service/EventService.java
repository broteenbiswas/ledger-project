package com.broteen.ledger.service;

import com.broteen.ledger.dto.request.EventRequest;
import com.broteen.ledger.dto.response.EventResponse;
import com.broteen.ledger.dto.response.PagedEventResponse;

public interface EventService {

    EventResponse submitEvent(EventRequest request);

    EventResponse getEventById(String eventId);

    PagedEventResponse getEventsByAccount(String accountId, int page, int size);
}
