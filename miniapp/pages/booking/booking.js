const api = require('../../utils/api')

Page({
  data: {
    slotId: null,
    slotNumber: '',
    lotName: '',
    enterDate: '',
    enterTime: '',
    duration: 60,
    durationText: '1小时',
    estimatedFee: 5,
  },
  onLoad(options) {
    this.setData({
      slotId: options.slotId,
      slotNumber: options.slotNumber,
      lotName: options.lotName || '',
    })
  },
  onDateChange(e) {
    this.setData({ enterDate: e.detail.value })
  },
  onTimeChange(e) {
    this.setData({ enterTime: e.detail.value })
  },
  onDurationChange(e) {
    const duration = parseInt(e.detail.value) || 60
    this.setData({
      duration,
      durationText: Math.floor(duration / 60) + '小时' + (duration % 60 ? (duration % 60) + '分' : ''),
      estimatedFee: Math.ceil(duration / 60) * 5,
    })
  },
  onSubmit() {
    if (!this.data.enterDate || !this.data.enterTime) {
      wx.showToast({ title: '请选择预计入场日期和时间', icon: 'none' })
      return
    }
    const token = wx.getStorageSync('token')
    if (!token) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      setTimeout(() => wx.switchTab({ url: '/pages/mine/mine' }), 1500)
      return
    }

    const planEnterTime = this.data.enterDate + ' ' + this.data.enterTime

    wx.showLoading({ title: '预约中...' })
    api.reserve({
      slotId: this.data.slotId,
      planEnterTime: planEnterTime,
      planDuration: this.data.duration,
    }).then(order => {
      wx.hideLoading()
      wx.showToast({ title: '预约成功，请支付', icon: 'success' })
      // 跳转到订单页，让用户手动支付
      setTimeout(() => wx.switchTab({ url: '/pages/order/order' }), 1500)
    }).catch(err => {
      wx.hideLoading()
      wx.showToast({ title: err.message || '预约失败', icon: 'none' })
    })
  },
})
