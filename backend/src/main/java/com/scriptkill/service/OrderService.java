package com.scriptkill.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.scriptkill.entity.Order;
import java.util.List;

public interface OrderService extends IService<Order> {
    Order createOrder(Long userId, Long sessionId, Integer playerCount);
    void payOrder(String orderNo, String paymentMethod);
    void cancelOrder(String orderNo);
    void refundOrder(String orderNo);
    void completeOrder(String orderNo);
    List<Order> listUserOrders(Long userId, Integer orderStatus);
}
