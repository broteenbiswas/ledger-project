package com.broteen.ledger.repository;

import com.broteen.ledger.domain.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByEventId(String eventId);

    List<Event> findByAccountIdOrderByEventTimestampAsc(String accountId);

    Page<Event> findByAccountId(String accountId, Pageable pageable);

    boolean existsByAccountId(String accountId);
}
