package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PricingCalculatorTest {

    private final PricingCalculator calculator = new PricingCalculator();

    @Test
    void appliesBulkDiscountOnlyAboveTenItems() {
        assertThat(calculate("10.00", 10, "regular", null)).isEqualByComparingTo("100.00");
        assertThat(calculate("10.00", 11, "regular", null)).isEqualByComparingTo("104.50");
    }

    @Test
    void appliesVipAndWholesaleDiscounts() {
        assertThat(calculate("100.00", 1, "vip", null)).isEqualByComparingTo("90.00");
        assertThat(calculate("100.00", 1, "wholesale", null)).isEqualByComparingTo("85.00");
    }

    @Test
    void appliesFixedAndPercentageCoupons() {
        assertThat(calculate("100.00", 1, "regular", "SAVE10")).isEqualByComparingTo("90.00");
        assertThat(calculate("100.00", 1, "regular", "SAVE20PERCENT")).isEqualByComparingTo("80.00");
    }

    @Test
    void doesNotAllowFixedCouponToProduceNegativeTotal() {
        assertThat(calculate("5.00", 1, "regular", "SAVE10")).isEqualByComparingTo("0.00");
    }

    @Test
    void appliesHighValueDiscountOnlyAboveOneThousand() {
        assertThat(calculate("1000.00", 1, "regular", null)).isEqualByComparingTo("1000.00");
        assertThat(calculate("1001.00", 1, "regular", null)).isEqualByComparingTo("980.98");
    }

    @Test
    void roundsHalfUpToTwoDecimalPlaces() {
        assertThat(calculate("2.345", 1, "regular", null)).isEqualByComparingTo("2.35");
    }

    @Test
    void appliesCustomerDiscountBeforeFixedCoupon() {
        assertThat(calculate("20.00", 1, "vip", "SAVE10")).isEqualByComparingTo("8.00");
    }

    @Test
    void ignoresUnknownCustomerTypesAndCoupons() {
        assertThat(calculate("20.00", 1, "student", "SAVE30")).isEqualByComparingTo("20.00");
    }

    @Test
    void emptyOrderHasZeroTotal() {
        assertThat(calculator.calculateOrderTotal(List.of(), "regular", null)).isEqualByComparingTo("0.00");
    }

    private BigDecimal calculate(String price, int quantity, String customerType, String couponCode) {
        return calculator.calculateOrderTotal(
                List.of(new PricingCalculator.LineItem(new BigDecimal(price), quantity)), customerType, couponCode);
    }
}
