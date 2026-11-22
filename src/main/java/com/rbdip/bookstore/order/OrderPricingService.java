package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderPricingService {

    private static final String REGULAR_CUSTOMER = "regular";

    private final PricingCalculator pricingCalculator;

    public OrderPricingService(PricingCalculator pricingCalculator) {
        this.pricingCalculator = pricingCalculator;
    }

    public BigDecimal calculateTotal(CreateOrderRequest request, List<OrderLine> lines) {
        List<PricingCalculator.LineItem> priceItems = lines.stream()
                .map(line -> new PricingCalculator.LineItem(line.product().getPrice(), line.quantity()))
                .toList();
        String customerType = request.customerType() == null ? REGULAR_CUSTOMER : request.customerType();
        return pricingCalculator.calculateOrderTotal(priceItems, customerType, request.couponCode());
    }
}
