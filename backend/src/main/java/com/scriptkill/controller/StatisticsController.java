package com.scriptkill.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.scriptkill.common.Constants;
import com.scriptkill.common.Result;
import com.scriptkill.entity.*;
import com.scriptkill.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ScriptService scriptService;
    @Autowired
    private UserService userService;
    @Autowired
    private TeamUpService teamUpService;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard(@RequestParam(required = false) String period, HttpServletRequest request) {
        Map<String, Object> data = new HashMap<>();
        
        try {
            String role = (String) request.getAttribute("role");
            Long userId = (Long) request.getAttribute("userId");
            
            if ("ADMIN".equals(role)) {
                data = getAdminStatistics(period);
            } else if ("HOST".equals(role)) {
                data = getHostStatistics(userId);
            } else if ("PLAYER".equals(role)) {
                data = getPlayerStatistics(userId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            data.put("totalOrders", 0);
            data.put("totalRevenue", 0);
            data.put("activeSessions", 0);
            data.put("totalUsers", 0);
        }
        
        return Result.success(data);
    }

    private Map<String, Object> getAdminStatistics(String period) {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime startTime = getStartTime(period);
        
        try {
            QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
            if (startTime != null) {
                orderWrapper.ge("create_time", startTime);
            }
            long totalOrders = orderService.count(orderWrapper);
            stats.put("totalOrders", totalOrders);
            
            QueryWrapper<Order> paidWrapper = new QueryWrapper<>();
            paidWrapper.eq("payment_status", 1);
            if (startTime != null) {
                paidWrapper.ge("create_time", startTime);
            }
            List<Order> paidOrders = orderService.list(paidWrapper);
            BigDecimal totalRevenue = paidOrders.stream()
                .map(Order::getTotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            stats.put("totalRevenue", totalRevenue);
        } catch (Exception e) {
            stats.put("totalOrders", 0);
            stats.put("totalRevenue", BigDecimal.ZERO);
        }
        
        try {
            QueryWrapper<Session> sessionWrapper = new QueryWrapper<>();
            sessionWrapper.eq("status", 1);
            long activeSessions = sessionService.count(sessionWrapper);
            stats.put("activeSessions", activeSessions);
        } catch (Exception e) {
            stats.put("activeSessions", 0);
        }
        
        try {
            QueryWrapper<User> userWrapper = new QueryWrapper<>();
            userWrapper.eq("status", 1);
            long totalUsers = userService.count(userWrapper);
            stats.put("totalUsers", totalUsers);
        } catch (Exception e) {
            stats.put("totalUsers", 0);
        }
        
        try {
            stats.put("topScripts", getTopScripts(10));
        } catch (Exception e) {
            stats.put("topScripts", new ArrayList<>());
        }
        
        try {
            stats.put("hostRanking", getHostRanking());
        } catch (Exception e) {
            stats.put("hostRanking", new ArrayList<>());
        }
        
        try {
            stats.put("hostRatingRanking", getHostRatingRanking());
        } catch (Exception e) {
            stats.put("hostRatingRanking", new ArrayList<>());
        }
        
        return stats;
    }

    private Map<String, Object> getHostStatistics(Long hostId) {
        Map<String, Object> stats = new HashMap<>();

        try {
            QueryWrapper<Session> sessionWrapper = new QueryWrapper<>();
            sessionWrapper.eq("host_id", hostId);
            long totalSessions = sessionService.count(sessionWrapper);
            stats.put("totalSessions", totalSessions);
        } catch (Exception e) {
            stats.put("totalSessions", 0);
        }
        
        try {
            Map<String, Object> reviewStats = reviewService.getHostStatistics(hostId);
            if (reviewStats != null) {
                stats.putAll(reviewStats);
            } else {
                stats.put("averageRating", 0.0);
                stats.put("totalReviews", 0);
                stats.put("goodRate", 0.0);
            }
        } catch (Exception e) {
            stats.put("averageRating", 0.0);
            stats.put("totalReviews", 0);
            stats.put("goodRate", 0.0);
        }

        // 场次完成数趋势（最近7天，按已结束场次统计）
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime start = today.minusDays(6).atStartOfDay();

            QueryWrapper<Session> sessionWrapper = new QueryWrapper<>();
            sessionWrapper.eq("host_id", hostId);
            sessionWrapper.eq("status", Constants.SESSION_STATUS_ENDED);
            sessionWrapper.ge("session_time", start);
            List<Session> endedSessions = sessionService.list(sessionWrapper);

            Map<LocalDate, Long> countByDate = endedSessions.stream()
                    .filter(s -> s.getSessionTime() != null)
                    .collect(Collectors.groupingBy(s -> s.getSessionTime().toLocalDate(), Collectors.counting()));

            List<Map<String, Object>> sessionTrend = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                LocalDate d = today.minusDays(i);
                Map<String, Object> item = new HashMap<>();
                item.put("date", d.toString());
                item.put("completedCount", countByDate.getOrDefault(d, 0L));
                sessionTrend.add(item);
            }
            stats.put("sessionTrend", sessionTrend);
        } catch (Exception e) {
            stats.put("sessionTrend", new ArrayList<>());
        }

        // 评分趋势（最近7天，按评价时间分组计算主持人评分均值）
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime start = today.minusDays(6).atStartOfDay();

            QueryWrapper<Review> reviewWrapper = new QueryWrapper<>();
            reviewWrapper.eq("host_id", hostId);
            reviewWrapper.ge("create_time", start);
            List<Review> hostReviews = reviewService.list(reviewWrapper);

            Map<LocalDate, List<Review>> reviewsByDate = hostReviews.stream()
                    .filter(r -> r.getCreateTime() != null)
                    .collect(Collectors.groupingBy(r -> r.getCreateTime().toLocalDate()));

            List<Map<String, Object>> ratingTrend = new ArrayList<>();
            for (int i = 6; i >= 0; i--) {
                LocalDate d = today.minusDays(i);
                List<Review> list = reviewsByDate.getOrDefault(d, Collections.emptyList());
                double avg = 0.0;
                if (!list.isEmpty()) {
                    avg = list.stream()
                            .map(Review::getHostRating)
                            .filter(Objects::nonNull)
                            .mapToInt(Integer::intValue)
                            .average()
                            .orElse(0.0);
                }
                Map<String, Object> item = new HashMap<>();
                item.put("date", d.toString());
                item.put("avgRating", avg);
                ratingTrend.add(item);
            }
            stats.put("ratingTrend", ratingTrend);
        } catch (Exception e) {
            stats.put("ratingTrend", new ArrayList<>());
        }

        return stats;
    }

    private Map<String, Object> getPlayerStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
            orderWrapper.eq("user_id", userId);
            long totalOrders = orderService.count(orderWrapper);
            stats.put("totalOrders", totalOrders);
        } catch (Exception e) {
            stats.put("totalOrders", 0);
        }
        
        try {
            QueryWrapper<TeamUp> teamUpWrapper = new QueryWrapper<>();
            teamUpWrapper.eq("initiator_id", userId);
            long totalTeamUps = teamUpService.count(teamUpWrapper);
            stats.put("totalTeamUps", totalTeamUps);
        } catch (Exception e) {
            stats.put("totalTeamUps", 0);
        }
        
        try {
            List<?> reviews = reviewService.listUserReviews(userId);
            stats.put("totalReviews", reviews != null ? reviews.size() : 0);
        } catch (Exception e) {
            stats.put("totalReviews", 0);
        }
        
        return stats;
    }

    private LocalDateTime getStartTime(String period) {
        if (period == null){
            period = "month";
        }
        LocalDate today = LocalDate.now();
        switch (period) {
            case "day":
                return today.atStartOfDay();
            case "week":
                return today.minusDays(7).atStartOfDay();
            case "month":
                return today.minusMonths(1).atStartOfDay();
            default:
                return today.minusMonths(1).atStartOfDay();
        }
    }

    private List<Map<String, Object>> getTopScripts(int limit) {
        QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
        orderWrapper.eq("payment_status", 1);
        List<Order> paidOrders = orderService.list(orderWrapper);
        
        if (paidOrders == null || paidOrders.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, Long> scriptOrderCounts = new HashMap<>();
        for (Order order : paidOrders) {
            Session session = sessionService.getById(order.getSessionId());
            if (session != null && session.getScriptId() != null) {
                scriptOrderCounts.merge(session.getScriptId(), 1L, Long::sum);
            }
        }
        
        return scriptOrderCounts.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(limit)
            .map(entry -> {
                Map<String, Object> item = new HashMap<>();
                Script script = scriptService.getById(entry.getKey());
                item.put("scriptId", entry.getKey());
                item.put("scriptName", script != null ? script.getName() : "未知");
                item.put("orderCount", entry.getValue());
                return item;
            })
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getHostRanking() {
        QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
        orderWrapper.eq("payment_status", 1);
        List<Order> paidOrders = orderService.list(orderWrapper);
        
        if (paidOrders == null || paidOrders.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, Long> hostOrderCounts = new HashMap<>();
        for (Order order : paidOrders) {
            Session session = sessionService.getById(order.getSessionId());
            if (session != null && session.getHostId() != null) {
                hostOrderCounts.merge(session.getHostId(), 1L, Long::sum);
            }
        }
        
        return hostOrderCounts.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .map(entry -> {
                Map<String, Object> item = new HashMap<>();
                User host = userService.getById(entry.getKey());
                item.put("hostId", entry.getKey());
                item.put("hostName", host != null ? host.getRealName() : "未知");
                item.put("orderCount", entry.getValue());
                return item;
            })
            .collect(Collectors.toList());
    }

    private List<Map<String, Object>> getHostRatingRanking() {
        QueryWrapper<User> userWrapper = new QueryWrapper<>();
        userWrapper.eq("role", "HOST");
        List<User> hosts = userService.list(userWrapper);
        
        if (hosts == null || hosts.isEmpty()) {
            return new ArrayList<>();
        }
        
        return hosts.stream()
            .map(host -> {
                Map<String, Object> stats = reviewService.getHostStatistics(host.getId());
                Map<String, Object> item = new HashMap<>();
                item.put("hostId", host.getId());
                item.put("hostName", host.getRealName());
                double avgRating = 0.0;
                if (stats != null && stats.get("averageRating") != null) {
                    Object val = stats.get("averageRating");
                    avgRating = val instanceof Number ? ((Number) val).doubleValue() : 0.0;
                }
                item.put("averageRating", avgRating);
                item.put("totalReviews", stats != null ? stats.getOrDefault("totalReviews", 0) : 0);
                return item;
            })
            .sorted((a, b) -> Double.compare(
                ((Number) b.get("averageRating")).doubleValue(),
                ((Number) a.get("averageRating")).doubleValue()
            ))
            .collect(Collectors.toList());
    }
}
