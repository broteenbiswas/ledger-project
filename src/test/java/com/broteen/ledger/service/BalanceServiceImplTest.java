package com.broteen.ledger.service;

import com.broteen.ledger.domain.entity.Event;
import com.broteen.ledger.domain.enums.EventType;
import com.broteen.ledger.dto.response.BalanceResponse;
import com.broteen.ledger.exception.AccountNotFoundException;
import com.broteen.ledger.repository.EventRepository;
import com.broteen.ledger.service.impl.BalanceServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private BalanceServiceImpl balanceService;

    @Test
    @DisplayName("getBalance: credits minus debits gives correct net balance")
    void getBalance_mixedEvents_correctNetBalance() {
        List<Event> events = List.of(
                buildEvent(EventType.CREDIT, "500.00"),
                buildEvent(EventType.DEBIT,  "200.00"),
                buildEvent(EventType.CREDIT, "100.00")
        );
        when(eventRepository.findByAccountIdOrderByEventTimestampAsc("acct-123")).thenReturn(events);

        BalanceResponse result = balanceService.getBalance("acct-123");

        assertThat(result.getBalance()).isEqualByComparingTo("400.00");
        assertThat(result.getAccountId()).isEqualTo("acct-123");
        assertThat(result.getEventCount()).isEqualTo(3);
        assertThat(result.getCurrency()).isEqualTo("USD");
    }

    @Test
    @DisplayName("getBalance: only credits produce positive balance")
    void getBalance_onlyCredits_positiveBalance() {
        List<Event> events = List.of(
                buildEvent(EventType.CREDIT, "300.00"),
                buildEvent(EventType.CREDIT, "200.00")
        );
        when(eventRepository.findByAccountIdOrderByEventTimestampAsc("acct-123")).thenReturn(events);

        BalanceResponse result = balanceService.getBalance("acct-123");

        assertThat(result.getBalance()).isEqualByComparingTo("500.00");
    }

    @Test
    @DisplayName("getBalance: only debits produce negative balance")
    void getBalance_onlyDebits_negativeBalance() {
        List<Event> events = List.of(buildEvent(EventType.DEBIT, "300.00"));
        when(eventRepository.findByAccountIdOrderByEventTimestampAsc("acct-123")).thenReturn(events);

        BalanceResponse result = balanceService.getBalance("acct-123");

        assertThat(result.getBalance()).isEqualByComparingTo("-300.00");
    }

    @Test
    @DisplayName("getBalance: unknown account throws AccountNotFoundException")
    void getBalance_unknownAccount_throwsAccountNotFoundException() {
        when(eventRepository.findByAccountIdOrderByEventTimestampAsc("unknown"))
                .thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> balanceService.getBalance("unknown"))
                .isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    @DisplayName("getBalance: out-of-order arrival does not affect balance computation")
    void getBalance_outOfOrderEvents_balanceUnaffected() {
        List<Event> events = List.of(
                buildEvent(EventType.DEBIT,  "100.00"),
                buildEvent(EventType.CREDIT, "300.00")
        );
        when(eventRepository.findByAccountIdOrderByEventTimestampAsc("acct-123")).thenReturn(events);

        BalanceResponse result = balanceService.getBalance("acct-123");

        assertThat(result.getBalance()).isEqualByComparingTo("200.00");
    }

    private Event buildEvent(EventType type, String amount) {
        Event event = new Event();
        event.setAccountId("acct-123");
        event.setType(type);
        event.setAmount(new BigDecimal(amount));
        event.setCurrency("USD");
        event.setEventTimestamp(Instant.now());
        event.setReceivedAt(Instant.now());
        return event;
    }
}
