import request from '@/utils/request'

export function uploadFile(file, category = 'other') {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('category', category)
  return request({
    url: '/oss/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function deleteFile(url) {
  return request({
    url: '/oss/delete',
    method: 'delete',
    params: { url }
  })
}
