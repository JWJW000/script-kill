import request from '@/utils/request'

export function createOrder(sessionId, playerCount) {
  return request({
    url: '/order/create',
    method: 'post',
    params: { sessionId, playerCount }
  })
}

export function payOrder(orderNo, paymentMethod) {
  return request({
    url: '/order/pay',
    method: 'post',
    params: { orderNo, paymentMethod }
  })
}

export function cancelOrder(orderNo) {
  return request({
    url: '/order/cancel',
    method: 'post',
    params: { orderNo }
  })
}

export function getOrderList(orderStatus) {
  return request({
    url: '/order/list',
    method: 'get',
    params: { orderStatus }
  })
}

export function getSessionOrders(sessionId) {
  return request({
    url: `/order/session/${sessionId}`,
    method: 'get'
  })
}

export function getOrderDetail(orderNo) {
  return request({
    url: `/order/${orderNo}`,
    method: 'get'
  })
}

export function refundOrder(orderNo) {
  return request({
    url: '/order/refund',
    method: 'post',
    params: { orderNo }
  })
}
