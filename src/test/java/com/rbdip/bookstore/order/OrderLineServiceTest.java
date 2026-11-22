package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.rbdip.bookstore.product.Product;
import com.rbdip.bookstore.product.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class OrderLineServiceTest {

    @Test
    void loadsProductsAndUsesDefaultQuantity() {
        ProductRepository repository = mock(ProductRepository.class);
        Product product = new Product("Clean Code", new BigDecimal("35.00"), null);
        when(repository.findById(4L)).thenReturn(Optional.of(product));
        OrderLineService service = new OrderLineService(repository);
        CreateOrderRequest request = new CreateOrderRequest(
                "Ivan Petrov", "Moscow", null, "regular", null,
                List.of(new CreateOrderRequest.Item(4L, null)));

        List<OrderLine> lines = service.loadLines(request);

        assertThat(lines).singleElement().satisfies(line -> {
            assertThat(line.product()).isSameAs(product);
            assertThat(line.quantity()).isEqualTo(1);
        });
    }

    @Test
    void rejectsUnknownProducts() {
        ProductRepository repository = mock(ProductRepository.class);
        when(repository.findById(404L)).thenReturn(Optional.empty());
        OrderLineService service = new OrderLineService(repository);
        CreateOrderRequest request = new CreateOrderRequest(
                "Ivan Petrov", "Moscow", null, "regular", null,
                List.of(new CreateOrderRequest.Item(404L, 1)));

        assertThatThrownBy(() -> service.loadLines(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("product 404 not found");
    }
}
