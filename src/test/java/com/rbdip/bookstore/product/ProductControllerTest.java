package com.rbdip.bookstore.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ProductControllerTest {

    @Test
    void createsAndListsProducts() {
        ProductRepository repository = mock(ProductRepository.class);
        ProductController controller = new ProductController(repository);
        Product product = mock(Product.class);
        when(product.getId()).thenReturn(3L);
        when(product.getName()).thenReturn("Refactoring");
        when(product.getPrice()).thenReturn(new BigDecimal("25.50"));
        when(repository.save(any(Product.class))).thenReturn(product);
        when(repository.findAll()).thenReturn(List.of(product));
        Map<String, Object> request = new HashMap<>();
        request.put("name", "Refactoring");
        request.put("price", "25.50");

        Map<String, Object> created = controller.createProduct(request);
        List<Map<String, Object>> products = controller.listProducts();

        assertThat(created).containsEntry("id", 3L).containsEntry("name", "Refactoring");
        assertThat(products).singleElement().satisfies(item -> {
            assertThat(item).containsEntry("id", 3L);
            assertThat(item).containsEntry("price", "25.50");
        });
    }
}
