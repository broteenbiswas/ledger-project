package com.broteen.ledger.service.impl;

import com.broteen.ledger.domain.entity.Event;
import com.broteen.ledger.domain.enums.EventType;
import com.broteen.ledger.dto.response.BalanceResponse;
import com.broteen.ledger.exception.AccountNotFoundException;
import com.broteen.ledger.repository.EventRepository;
import com.broteen.ledger.service.BalanceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class BalanceServiceImpl implements BalanceService {

    private final EventRepository eventRepository;

    public BalanceServiceImpl(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public BalanceResponse getBalance(String accountId) {
        List<Event> events = eventRepository.findByAccountIdOrderByEventTimestampAsc(accountId);

        if (events.isEmpty()) {
            throw new AccountNotFoundException(accountId);
        }

        BalanceResponse response = new BalanceResponse();
        response.setAccountId(accountId);
        response.setBalance(computeBalance(events));
        response.setCurrency(events.get(0).getCurrency());
        response.setEventCount(events.size());
        return response;
    }

    private BigDecimal computeBalance(List<Event> events) {
        return events.stream()
                .map(event -> EventType.CREDIT.equals(event.getType())
                        ? event.getAmount()
                        : event.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
