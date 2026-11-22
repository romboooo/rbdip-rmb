package com.rbdip.bookstore.purchase;

/** Contract for checking whether a product has appeared in a purchase. */
public interface PurchaseHistory {

    boolean hasAnyPurchaseForProduct(Long productId);
}
