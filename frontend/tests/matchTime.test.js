import test from 'node:test'
import assert from 'node:assert/strict'
import { matchTime, toIsoDate, getTimeRange, nhanKy } from '../src/utils/matchTime.js'

const homNay = new Date()

test('matchTime: preset năm chỉ khớp năm hiện tại', () => {
  assert.equal(matchTime(homNay, { preset: 'year' }), true)
  assert.equal(matchTime(new Date(homNay.getFullYear() - 1, 0, 1), { preset: 'year' }), false)
})

test('matchTime: preset tháng chỉ khớp tháng hiện tại', () => {
  assert.equal(matchTime(homNay, { preset: 'month' }), true)
  const thangTruoc = new Date(homNay.getFullYear(), homNay.getMonth() - 1, 15)
  assert.equal(matchTime(thangTruoc, { preset: 'month' }), false)
})

test('matchTime: preset quý khớp đúng quý', () => {
  const quy = Math.floor(homNay.getMonth() / 3)
  const thangTrongQuy = new Date(homNay.getFullYear(), quy * 3, 1)
  assert.equal(matchTime(thangTrongQuy, { preset: 'quarter' }), true)
})

test('matchTime: custom lọc theo khoảng ngày', () => {
  const time = {
    preset: 'custom',
    from: new Date(2026, 0, 1),
    to: new Date(2026, 0, 31),
  }
  assert.equal(matchTime(new Date(2026, 0, 15), time), true)
  assert.equal(matchTime(new Date(2026, 1, 1), time), false)
})

test('matchTime: custom thiếu đầu ngày thì không lọc', () => {
  assert.equal(matchTime(new Date(2020, 0, 1), { preset: 'custom', from: null, to: null }), true)
})

test('toIsoDate trả về yyyy-MM-dd theo giờ địa phương', () => {
  assert.equal(toIsoDate(new Date(2026, 8, 5)), '2026-09-05')
})

test('getTimeRange trả về đúng khoảng tháng', () => {
  const now = new Date(2026, 9, 20)
  const range = getTimeRange({ preset: 'month' }, now)
  assert.equal(toIsoDate(range.from), '2026-10-01')
  assert.equal(toIsoDate(range.to), '2026-10-31')
})

test('getTimeRange trả null với custom chưa chọn đủ ngày', () => {
  assert.equal(getTimeRange({ preset: 'custom', from: null, to: null }), null)
})

test('nhanKy hiển thị nhãn tháng/năm', () => {
  const time = { preset: 'month' }
  const range = { from: new Date(2026, 9, 1), to: new Date(2026, 9, 31) }
  assert.equal(nhanKy(time, range), 'T10/2026')
})
