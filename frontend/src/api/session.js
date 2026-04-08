import request from '@/utils/request'

export function getSessionList(params) {
  return request({
    url: '/session/list',
    method: 'get',
    params
  })
}

export function createSession(data) {
  return request({
    url: '/session',
    method: 'post',
    data
  })
}

export function updateSession(id, data) {
  return request({
    url: `/session/${id}`,
    method: 'put',
    data
  })
}

export function deleteSession(id) {
  return request({
    url: `/session/${id}`,
    method: 'delete'
  })
}

export function updateSessionStatus(id, status) {
  return request({
    url: `/session/${id}/status`,
    method: 'post',
    params: { status }
  })
}

// 主持人功能
export function getHostSessions() {
  return request({
    url: '/session/host/my-sessions',
    method: 'get'
  })
}

export function confirmAttendance(sessionId) {
  return request({
    url: `/session/${sessionId}/confirm`,
    method: 'post'
  })
}

export function cancelConfirmAttendance(sessionId) {
  return request({
    url: `/session/${sessionId}/cancel-confirm`,
    method: 'post'
  })
}

export function cancelSession(sessionId) {
  return request({
    url: `/session/${sessionId}/cancel`,
    method: 'post'
  })
}

export function endSession(sessionId) {
  return request({
    url: `/session/${sessionId}/end`,
    method: 'post'
  })
}
