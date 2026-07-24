const api = require('../../utils/api')

Page({
  data: {
    parkingList: [],
    loading: false,
  },
  onLoad() {
    this.loadParkingList()
  },
  onShow() {
    // 每次显示页面时刷新
    this.loadParkingList()
  },
  loadParkingList() {
    this.setData({ loading: true })
    const params = {}
    // 尝试获取用户位置
    wx.getLocation({
      type: 'gcj02',
      success: (res) => {
        params.longitude = res.longitude
        params.latitude = res.latitude
        this.doLoadList(params)
      },
      fail: () => {
        // 使用默认坐标：大连理工大学
        params.longitude = 121.532
        params.latitude = 38.877
        this.doLoadList(params)
      }
    })
  },
  doLoadList(params) {
    api.getParkingList(params)
      .then(list => this.setData({ parkingList: list || [] }))
      .catch(() => {})
      .finally(() => this.setData({ loading: false }))
  },
  onParkingTap(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: `/pages/parking/parking?id=${id}` })
  },
  onRefresh() {
    this.loadParkingList()
  },
})
