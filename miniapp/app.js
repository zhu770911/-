App({
  onLaunch() {
    // 检查登录状态
    const token = wx.getStorageSync('token')
    if (!token) {
      console.log('未登录')
    }
  },
  globalData: {
    userInfo: null,
    baseUrl: 'http://localhost:8080/api',
  },
})
