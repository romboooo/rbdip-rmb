package com.rbdip.bookstore.order;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderRequestValidator {

    public void validate(CreateOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("order request is required");
        }
        validateCustomer(request);
        validateItems(request.items());
    }

    private void validateCustomer(CreateOrderRequest request) {
        if (isBlank(request.customerFullName())) {
            throw new IllegalArgumentException("customerFullName is required");
        }
        if (isBlank(request.customerAddress())) {
            throw new IllegalArgumentException("customerAddress is required");
        }
    }

    private void validateItems(List<CreateOrderRequest.Item> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("order must contain at least one item");
        }
        for (CreateOrderRequest.Item item : items) {
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
