import request from '@/utils/request'

export function getScripts(params) {
  return request({
    url: '/script/list',
    method: 'get',
    params
  })
}

export function getScriptById(id) {
  return request({
    url: `/script/${id}`,
    method: 'get'
  })
}

export function createScript(data) {
  return request({
    url: '/script',
    method: 'post',
    data
  })
}

export function updateScript(data) {
  return request({
    url: '/script',
    method: 'put',
    data
  })
}

export function deleteScript(id) {
  return request({
    url: `/script/${id}`,
    method: 'delete'
  })
}

export function updateScriptStatus(id, status) {
  return request({
    url: `/script/${id}/status`,
    method: 'post',
    params: { status }
  })
}

export function uploadScriptCover(id, file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: `/script/${id}/cover`,
    method: 'post',
    data: formData,
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 30000
  })
}
