package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.rbdip.bookstore.product.Product;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderPricingServiceTest {

    @Test
    void defaultsCustomerTypeAndAppliesCoupon() {
        OrderPricingService service = new OrderPricingService(new PricingCalculator());
        CreateOrderRequest request = new CreateOrderRequest(
                "Ivan Petrov", "Moscow", null, null, "SAVE10", List.of());
        List<OrderLine> lines = List.of(new OrderLine(
                new Product("Book", new BigDecimal("25.00"), null), 1));

        BigDecimal total = service.calculateTotal(request, lines);

        assertThat(total).isEqualByComparingTo("15.00");
    }
}
