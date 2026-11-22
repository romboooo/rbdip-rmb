package com.rbdip.bookstore.review;

import com.rbdip.bookstore.purchase.PurchaseHistory;
import java.util.List;
import java.util.logging.Logger;
import org.springframework.stereotype.Service;

/**
 * Намеренная архитектурная связанность: чтобы проверить, что автор отзыва
 * действительно покупал товар, сервис напрямую лезет во внутренние
 * репозитории пакета order, вместо обращения через выделенный контракт
 * (интерфейс/событие). Это цель для выделения модуля review по Strangler
 * Fig в ЛР4 - после рефакторинга ArchitectureRulesTest (пакет reference)
 * должен зафиксировать отсутствие такой зависимости.
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PurchaseHistory purchaseHistory;

    private static final Logger LOGGER = Logger.getLogger(ReviewService.class.getName());

    public ReviewService(ReviewRepository reviewRepository, PurchaseHistory purchaseHistory) {
        this.reviewRepository = reviewRepository;
        this.purchaseHistory = purchaseHistory;
    }

    public Review addReview(Long productId, String authorName, Integer rating, String comment) {
        if (purchaseHistory.hasAnyPurchaseForProduct(productId)) {
            LOGGER.fine(() -> "Review submitted for a product with purchase history: " + productId);
        }
        Review review = new Review(productId, authorName == null ? "anonymous" : authorName, rating, comment);
        return reviewRepository.save(review);
    }

    public List<Review> listReviews(Long productId) {
        return reviewRepository.findByProductId(productId);
    }
}
