package com.rbdip.bookstore.order;

import org.springframework.stereotype.Component;

@Component
public class OrderRequestValidator {

    public void validate(CreateOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("order request is required");
        }
        if (isBlank(request.customerFullName())) {
            throw new IllegalArgumentException("customerFullName is required");
        }
        if (isBlank(request.customerAddress())) {
            throw new IllegalArgumentException("customerAddress is required");
        }
        if (request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("order must contain at least one item");
        }
        for (CreateOrderRequest.Item item : request.items()) {
            validateItem(item);
        }
    }

    private void validateItem(CreateOrderRequest.Item item) {
        if (item == null || item.productId() == null) {
            throw new IllegalArgumentException("productId is required");
        }
        if (item.quantity() != null && item.quantity() <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
