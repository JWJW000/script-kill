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

@RestController
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private ScriptService scriptService;
    @Autowired
    private UserService userService;
    @Autowired
    private ReviewService reviewService;

    @PostMapping("/create")
    public Result<Order> createOrder(@RequestParam Long sessionId, @RequestParam Integer playerCount, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            Order order = orderService.createOrder(userId, sessionId, playerCount);
            enrichOrder(order);
            return Result.success("订单创建成功", order);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/pay")
    public Result<?> payOrder(@RequestParam String orderNo, @RequestParam String paymentMethod) {
        try {
            orderService.payOrder(orderNo, paymentMethod);
            return Result.success("支付成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/cancel")
    public Result<?> cancelOrder(@RequestParam String orderNo, HttpServletRequest request) {
        try {
            orderService.cancelOrder(orderNo);
            return Result.success("取消成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<List<Order>> listOrders(@RequestParam(required = false) Integer orderStatus, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");

        List<Order> orders;
        if ("ADMIN".equals(role)) {
            if (orderStatus != null) {
                QueryWrapper<Order> wrapper = new QueryWrapper<>();
                wrapper.eq("order_status", orderStatus);
                wrapper.orderByDesc("create_time");
                orders = orderService.list(wrapper);
            } else {
                QueryWrapper<Order> wrapper = new QueryWrapper<>();
                wrapper.orderByDesc("create_time");
                orders = orderService.list(wrapper);
            }
        } else {
            orders = orderService.listUserOrders(userId, orderStatus);
        }

        // 补全每条订单的关联信息
        for (Order order : orders) {
            enrichOrder(order);
        }

        // 标记哪些订单已评价：根据 review 表中是否存在对应 orderId
        if (orders != null && !orders.isEmpty()) {
            java.util.Set<Long> reviewedOrderIds = new java.util.HashSet<>();
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Review> rw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            rw.in("order_id", orders.stream().map(Order::getId).toArray());
            java.util.List<Review> reviews = reviewService.list(rw);
            for (Review r : reviews) {
                reviewedOrderIds.add(r.getOrderId());
            }
            for (Order o : orders) {
                o.setReviewed(reviewedOrderIds.contains(o.getId()));
            }
        }

        return Result.success(orders);
    }

    /**
     * 按场次查询所有订单（主持人 / 管理员查看预约玩家）
     */
    @GetMapping("/session/{sessionId}")
    public Result<List<Order>> listSessionOrders(@PathVariable Long sessionId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");

        Session session = sessionService.getById(sessionId);
        if (session == null) {
            return Result.error("场次不存在");
        }
        // 仅允许管理员或该场次主持人查看
        if (!"ADMIN".equals(role) && !("HOST".equals(role) && session.getHostId() != null && session.getHostId().equals(userId))) {
            return Result.error(403, "无权限查看该场次订单");
        }

        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", sessionId);
        wrapper.orderByDesc("create_time");
        List<Order> orders = orderService.list(wrapper);

        for (Order order : orders) {
            enrichOrder(order);
        }

        // 标记哪些订单已评价
        if (orders != null && !orders.isEmpty()) {
            java.util.Set<Long> reviewedOrderIds = new java.util.HashSet<>();
            com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Review> rw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
            rw.in("order_id", orders.stream().map(Order::getId).toArray());
            java.util.List<Review> reviews = reviewService.list(rw);
            for (Review r : reviews) {
                reviewedOrderIds.add(r.getOrderId());
            }
            for (Order o : orders) {
                o.setReviewed(reviewedOrderIds.contains(o.getId()));
            }
        }

        return Result.success(orders);
    }

    @PostMapping("/refund")
    public Result<?> refundOrder(@RequestParam String orderNo, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        try {
            orderService.refundOrder(orderNo);
            return Result.success("退款成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 完成订单（管理员/主持人将已支付订单标记为已完成）
     */
    @PostMapping("/complete")
    public Result<?> completeOrder(@RequestParam String orderNo, HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        if (!"ADMIN".equals(role) && !"HOST".equals(role)) {
            return Result.error(403, "无权限操作");
        }
        try {
            orderService.completeOrder(orderNo);
            return Result.success("订单已完成");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{orderNo}")
    public Result<Order> getOrderDetail(@PathVariable String orderNo, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");

        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("order_no", orderNo);
        Order order = orderService.getOne(wrapper);

        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!"ADMIN".equals(role) && !order.getUserId().equals(userId)) {
            return Result.error(403, "无权限查看此订单");
        }

        enrichOrder(order);
        return Result.success(order);
    }

    /**
     * 补全订单的关联信息（剧本名、场次时间、主持人等）
     */
    private void enrichOrder(Order order) {
        if (order == null) return;

        try {
            // 补全用户名
            if (order.getUserId() != null) {
                User user = userService.getById(order.getUserId());
                if (user != null) {
                    order.setUserName(user.getRealName() != null ? user.getRealName() : user.getUsername());
                }
            }

            // 通过 sessionId 查找场次 → 补全场次时间、地点、剧本、主持人
            if (order.getSessionId() != null) {
                Session session = sessionService.getById(order.getSessionId());
                if (session != null) {
                    order.setSessionTime(session.getSessionTime());
                    order.setLocation(session.getLocation());
                    order.setHostId(session.getHostId());

                    // 主持人名称
                    if (session.getHostId() != null) {
                        User host = userService.getById(session.getHostId());
                        if (host != null) {
                            order.setHostName(host.getRealName() != null ? host.getRealName() : host.getUsername());
                        }
                    }

                    // 剧本信息
                    if (session.getScriptId() != null) {
                        order.setScriptId(session.getScriptId());
                        Script script = scriptService.getById(session.getScriptId());
                        if (script != null) {
                            order.setScriptName(script.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 关联数据查询失败不影响主数据返回
        }
    }
}
