package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Модуль расчёта цены заказа. Намеренно почти не покрыт тестами и
 * содержит magic numbers / нечитаемые ветвления скидок - цель для
 * характеризационных тестов (ЛР2) и mutation-testing гейта PIT (ЛР5).
 */
@Component
public class PricingCalculator {

    private static final int BULK_QUANTITY_THRESHOLD = 10;
    private static final BigDecimal BULK_DISCOUNT_FACTOR = BigDecimal.valueOf(0.95);
    private static final BigDecimal VIP_DISCOUNT_FACTOR = BigDecimal.valueOf(0.90);
    private static final BigDecimal WHOLESALE_DISCOUNT_FACTOR = BigDecimal.valueOf(0.85);
    private static final BigDecimal SAVE_10_AMOUNT = BigDecimal.TEN;
    private static final BigDecimal SAVE_20_FACTOR = BigDecimal.valueOf(0.80);
    private static final BigDecimal HIGH_VALUE_THRESHOLD = BigDecimal.valueOf(1000);
    private static final BigDecimal HIGH_VALUE_DISCOUNT_FACTOR = BigDecimal.valueOf(0.98);

    public record LineItem(BigDecimal price, int quantity) {
    }

    public BigDecimal calculateOrderTotal(List<LineItem> items, String customerType, String couponCode) {
        BigDecimal total = BigDecimal.ZERO;

        for (LineItem item : items) {
            BigDecimal linePrice = item.price().multiply(BigDecimal.valueOf(item.quantity()));
            if (item.quantity() > BULK_QUANTITY_THRESHOLD) {
                linePrice = linePrice.multiply(BULK_DISCOUNT_FACTOR);
            }
            total = total.add(linePrice);
        }

        if ("vip".equals(customerType)) {
            total = total.multiply(VIP_DISCOUNT_FACTOR);
        } else if ("wholesale".equals(customerType)) {
            total = total.multiply(WHOLESALE_DISCOUNT_FACTOR);
        }

        if ("SAVE10".equals(couponCode)) {
            total = total.subtract(SAVE_10_AMOUNT);
        } else if ("SAVE20PERCENT".equals(couponCode)) {
            total = total.multiply(SAVE_20_FACTOR);
        }

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        if (total.compareTo(HIGH_VALUE_THRESHOLD) > 0) {
            total = total.multiply(HIGH_VALUE_DISCOUNT_FACTOR);
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
