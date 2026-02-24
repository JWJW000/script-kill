package com.scriptkill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scriptkill.entity.Order;
import com.scriptkill.entity.Review;
import com.scriptkill.entity.Session;
import com.scriptkill.mapper.ReviewMapper;
import com.scriptkill.service.OrderService;
import com.scriptkill.service.ReviewService;
import com.scriptkill.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {
    @Autowired
    private OrderService orderService;
    @Autowired
    private SessionService sessionService;

    @Override
    public Review createReview(Long orderId, Long userId, Integer scriptRating, Integer hostRating, String scriptComment, String hostComment) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("该订单已评价");
        }
        
        // 从订单获取场次信息
        Order order = orderService.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权评价此订单");
        }
        if (order.getOrderStatus() != 1 && order.getOrderStatus() != 3) {
            throw new RuntimeException("只能评价已支付或已完成的订单");
        }
        
        // 从场次获取剧本和主持人信息
        Session session = sessionService.getById(order.getSessionId());
        if (session == null) {
            throw new RuntimeException("场次不存在");
        }
        
        Review review = new Review();
        review.setOrderId(orderId);
        review.setUserId(userId);
        review.setScriptId(session.getScriptId());
        review.setHostId(session.getHostId());
        review.setScriptRating(scriptRating);
        review.setHostRating(hostRating);
        review.setScriptComment(scriptComment);
        review.setHostComment(hostComment);
        review.setStatus(1);
        
        this.save(review);
        return review;
    }

    @Override
    public List<Review> listReviews(Long scriptId, Long hostId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        if (scriptId != null) {
            wrapper.eq("script_id", scriptId);
        }
        if (hostId != null) {
            wrapper.eq("host_id", hostId);
        }
        wrapper.orderByDesc("create_time");
        return this.list(wrapper);
    }

    @Override
    public List<Review> listUserReviews(Long userId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("status", 1);
        wrapper.orderByDesc("create_time");
        return this.list(wrapper);
    }

    @Override
    public Review updateReview(Long reviewId, Long userId, Integer scriptRating, Integer hostRating, String scriptComment, String hostComment) {
        Review review = this.getById(reviewId);
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }
        if (!review.getUserId().equals(userId)) {
            throw new RuntimeException("无权修改此评价");
        }
        if (scriptRating != null) review.setScriptRating(scriptRating);
        if (hostRating != null) review.setHostRating(hostRating);
        if (scriptComment != null) review.setScriptComment(scriptComment);
        if (hostComment != null) review.setHostComment(hostComment);
        this.updateById(review);
        return review;
    }

    @Override
    public void deleteReview(Long reviewId, Long userId) {
        Review review = this.getById(reviewId);
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }
        if (!review.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除此评价");
        }
        review.setStatus(0);
        this.updateById(review);
    }

    @Override
    public Map<String, Object> getHostStatistics(Long hostId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("host_id", hostId);
        wrapper.eq("status", 1);
        List<Review> reviews = this.list(wrapper);
        
        Map<String, Object> stats = new HashMap<>();
        if (reviews.isEmpty()) {
            stats.put("averageRating", 0.0);
            stats.put("totalReviews", 0);
            stats.put("goodRate", 0.0);
        } else {
            double avgRating = reviews.stream().mapToInt(Review::getHostRating).average().orElse(0.0);
            long goodCount = reviews.stream().filter(r -> r.getHostRating() >= 4).count();
            double goodRate = (double) goodCount / reviews.size() * 100;
            
            stats.put("averageRating", avgRating);
            stats.put("totalReviews", reviews.size());
            stats.put("goodRate", goodRate);
        }
        
        return stats;
    }

    @Override
    public Map<String, Object> getScriptStatistics(Long scriptId) {
        QueryWrapper<Review> wrapper = new QueryWrapper<>();
        wrapper.eq("script_id", scriptId);
        wrapper.eq("status", 1);
        List<Review> reviews = this.list(wrapper);
        
        Map<String, Object> stats = new HashMap<>();
        if (reviews.isEmpty()) {
            stats.put("averageRating", 0.0);
            stats.put("totalReviews", 0);
            stats.put("goodRate", 0.0);
        } else {
            double avgRating = reviews.stream().mapToInt(Review::getScriptRating).average().orElse(0.0);
            long goodCount = reviews.stream().filter(r -> r.getScriptRating() >= 4).count();
            double goodRate = (double) goodCount / reviews.size() * 100;
            
            stats.put("averageRating", avgRating);
            stats.put("totalReviews", reviews.size());
            stats.put("goodRate", goodRate);
        }
        
        return stats;
    }
}
