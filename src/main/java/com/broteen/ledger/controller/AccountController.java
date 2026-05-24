package com.broteen.ledger.controller;

import com.broteen.ledger.dto.response.BalanceResponse;
import com.broteen.ledger.dto.response.ErrorResponse;
import com.broteen.ledger.service.BalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@Tag(name = "Accounts", description = "Account balance computation")
public class AccountController {

    private final BalanceService balanceService;

    public AccountController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @GetMapping("/{accountId}/balance")
    @Operation(
            summary = "Get account balance",
            description = "Computes the net balance for an account as sum(CREDIT) - sum(DEBIT). "
                    + "Correct regardless of the order events were received."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Balance computed successfully",
                    content = @Content(schema = @Schema(implementation = BalanceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Account not found (no events for this account)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BalanceResponse> getBalance(
            @Parameter(description = "Account ID", example = "acct-123")
            @PathVariable String accountId) {
        return ResponseEntity.ok(balanceService.getBalance(accountId));
    }
}
