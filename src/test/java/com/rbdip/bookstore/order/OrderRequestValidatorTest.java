package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class OrderRequestValidatorTest {

    private final OrderRequestValidator validator = new OrderRequestValidator();

    @Test
    void acceptsRequestWithDefaultQuantity() {
        CreateOrderRequest request = new CreateOrderRequest(
                "Ivan Petrov", "Moscow", null, "regular", null,
                List.of(new CreateOrderRequest.Item(1L, null)));

        assertThatCode(() -> validator.validate(request)).doesNotThrowAnyException();
    }

    @Test
    void rejectsMissingCustomerName() {
        CreateOrderRequest request = new CreateOrderRequest(
                " ", "Moscow", null, "regular", null,
                List.of(new CreateOrderRequest.Item(1L, 1)));

        assertThatThrownBy(() -> validator.validate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("customerFullName is required");
    }

    @Test
    void rejectsMissingProductIdAndNonPositiveQuantity() {
        CreateOrderRequest missingProduct = new CreateOrderRequest(
                "Ivan Petrov", "Moscow", null, "regular", null,
                List.of(new CreateOrderRequest.Item(null, 1)));
        CreateOrderRequest invalidQuantity = new CreateOrderRequest(
                "Ivan Petrov", "Moscow", null, "regular", null,
                List.of(new CreateOrderRequest.Item(1L, 0)));

        assertThatThrownBy(() -> validator.validate(missingProduct))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("productId is required");
        assertThatThrownBy(() -> validator.validate(invalidQuantity))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("quantity must be positive");
    }
}
