package com.scriptkill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scriptkill.entity.Review;
import java.util.List;
import java.util.Map;

public interface ReviewService extends IService<Review> {
    Review createReview(Long orderId, Long userId, Integer scriptRating, Integer hostRating, String scriptComment, String hostComment);
    List<Review> listReviews(Long scriptId, Long hostId);
    List<Review> listUserReviews(Long userId);
    Review updateReview(Long reviewId, Long userId, Integer scriptRating, Integer hostRating, String scriptComment, String hostComment);
    void deleteReview(Long reviewId, Long userId);
    Map<String, Object> getHostStatistics(Long hostId);
    Map<String, Object> getScriptStatistics(Long scriptId);
}
