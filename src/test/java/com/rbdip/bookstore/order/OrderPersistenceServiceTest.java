package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rbdip.bookstore.product.Product;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class OrderPersistenceServiceTest {

    @Test
    void savesCustomerOrderAndItems() {
        OrderRepository orders = mock(OrderRepository.class);
        OrderItemRepository items = mock(OrderItemRepository.class);
        CustomerRepository customers = mock(CustomerRepository.class);
        when(customers.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(orders.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        OrderPersistenceService service = new OrderPersistenceService(orders, items, customers);
        CreateOrderRequest request = new CreateOrderRequest(
                "Ivan Petrov", "Moscow", "+70000000000", "regular", null, List.of());
        Product product = new Product("Clean Code", new BigDecimal("35.00"), null);

        Order saved = service.save(request, List.of(new OrderLine(product, 2)));

        assertThat(saved.getCustomerFullName()).isEqualTo("Ivan Petrov");
        verify(customers).save(any(Customer.class));
        verify(orders).save(any(Order.class));
        verify(items).save(any(OrderItem.class));
    }
}
