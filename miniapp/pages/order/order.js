const api = require('../../utils/api')

Page({
  data: {
    orders: [],
    needLogin: false,
    statusTab: '',
    tabs: [
      { label: '全部', value: '' },
      { label: '待支付', value: '0' },
      { label: '已预约', value: '1' },
      { label: '已入场', value: '2' },
      { label: '已完成', value: '3' },
      { label: '已取消', value: '4' },
    ],
  },
  onShow() {
    const token = wx.getStorageSync('token')
    if (!token) {
      this.setData({ orders: [], needLogin: true })
      return
    }
    this.setData({ needLogin: false })
    this.loadOrders()
  },
  loadOrders() {
    const val = this.data.statusTab
    const params = (val !== '' && val !== null) ? { status: parseInt(val) } : {}
    api.getMyOrders(params).then(orders => {
      this.setData({ orders: orders?.records || orders || [] })
    }).catch(() => {})
  },
  onTabChange(e) {
    this.setData({ statusTab: e.currentTarget.dataset.value })
    this.loadOrders()
  },
  onPay(e) {
    const orderId = e.currentTarget.dataset.id
    wx.showLoading({ title: '支付中...' })
    api.pay(orderId).then(() => {
      wx.hideLoading()
      wx.showToast({ title: '支付成功', icon: 'success' })
      this.loadOrders()
    }).catch(() => {
      wx.hideLoading()
    })
  },
  onCancel(e) {
    const orderId = e.currentTarget.dataset.id
    wx.showModal({
      title: '提示',
      content: '确认取消该预约？',
      success: (res) => {
        if (res.confirm) {
          api.cancel(orderId).then(() => {
            wx.showToast({ title: '已取消', icon: 'success' })
            this.loadOrders()
          })
        }
      },
    })
  },
  goLogin() {
    wx.switchTab({ url: '/pages/mine/mine' })
  },
  statusText(s) {
    return ['待支付', '已预约', '已入场', '已完成', '已取消'][s] || '未知'
  },
  statusDesc(s) {
    return ['请尽快完成支付', '等待管理员确认入场', '车辆在停车场内', '已完成，感谢使用', '该订单已取消'][s] || ''
  },
})
