const api = require('../../utils/api')

Page({
  data: {
    parkingId: null,
    parkingInfo: null,
    slots: [],
  },
  onLoad(options) {
    this.setData({ parkingId: options.id })
    this.loadDetail()
    this.loadSlots()
  },
  loadDetail() {
    api.getParkingDetail(this.data.parkingId)
      .then(info => this.setData({ parkingInfo: info }))
  },
  loadSlots() {
    api.getParkingSlots(this.data.parkingId)
      .then(slots => this.setData({ slots: slots || [] }))
  },
  onSlotTap(e) {
    const slot = e.currentTarget.dataset.slot
    if (slot.status !== 0) return
    wx.navigateTo({
      url: `/pages/booking/booking?slotId=${slot.id}&slotNumber=${slot.slotNumber}&lotName=${this.data.parkingInfo?.name || ''}`,
    })
  },
})
