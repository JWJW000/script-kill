package com.scriptkill.common;

public class Constants {
    // 用户角色
    public static final String ROLE_PLAYER = "PLAYER";
    public static final String ROLE_HOST = "HOST";
    public static final String ROLE_ADMIN = "ADMIN";

    // 订单状态
    public static final int ORDER_STATUS_PENDING = 0; // 待支付
    public static final int ORDER_STATUS_PAID = 1; // 已支付
    public static final int ORDER_STATUS_CANCELLED = 2; // 已取消
    public static final int ORDER_STATUS_COMPLETED = 3; // 已完成

    // 支付状态
    public static final int PAYMENT_STATUS_PENDING = 0; // 待支付
    public static final int PAYMENT_STATUS_PAID = 1; // 已支付
    public static final int PAYMENT_STATUS_CANCELLED = 2; // 已取消
    public static final int PAYMENT_STATUS_REFUNDED = 3; // 已退款

    // 场次状态
    public static final int SESSION_STATUS_CANCELLED = 0; // 已取消/下架
    public static final int SESSION_STATUS_AVAILABLE = 1; // 可预约
    public static final int SESSION_STATUS_FULL = 2; // 已满员
    public static final int SESSION_STATUS_ENDED = 3; // 已结束

    // 拼场状态
    public static final int TEAM_UP_STATUS_CLOSED = 0; // 已关闭
    public static final int TEAM_UP_STATUS_ACTIVE = 1; // 进行中
    public static final int TEAM_UP_STATUS_FULL = 2; // 已达标
    public static final int TEAM_UP_STATUS_SESSION_CREATED = 3; // 已生成场次

    // 支付方式
    public static final String PAYMENT_WECHAT = "WECHAT";
    public static final String PAYMENT_ALIPAY = "ALIPAY";
}
