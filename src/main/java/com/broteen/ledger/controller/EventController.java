package com.broteen.ledger.controller;

import com.broteen.ledger.dto.request.EventRequest;
import com.broteen.ledger.dto.response.ErrorResponse;
import com.broteen.ledger.dto.response.EventResponse;
import com.broteen.ledger.dto.response.PagedEventResponse;
import com.broteen.ledger.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
@Tag(name = "Events", description = "Transaction event submission and retrieval")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @Operation(
            summary = "Submit a transaction event",
            description = "Submits a new transaction event. If the eventId already exists, the original event is returned with HTTP 200 (idempotent)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Event created",
                    content = @Content(schema = @Schema(implementation = EventResponse.class))),
            @ApiResponse(responseCode = "200", description = "Duplicate submission — original event returned",
                    content = @Content(schema = @Schema(implementation = EventResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EventResponse> submitEvent(@Valid @RequestBody EventRequest request) {
        EventResponse response = eventService.submitEvent(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get event by ID",
            description = "Retrieves a single transaction event by its unique event ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Event found",
                    content = @Content(schema = @Schema(implementation = EventResponse.class))),
            @ApiResponse(responseCode = "404", description = "Event not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EventResponse> getEventById(
            @Parameter(description = "The unique event ID", example = "evt-001")
            @PathVariable("id") String eventId) {
        return ResponseEntity.ok(eventService.getEventById(eventId));
    }

    @GetMapping
    @Operation(
            summary = "List events for an account",
            description = "Returns a paginated list of events for the given account, ordered chronologically "
                    + "by eventTimestamp regardless of arrival order. Defaults to page 0 with 20 items per page."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Events retrieved",
                    content = @Content(schema = @Schema(implementation = PagedEventResponse.class))),
            @ApiResponse(responseCode = "400", description = "Missing or invalid query parameter",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PagedEventResponse> getEventsByAccount(
            @Parameter(description = "Account ID to filter events by", required = true, example = "acct-123")
            @RequestParam("account") String accountId,
            @Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Number of items per page (max 100)", example = "20")
            @RequestParam(value = "size", defaultValue = "20") int size) {
        int clampedSize = Math.min(size, 100);
        return ResponseEntity.ok(eventService.getEventsByAccount(accountId, page, clampedSize));
    }
}
