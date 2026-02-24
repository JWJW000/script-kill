import request from '@/utils/request'

export function createReview(data) {
  return request({
    url: '/review/create',
    method: 'post',
    params: data
  })
}

export function getReviewList(params) {
  return request({
    url: '/review/list',
    method: 'get',
    params
  })
}

export function getMyReviews() {
  return request({
    url: '/review/my-reviews',
    method: 'get'
  })
}

export function updateReview(id, data) {
  return request({
    url: `/review/${id}`,
    method: 'put',
    params: data
  })
}

export function deleteReview(id) {
  return request({
    url: `/review/${id}`,
    method: 'delete'
  })
}

export function getHostStatistics(hostId) {
  return request({
    url: `/review/host/${hostId}/statistics`,
    method: 'get'
  })
}

export function getScriptStatistics(scriptId) {
  return request({
    url: `/review/script/${scriptId}/statistics`,
    method: 'get'
  })
}
