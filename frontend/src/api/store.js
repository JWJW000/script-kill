import request from '@/utils/request'

export function getStoreList() {
  return request({
    url: '/store/list',
    method: 'get'
  })
}

export function getStore(id) {
  return request({
    url: `/store/${id}`,
    method: 'get'
  })
}

export function createStore(data) {
  return request({
    url: '/store',
    method: 'post',
    data
  })
}

export function updateStore(id, data) {
  return request({
    url: `/store/${id}`,
    method: 'put',
    data
  })
}

export function deleteStore(id) {
  return request({
    url: `/store/${id}`,
    method: 'delete'
  })
}
