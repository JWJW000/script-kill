import request from '@/utils/request'

export function chatWithAI(question) {
  return request({
    url: '/ai/chat',
    method: 'post',
    data: { question }
  })
}
