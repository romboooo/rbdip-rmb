package com.rbdip.bookstore.order;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Оптимизированный список для GET /orders появится в ЛР4.
    List<OrderItem> findByOrder_Id(Long orderId);
}
