package com.rbdip.bookstore.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.rbdip.bookstore.purchase.PurchaseHistory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private PurchaseHistory purchaseHistory;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void addsAnonymousReviewAndChecksPurchaseHistory() {
        when(purchaseHistory.hasAnyPurchaseForProduct(17L)).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Review review = reviewService.addReview(17L, null, 5, "Good book");

        assertThat(review.getAuthorName()).isEqualTo("anonymous");
        assertThat(review.getRating()).isEqualTo(5);
        verify(purchaseHistory).hasAnyPurchaseForProduct(17L);
    }
}
