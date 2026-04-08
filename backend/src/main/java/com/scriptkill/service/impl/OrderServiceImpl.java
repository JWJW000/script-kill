package com.scriptkill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scriptkill.common.Constants;
import com.scriptkill.entity.Order;
import com.scriptkill.entity.Session;
import com.scriptkill.entity.TeamUp;
import com.scriptkill.mapper.OrderMapper;
import com.scriptkill.service.OrderService;
import com.scriptkill.service.SessionService;
import com.scriptkill.service.TeamUpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    @Autowired
    private SessionService sessionService;
    @Autowired
    private TeamUpService teamUpService;

    @Override
    @Transactional
    public Order createOrder(Long userId, Long sessionId, Integer playerCount) {
        Session session = sessionService.getById(sessionId);
        if (session == null) {
            throw new RuntimeException("场次不存在");
        }
        
        Order order = new Order();
        order.setOrderNo("SK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setUserId(userId);
        order.setSessionId(sessionId);
        order.setPlayerCount(playerCount);
        order.setTotalAmount(session.getPrice().multiply(new java.math.BigDecimal(playerCount)));
        order.setPaymentStatus(Constants.PAYMENT_STATUS_PENDING);
        order.setOrderStatus(Constants.ORDER_STATUS_PENDING);
        
        this.save(order);
        
        // 锁定库存
        sessionService.bookSession(sessionId, playerCount);
        
        return order;
    }

    @Override
    @Transactional
    public Order createTeamUpOrder(Long userId, Long teamUpId, Integer playerCount, java.math.BigDecimal price) {
        Order order = new Order();
        order.setOrderNo("TU" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        order.setUserId(userId);
        order.setSessionId(-teamUpId); // 临时用负数teamUpId作为标识
        order.setPlayerCount(playerCount);
        order.setTotalAmount(price.multiply(new java.math.BigDecimal(playerCount)));
        order.setPaymentStatus(Constants.PAYMENT_STATUS_PENDING);
        order.setOrderStatus(Constants.ORDER_STATUS_PENDING);
        
        this.save(order);
        return order;
    }

    @Override
    public void updateTeamUpOrdersToSession(Long teamUpId, Long sessionId) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("session_id", -teamUpId); // 找到临时订单
        List<Order> orders = this.list(wrapper);
        for (Order order : orders) {
            order.setSessionId(sessionId);
            this.updateById(order);
        }
    }

    @Override
    @Transactional
    public void payOrder(String orderNo, String paymentMethod) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("order_no", orderNo);
        Order order = this.getOne(wrapper);
        
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getPaymentStatus() != Constants.PAYMENT_STATUS_PENDING) {
            throw new RuntimeException("订单状态异常");
        }
        
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus(Constants.PAYMENT_STATUS_PAID);
        order.setOrderStatus(Constants.ORDER_STATUS_PAID);
        order.setPayTime(LocalDateTime.now());
        
        this.updateById(order);
        
        // 如果是拼场订单（sessionId < 0），检查是否所有参与者都已支付
        if (order.getSessionId() < 0) {
            Long teamUpId = -order.getSessionId();
            checkAndCompleteTeamUp(teamUpId);
        } else {
            // 普通场次订单，检查是否所有该场次的订单都已支付
            checkAndUpdateSessionFull(order.getSessionId());
        }
    }

    /**
     * 检查拼场的所有订单是否都已支付，如果都已支付，则创建正式场次
     */
    private void checkAndCompleteTeamUp(Long teamUpId) {
        // 获取该拼场的所有订单
        QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
        orderWrapper.eq("session_id", -teamUpId); // 临时sessionId为负的teamUpId
        List<Order> teamUpOrders = this.list(orderWrapper);
        
        if (teamUpOrders == null || teamUpOrders.isEmpty()) {
            return;
        }
        
        // 检查是否所有订单都已支付
        boolean allPaid = true;
        for (Order o : teamUpOrders) {
            if (o.getPaymentStatus() != Constants.PAYMENT_STATUS_PAID) {
                allPaid = false;
                break;
            }
        }
        
        if (allPaid) {
            // 所有订单都已支付，创建正式场次
            teamUpService.createSessionFromTeamUp(teamUpId);
        }
    }

    /**
     * 检查场次的所有订单是否都已支付，如果都已支付，则将场次状态设为已满员
     */
    private void checkAndUpdateSessionFull(Long sessionId) {
        // 检查该场次是否是对应拼场生成的
        QueryWrapper<TeamUp> teamUpWrapper = new QueryWrapper<>();
        teamUpWrapper.eq("session_id", sessionId);
        teamUpWrapper.in("status", Constants.TEAM_UP_STATUS_FULL, Constants.TEAM_UP_STATUS_ACTIVE);
        TeamUp teamUp = teamUpService.getOne(teamUpWrapper);
        
        if (teamUp == null) {
            // 该场次不是由拼场生成的，不需要处理
            return;
        }
        
        // 获取该场次的所有订单
        QueryWrapper<Order> orderWrapper = new QueryWrapper<>();
        orderWrapper.eq("session_id", sessionId);
        List<Order> sessionOrders = this.list(orderWrapper);
        
        // 检查是否所有订单都已支付
        boolean allPaid = sessionOrders != null && !sessionOrders.isEmpty();
        for (Order o : sessionOrders) {
            if (o.getPaymentStatus() != Constants.PAYMENT_STATUS_PAID) {
                allPaid = false;
                break;
            }
        }
        
        if (allPaid) {
            // 所有订单都已支付，将场次状态设为已满员
            Session session = sessionService.getById(sessionId);
            if (session != null && session.getStatus() == Constants.SESSION_STATUS_AVAILABLE) {
                session.setStatus(Constants.SESSION_STATUS_FULL);
                sessionService.updateById(session);
            }
        }
    }

    @Override
    @Transactional
    public void cancelOrder(String orderNo) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("order_no", orderNo);
        Order order = this.getOne(wrapper);
        
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != Constants.ORDER_STATUS_PENDING) {
            throw new RuntimeException("只能取消待支付订单");
        }
        
        order.setPaymentStatus(Constants.PAYMENT_STATUS_CANCELLED);
        order.setOrderStatus(Constants.ORDER_STATUS_CANCELLED);
        order.setCancelTime(LocalDateTime.now());
        
        this.updateById(order);
        
        // 释放库存
        Session session = sessionService.getById(order.getSessionId());
        if (session != null) {
            session.setCurrentPlayers(session.getCurrentPlayers() - order.getPlayerCount());
            if (session.getStatus() == Constants.SESSION_STATUS_FULL) {
                session.setStatus(Constants.SESSION_STATUS_AVAILABLE);
            }
            sessionService.updateById(session);
        }
    }

    @Override
    @Transactional
    public void refundOrder(String orderNo) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("order_no", orderNo);
        Order order = this.getOne(wrapper);
        
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getPaymentStatus() != Constants.PAYMENT_STATUS_PAID) {
            throw new RuntimeException("只能退款已支付的订单");
        }
        
        order.setPaymentStatus(Constants.PAYMENT_STATUS_REFUNDED);
        order.setOrderStatus(Constants.ORDER_STATUS_CANCELLED);
        this.updateById(order);
        
        // 释放库存
        Session session = sessionService.getById(order.getSessionId());
        if (session != null) {
            session.setCurrentPlayers(session.getCurrentPlayers() - order.getPlayerCount());
            if (session.getStatus() == Constants.SESSION_STATUS_FULL) {
                session.setStatus(Constants.SESSION_STATUS_AVAILABLE);
            }
            sessionService.updateById(session);
        }
    }

    @Override
    @Transactional
    public void completeOrder(String orderNo) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("order_no", orderNo);
        Order order = this.getOne(wrapper);
        
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (order.getOrderStatus() != Constants.ORDER_STATUS_PAID) {
            throw new RuntimeException("只能完成已支付的订单");
        }
        
        order.setOrderStatus(Constants.ORDER_STATUS_COMPLETED);
        order.setCompleteTime(LocalDateTime.now());
        this.updateById(order);
    }

    @Override
    public List<Order> listUserOrders(Long userId, Integer orderStatus) {
        QueryWrapper<Order> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        if (orderStatus != null) {
            wrapper.eq("order_status", orderStatus);
        }
        wrapper.orderByDesc("create_time");
        return this.list(wrapper);
    }
}
