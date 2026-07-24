const api = require('../../utils/api')

Page({
  data: {
    isLogin: false,
    userInfo: null,
    phone: '',
    password: '',
    showRegister: false,
  },
  onShow() {
    const token = wx.getStorageSync('token')
    if (token) {
      this.setData({ isLogin: true })
      this.loadUserInfo()
    }
  },
  loadUserInfo() {
    api.getUserInfo().then(user => {
      this.setData({ userInfo: user })
    }).catch(() => {
      this.setData({ isLogin: false })
      wx.removeStorageSync('token')
    })
  },
  onPhoneInput(e) {
    this.setData({ phone: e.detail.value })
  },
  onPasswordInput(e) {
    this.setData({ password: e.detail.value })
  },
  onLogin() {
    if (!this.data.phone || !this.data.password) {
      wx.showToast({ title: '请填写手机号和密码', icon: 'none' })
      return
    }
    api.login({ phone: this.data.phone, password: this.data.password }).then(res => {
      wx.setStorageSync('token', res.token)
      this.setData({ isLogin: true })
      this.loadUserInfo()
      wx.showToast({ title: '登录成功', icon: 'success' })
    }).catch(() => {})
  },
  onRegister() {
    if (!this.data.phone || !this.data.password) {
      wx.showToast({ title: '请填写手机号和密码', icon: 'none' })
      return
    }
    api.register({ phone: this.data.phone, password: this.data.password }).then(() => {
      wx.showToast({ title: '注册成功，请登录', icon: 'success' })
      this.setData({ showRegister: false })
      this.onLogin()
    }).catch(() => {})
  },
  toggleForm() {
    this.setData({ showRegister: !this.data.showRegister })
  },
  onLogout() {
    wx.removeStorageSync('token')
    this.setData({ isLogin: false, userInfo: null })
  },
})
