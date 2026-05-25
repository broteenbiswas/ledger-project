package com.broteen.ledger.service.impl;

import com.broteen.ledger.domain.entity.Event;
import com.broteen.ledger.dto.request.EventRequest;
import com.broteen.ledger.dto.response.EventResponse;
import com.broteen.ledger.dto.response.PagedEventResponse;
import com.broteen.ledger.exception.DuplicateEventException;
import com.broteen.ledger.exception.EventNotFoundException;
import com.broteen.ledger.mapper.EventMapper;
import com.broteen.ledger.repository.EventRepository;
import com.broteen.ledger.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private static final Logger log = LoggerFactory.getLogger(EventServiceImpl.class);

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

    public EventServiceImpl(EventRepository eventRepository, EventMapper eventMapper) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
    }

    @Override
    @Transactional
    public EventResponse submitEvent(EventRequest request) {
        log.info("Submitting event: eventId={}, accountId={}", request.getEventId(), request.getAccountId());
        Optional<Event> existing = eventRepository.findByEventId(request.getEventId());
        if (existing.isPresent()) {
            log.warn("Duplicate event received — returning original: eventId={}", request.getEventId());
            throw new DuplicateEventException(eventMapper.toResponse(existing.get()));
        }
        Event saved = eventRepository.save(eventMapper.toEntity(request));
        log.info("Event saved successfully: eventId={}, accountId={}", saved.getEventId(), saved.getAccountId());
        return eventMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EventResponse getEventById(String eventId) {
        log.debug("Fetching event by id: {}", eventId);
        return eventRepository.findByEventId(eventId)
                .map(eventMapper::toResponse)
                .orElseThrow(() -> {
                    log.warn("Event not found: {}", eventId);
                    return new EventNotFoundException(eventId);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public PagedEventResponse getEventsByAccount(String accountId, int page, int size) {
        log.debug("Fetching events for accountId={}, page={}, size={}", accountId, page, size);
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("eventTimestamp").ascending());
        Page<Event> eventPage = eventRepository.findByAccountId(accountId, pageRequest);

        List<EventResponse> content = eventPage.getContent()
                .stream()
                .map(eventMapper::toResponse)
                .collect(Collectors.toList());

        PagedEventResponse response = new PagedEventResponse();
        response.setContent(content);
        response.setPage(eventPage.getNumber());
        response.setSize(eventPage.getSize());
        response.setTotalElements(eventPage.getTotalElements());
        response.setTotalPages(eventPage.getTotalPages());
        response.setFirst(eventPage.isFirst());
        response.setLast(eventPage.isLast());
        return response;
    }
}
