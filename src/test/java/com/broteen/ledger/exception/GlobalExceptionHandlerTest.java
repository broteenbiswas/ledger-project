package com.broteen.ledger.exception;

import com.broteen.ledger.dto.response.ErrorResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("DataIntegrityViolationException returns 409 Conflict")
    void handleDataIntegrityViolation_returns409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("duplicate key");
        ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(409, response.getBody().getStatus());
        assertEquals("Conflict", response.getBody().getError());
    }

    @Test
    @DisplayName("Unhandled Exception returns 500 Internal Server Error")
    void handleGenericException_returns500() {
        Exception ex = new RuntimeException("something unexpected");
        ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("Internal Server Error", response.getBody().getError());
    }

    @Test
    @DisplayName("MissingServletRequestParameterException returns 400 with param name")
    void handleMissingParam_returns400WithParamName() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("account", "String");
        ResponseEntity<ErrorResponse> response = handler.handleMissingParam(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatus());
        assertTrue(response.getBody().getMessage().contains("account"));
    }
}
