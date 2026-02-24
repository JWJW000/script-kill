package com.scriptkill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.scriptkill.common.Result;
import com.scriptkill.entity.Order;
import com.scriptkill.entity.Review;
import com.scriptkill.entity.Script;
import com.scriptkill.entity.Session;
import com.scriptkill.entity.User;
import com.scriptkill.service.OrderService;
import com.scriptkill.service.ReviewService;
import com.scriptkill.service.ScriptService;
import com.scriptkill.service.SessionService;
import com.scriptkill.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/review")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private UserService userService;
    @Autowired
    private ScriptService scriptService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private OrderService orderService;

    @PostMapping("/create")
    public Result<Review> createReview(@RequestParam Long orderId, @RequestParam Integer scriptRating,
                                       @RequestParam Integer hostRating, @RequestParam(required = false) String scriptComment,
                                       @RequestParam(required = false) String hostComment, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            Review review = reviewService.createReview(orderId, userId, scriptRating, hostRating, scriptComment, hostComment);
            enrichReview(review);
            return Result.success("评价成功", review);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<Review>> listReviews(@RequestParam(required = false) Long scriptId,
                                             @RequestParam(required = false) Long hostId) {
        List<Review> reviews = reviewService.listReviews(scriptId, hostId);
        reviews.forEach(this::enrichReview);
        return Result.success(reviews);
    }

    @GetMapping("/host/{hostId}/statistics")
    public Result<Map<String, Object>> getHostStatistics(@PathVariable Long hostId) {
        Map<String, Object> stats = reviewService.getHostStatistics(hostId);
        return Result.success(stats);
    }

    @GetMapping("/script/{scriptId}/statistics")
    public Result<Map<String, Object>> getScriptStatistics(@PathVariable Long scriptId) {
        Map<String, Object> stats = reviewService.getScriptStatistics(scriptId);
        return Result.success(stats);
    }

    @GetMapping("/my-reviews")
    public Result<List<Review>> getMyReviews(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<Review> reviews = reviewService.listUserReviews(userId);
        reviews.forEach(this::enrichReview);
        return Result.success(reviews);
    }

    @PutMapping("/{id}")
    public Result<Review> updateReview(@PathVariable Long id, @RequestParam(required = false) Integer scriptRating,
                                      @RequestParam(required = false) Integer hostRating,
                                      @RequestParam(required = false) String scriptComment,
                                      @RequestParam(required = false) String hostComment,
                                      HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            Review review = reviewService.updateReview(id, userId, scriptRating, hostRating, scriptComment, hostComment);
            enrichReview(review);
            return Result.success("更新成功", review);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteReview(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            reviewService.deleteReview(id, userId);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/admin/{id}")
    public Result<?> adminDeleteReview(@PathVariable Long id, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        Review review = reviewService.getById(id);
        if (review != null) {
            review.setStatus(0);
            reviewService.updateById(review);
        }
        return Result.success("删除成功");
    }

    private void enrichReview(Review review) {
        if (review == null) return;
        try {
            if (review.getUserId() != null) {
                User user = userService.getById(review.getUserId());
                if (user != null) {
                    review.setUserName(user.getRealName() != null ? user.getRealName() : user.getUsername());
                }
            }
            if (review.getScriptId() != null) {
                Script script = scriptService.getById(review.getScriptId());
                if (script != null) {
                    review.setScriptName(script.getName());
                }
            }
            if (review.getHostId() != null) {
                User host = userService.getById(review.getHostId());
                if (host != null) {
                    review.setHostName(host.getRealName() != null ? host.getRealName() : host.getUsername());
                }
            }
            // 通过订单 -> 场次，补充评价对应的场次时间
            if (review.getOrderId() != null && review.getSessionTime() == null) {
                Order order = orderService.getById(review.getOrderId());
                if (order != null && order.getSessionId() != null) {
                    Session session = sessionService.getById(order.getSessionId());
                    if (session != null) {
                        review.setSessionTime(session.getSessionTime());
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }
}
