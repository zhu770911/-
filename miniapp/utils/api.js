const BASE_URL = 'http://localhost:8080/api'

/**
 * 封装请求
 */
function request(url, options = {}) {
  const token = wx.getStorageSync('token')
  return new Promise((resolve, reject) => {
    wx.request({
      url: BASE_URL + url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '',
      },
      success(res) {
        if (res.statusCode === 401) {
          wx.removeStorageSync('token')
          wx.showToast({ title: '请先登录', icon: 'none' })
          // tabBar 页面只能用 switchTab
          wx.switchTab({ url: '/pages/mine/mine' })
          reject(new Error('请先登录'))
          return
        }
        if (res.data.code === 200) {
          resolve(res.data.data)
        } else {
          wx.showToast({ title: res.data.message || '请求失败', icon: 'none' })
          reject(new Error(res.data.message))
        }
      },
      fail(err) {
        wx.showToast({ title: '网络错误', icon: 'none' })
        reject(err)
      },
    })
  })
}

// ============ API 方法 ============
module.exports = {
  // 用户
  login: (data) => request('/user/login', { method: 'POST', data }),
  register: (data) => request('/user/register', { method: 'POST', data }),
  getUserInfo: () => request('/user/info'),

  // 停车场
  getParkingList: (params) => request('/parking/list', { data: params }),
  getParkingDetail: (id) => request(`/parking/${id}`),
  getParkingSlots: (id) => request(`/parking/${id}/slots`),

  // 预约
  reserve: (data) => request('/booking/reserve', { method: 'POST', data }),
  pay: (orderId) => request(`/booking/pay/${orderId}`, { method: 'PUT' }),
  cancel: (orderId) => request(`/booking/cancel/${orderId}`, { method: 'PUT' }),

  // 订单
  getMyOrders: (params) => request('/order/my', { data: params }),
}
