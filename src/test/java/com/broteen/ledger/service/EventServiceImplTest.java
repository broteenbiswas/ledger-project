package com.broteen.ledger.service;

import com.broteen.ledger.domain.entity.Event;
import com.broteen.ledger.domain.enums.EventType;
import com.broteen.ledger.dto.request.EventRequest;
import com.broteen.ledger.dto.response.EventResponse;
import com.broteen.ledger.dto.response.PagedEventResponse;
import com.broteen.ledger.exception.DuplicateEventException;
import com.broteen.ledger.exception.EventNotFoundException;
import com.broteen.ledger.mapper.EventMapper;
import com.broteen.ledger.repository.EventRepository;
import com.broteen.ledger.service.impl.EventServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    private EventRequest request;
    private Event entity;
    private EventResponse response;

    @BeforeEach
    void setUp() {
        request = new EventRequest();
        request.setEventId("evt-001");
        request.setAccountId("acct-123");
        request.setType(EventType.CREDIT);
        request.setAmount(new BigDecimal("150.00"));
        request.setCurrency("USD");
        request.setEventTimestamp(Instant.parse("2026-05-15T14:02:11Z"));

        entity = new Event();
        entity.setEventId("evt-001");
        entity.setAccountId("acct-123");
        entity.setType(EventType.CREDIT);
        entity.setAmount(new BigDecimal("150.00"));
        entity.setCurrency("USD");
        entity.setEventTimestamp(Instant.parse("2026-05-15T14:02:11Z"));
        entity.setReceivedAt(Instant.now());

        response = new EventResponse();
        response.setEventId("evt-001");
        response.setAccountId("acct-123");
        response.setType(EventType.CREDIT);
        response.setAmount(new BigDecimal("150.00"));
        response.setCurrency("USD");
    }

    @Test
    @DisplayName("submitEvent: new event is persisted and returned")
    void submitEvent_newEvent_persistsAndReturns() {
        when(eventRepository.findByEventId("evt-001")).thenReturn(Optional.empty());
        when(eventMapper.toEntity(request)).thenReturn(entity);
        when(eventRepository.save(entity)).thenReturn(entity);
        when(eventMapper.toResponse(entity)).thenReturn(response);

        EventResponse result = eventService.submitEvent(request);

        assertThat(result.getEventId()).isEqualTo("evt-001");
        verify(eventRepository).save(entity);
    }

    @Test
    @DisplayName("submitEvent: duplicate eventId throws DuplicateEventException without saving")
    void submitEvent_duplicateEventId_throwsDuplicateEventException() {
        when(eventRepository.findByEventId("evt-001")).thenReturn(Optional.of(entity));
        when(eventMapper.toResponse(entity)).thenReturn(response);

        assertThatThrownBy(() -> eventService.submitEvent(request))
                .isInstanceOf(DuplicateEventException.class)
                .satisfies(ex -> assertThat(((DuplicateEventException) ex).getExistingEvent().getEventId())
                        .isEqualTo("evt-001"));

        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("getEventById: existing event is returned")
    void getEventById_exists_returnsEvent() {
        when(eventRepository.findByEventId("evt-001")).thenReturn(Optional.of(entity));
        when(eventMapper.toResponse(entity)).thenReturn(response);

        EventResponse result = eventService.getEventById("evt-001");

        assertThat(result.getEventId()).isEqualTo("evt-001");
    }

    @Test
    @DisplayName("getEventById: unknown id throws EventNotFoundException")
    void getEventById_notFound_throwsEventNotFoundException() {
        when(eventRepository.findByEventId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getEventById("unknown"))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    @DisplayName("getEventsByAccount: returns paginated response with correct metadata")
    void getEventsByAccount_returnsPaginatedResponse() {
        Event early = buildEvent("evt-A", Instant.parse("2026-05-15T08:00:00Z"));
        Event late  = buildEvent("evt-B", Instant.parse("2026-05-15T12:00:00Z"));
        when(eventRepository.findByAccountId(eq("acct-123"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(early, late)));
        when(eventMapper.toResponse(any())).thenReturn(response);

        PagedEventResponse result = eventService.getEventsByAccount("acct-123", 0, 20);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.isLast()).isTrue();
        verify(eventRepository).findByAccountId(eq("acct-123"), any(Pageable.class));
    }

    private Event buildEvent(String eventId, Instant timestamp) {
        Event e = new Event();
        e.setEventId(eventId);
        e.setAccountId("acct-123");
        e.setEventTimestamp(timestamp);
        e.setReceivedAt(Instant.now());
        return e;
    }
}
