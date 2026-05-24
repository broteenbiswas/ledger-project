package com.broteen.ledger.dto.response;

import com.broteen.ledger.domain.enums.EventType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Schema(description = "Transaction event details")
public class EventResponse {

    @Schema(description = "Unique identifier for the event", example = "evt-001")
    private String eventId;

    @Schema(description = "The account this event belongs to", example = "acct-123")
    private String accountId;

    @Schema(description = "Transaction type", example = "CREDIT")
    private EventType type;

    @Schema(description = "Transaction amount", example = "150.00")
    private BigDecimal amount;

    @Schema(description = "ISO 4217 currency code", example = "USD")
    private String currency;

    @Schema(description = "When the event originally occurred")
    private Instant eventTimestamp;

    @Schema(description = "Optional additional context metadata")
    private Map<String, Object> metadata;

    @Schema(description = "When this event was received by the API")
    private Instant receivedAt;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Instant getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(Instant eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }
}
