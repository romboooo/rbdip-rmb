package com.rbdip.bookstore.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ReviewControllerTest {

    @Test
    void addsReviewsAndRendersMissingCommentAsEmptyText() {
        ReviewService service = mock(ReviewService.class);
        ReviewController controller = new ReviewController(service);
        Review review = mock(Review.class);
        when(review.getId()).thenReturn(9L);
        when(service.addReview(3L, "Ana", 5, "Useful")).thenReturn(review);
        Review listedReview = mock(Review.class);
        when(listedReview.getAuthorName()).thenReturn("Ana");
        when(listedReview.getRating()).thenReturn(5);
        when(listedReview.getComment()).thenReturn(null);
        when(service.listReviews(3L)).thenReturn(List.of(listedReview));

        Map<String, Object> response = controller.addReview(
                3L, Map.of("authorName", "Ana", "rating", 5, "comment", "Useful"));
        List<Map<String, Object>> reviews = controller.listReviews(3L);

        assertThat(response).containsEntry("id", 9L);
        assertThat(reviews).singleElement().satisfies(item -> {
            assertThat(item).containsEntry("authorName", "Ana");
            assertThat(item).containsEntry("rating", 5);
            assertThat(item).containsEntry("comment", "");
        });
    }
}
