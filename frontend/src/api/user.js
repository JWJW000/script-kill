import request from '@/utils/request'

export function getUserInfo() {
  return request({
    url: '/user/info',
    method: 'get'
  })
}

export function updateUserInfo(data) {
  return request({
    url: '/user/info',
    method: 'put',
    data
  })
}

export function updatePassword(oldPassword, newPassword) {
  return request({
    url: '/user/password',
    method: 'post',
    params: { oldPassword, newPassword }
  })
}

export function uploadAvatar(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/user/avatar',
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 30000
  })
}

// 管理员功能
export function getUserList() {
  return request({
    url: '/user/list',
    method: 'get'
  })
}

export function getUserById(id) {
  return request({
    url: `/user/${id}`,
    method: 'get'
  })
}

export function createUser(data) {
  return request({
    url: '/user',
    method: 'post',
    data
  })
}

export function updateUser(id, data) {
  return request({
    url: `/user/${id}`,
    method: 'put',
    data
  })
}

export function deleteUser(id) {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}

export function resetPassword(id, newPassword) {
  return request({
    url: `/user/${id}/reset-password`,
    method: 'post',
    params: { newPassword }
  })
}

export function assignRole(id, role) {
  return request({
    url: `/user/${id}/role`,
    method: 'post',
    params: { role }
  })
}
