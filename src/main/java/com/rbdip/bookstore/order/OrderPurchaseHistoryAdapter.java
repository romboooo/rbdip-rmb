package com.rbdip.bookstore.order;

import com.rbdip.bookstore.purchase.PurchaseHistory;
import org.springframework.stereotype.Service;

@Service
public class OrderPurchaseHistoryAdapter implements PurchaseHistory {

    private final OrderItemRepository orderItemRepository;

    public OrderPurchaseHistoryAdapter(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public boolean hasAnyPurchaseForProduct(Long productId) {
        return orderItemRepository.existsByProduct_Id(productId);
    }
}
