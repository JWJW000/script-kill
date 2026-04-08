import request from '@/utils/request'

export function getRoomList() {
  return request({
    url: '/room/list',
    method: 'get'
  })
}

export function getRoomsByStore(storeId) {
  return request({
    url: '/room/list-by-store',
    method: 'get',
    params: { storeId }
  })
}

export function getRoom(id) {
  return request({
    url: `/room/${id}`,
    method: 'get'
  })
}

export function createRoom(data) {
  return request({
    url: '/room',
    method: 'post',
    data
  })
}

export function updateRoom(id, data) {
  return request({
    url: `/room/${id}`,
    method: 'put',
    data
  })
}

export function deleteRoom(id) {
  return request({
    url: `/room/${id}`,
    method: 'delete'
  })
}
