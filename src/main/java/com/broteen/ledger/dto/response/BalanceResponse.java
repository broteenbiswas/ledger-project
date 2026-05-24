package com.broteen.ledger.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Computed account balance")
public class BalanceResponse {

    @Schema(description = "Account identifier", example = "acct-123")
    private String accountId;

    @Schema(description = "Net balance: sum(CREDIT) - sum(DEBIT)", example = "350.00")
    private BigDecimal balance;

    @Schema(description = "Currency of the balance", example = "USD")
    private String currency;

    @Schema(description = "Total number of events factored into the balance", example = "3")
    private long eventCount;

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public long getEventCount() {
        return eventCount;
    }

    public void setEventCount(long eventCount) {
        this.eventCount = eventCount;
    }
}
