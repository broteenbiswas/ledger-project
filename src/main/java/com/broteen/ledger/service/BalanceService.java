package com.broteen.ledger.service;

import com.broteen.ledger.dto.response.BalanceResponse;

public interface BalanceService {

    BalanceResponse getBalance(String accountId);
}
