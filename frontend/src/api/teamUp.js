import request from '@/utils/request'

export function createTeamUp(data) {
  return request({
    url: '/team-up/create',
    method: 'post',
    params: data
  })
}

export function joinTeamUp(id) {
  return request({
    url: `/team-up/join/${id}`,
    method: 'post'
  })
}

export function leaveTeamUp(id) {
  return request({
    url: `/team-up/leave/${id}`,
    method: 'post'
  })
}

export function getTeamUpList(params) {
  return request({
    url: '/team-up/list',
    method: 'get',
    params
  })
}

export function getRecommendedTeamUps() {
  return request({
    url: '/team-up/recommend',
    method: 'get'
  })
}
