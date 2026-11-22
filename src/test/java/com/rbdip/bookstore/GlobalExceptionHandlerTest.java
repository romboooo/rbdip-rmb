package com.rbdip.bookstore;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GlobalExceptionHandlerTest {

    @Test
    void mapsIllegalArgumentToBadRequest() {
        var response = new GlobalExceptionHandler().handleValidation(new IllegalArgumentException("invalid order"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "invalid order");
    }
}
