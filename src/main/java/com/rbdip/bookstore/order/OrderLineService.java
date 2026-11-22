package com.rbdip.bookstore.order;

import com.rbdip.bookstore.product.Product;
import com.rbdip.bookstore.product.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class OrderLineService {

    private static final int DEFAULT_QUANTITY = 1;

    private final ProductRepository productRepository;

    public OrderLineService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<OrderLine> loadLines(CreateOrderRequest request) {
        return request.items().stream()
                .map(this::loadLine)
                .toList();
    }

    private OrderLine loadLine(CreateOrderRequest.Item item) {
        Product product = productRepository.findById(item.productId())
                .orElseThrow(() -> new IllegalArgumentException("product " + item.productId() + " not found"));
        int quantity = item.quantity() == null ? DEFAULT_QUANTITY : item.quantity();
        return new OrderLine(product, quantity);
    }
}
