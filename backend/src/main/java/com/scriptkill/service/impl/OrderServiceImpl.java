package com.scriptkill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.scriptkill.common.Constants;
import com.scriptkill.entity.Order;
import com.scriptkill.entity.Session;
import com.scriptkill.mapper.OrderMapper;
import com.scriptkill.service.OrderService;
import com.scriptkill.service.SessionService;
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
