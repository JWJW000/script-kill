import request from '@/utils/request'

export function getDashboard(period) {
  return request({
    url: '/statistics/dashboard',
    method: 'get',
    params: { period }
  })
}
