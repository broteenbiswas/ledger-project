package com.broteen.ledger.dto.request;

import com.broteen.ledger.domain.enums.EventType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

@Schema(description = "Payload for submitting a transaction event")
public class EventRequest {

    @NotBlank(message = "eventId is required")
    @Schema(description = "Unique identifier for the event", example = "evt-001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String eventId;

    @NotBlank(message = "accountId is required")
    @Schema(description = "The account this event belongs to", example = "acct-123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String accountId;

    @NotNull(message = "type is required")
    @Schema(description = "Transaction type: CREDIT or DEBIT", example = "CREDIT", requiredMode = Schema.RequiredMode.REQUIRED)
    private EventType type;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "amount must be greater than 0")
    @Schema(description = "Transaction amount, must be greater than zero", example = "150.00", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal amount;

    @NotBlank(message = "currency is required")
    @Schema(description = "ISO 4217 currency code", example = "USD", requiredMode = Schema.RequiredMode.REQUIRED)
    private String currency;

    @NotNull(message = "eventTimestamp is required")
    @Schema(description = "ISO 8601 timestamp of when the event originally occurred", example = "2026-05-15T14:02:11Z", requiredMode = Schema.RequiredMode.REQUIRED)
    private Instant eventTimestamp;

    @Schema(description = "Optional additional context metadata", nullable = true)
    private Map<String, Object> metadata;

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
}
