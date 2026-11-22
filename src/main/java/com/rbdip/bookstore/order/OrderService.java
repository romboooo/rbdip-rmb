package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Координирует создание заказа, делегируя отдельные обязанности сервисам.
 */
@Service
public class OrderService {

    private final OrderRequestValidator requestValidator;
    private final OrderLineService orderLineService;
    private final OrderPricingService orderPricingService;
    private final OrderPersistenceService persistenceService;
    private final OrderConfirmationNotifier notifier;

    public OrderService(
            OrderRequestValidator requestValidator,
            OrderLineService orderLineService,
            OrderPricingService orderPricingService,
            OrderPersistenceService persistenceService,
            OrderConfirmationNotifier notifier) {
        this.requestValidator = requestValidator;
        this.orderLineService = orderLineService;
        this.orderPricingService = orderPricingService;
        this.persistenceService = persistenceService;
        this.notifier = notifier;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        requestValidator.validate(request);
        List<OrderLine> lines = orderLineService.loadLines(request);
        BigDecimal total = orderPricingService.calculateTotal(request, lines);
        Order order = persistenceService.save(request, lines);
        notifier.send(request.customerFullName(), order.getId(), total);
        return order;
    }
}
