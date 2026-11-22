package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import java.util.logging.Logger;
import org.springframework.stereotype.Component;

@Component
public class OrderConfirmationNotifier {

    private static final Logger LOGGER = Logger.getLogger(OrderConfirmationNotifier.class.getName());

    public void send(String customerName, Long orderId, BigDecimal total) {
        // Реальный почтовый транспорт не настроен в учебном проекте.
        LOGGER.info(() -> String.format(
                "[email] Dear %s, your order #%d for %s has been placed.", customerName, orderId, total));
    }
}
