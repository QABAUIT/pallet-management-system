import { useMemo, useState } from 'react'
import { useServerTable } from './useServerTable'
import { notifyError } from '../utils/notify'
import { downloadBlob } from '../utils/downloadFile'
import { toApiDateTime } from '../utils/format'

/**
 * Logic chung cho 2 trang Lịch sử nhập / xuất kho: tìm kiếm (Enter), lọc trạng thái,
 * lọc khoảng ngày, phân trang phía server, xuất Excel theo đúng bộ lọc đang chọn.
 */
export function usePhieuKhoList({ fetchList, exportExcel, fileName }) {
  const [keyword, setKeyword] = useState('') // chữ đang gõ
  const [searchTerm, setSearchTerm] = useState('') // chữ đã áp dụng (Enter)
  const [trangThai, setTrangThai] = useState(undefined)
  const [range, setRange] = useState(null) // [dayjs, dayjs] | null
  const [exporting, setExporting] = useState(false)

  const extraParams = useMemo(
    () => ({
      keyword: searchTerm || undefined,
      trangThai: trangThai || undefined,
      tuNgay: range?.[0] ? toApiDateTime(range[0].startOf('day')) : undefined,
      denNgay: range?.[1] ? toApiDateTime(range[1].endOf('day')) : undefined,
    }),
    [searchTerm, trangThai, range],
  )

  const table = useServerTable(fetchList, extraParams)
  const { pageSize } = table.pagination
  const goToFirstPage = () => table.handleTableChange({ current: 1, pageSize })

  const handleSearch = () => {
    setSearchTerm(keyword.trim())
    goToFirstPage()
  }

  const handleKeywordChange = (value) => {
    setKeyword(value)
    if (value === '' && searchTerm !== '') {
      setSearchTerm('')
      goToFirstPage()
    }
  }

  const handleTrangThaiChange = (value) => {
    setTrangThai(value)
    goToFirstPage()
  }

  const handleRangeChange = (value) => {
    setRange(value && value[0] && value[1] ? value : null)
    goToFirstPage()
  }

  const handleExport = async () => {
    setExporting(true)
    try {
      const blob = await exportExcel(extraParams)
      downloadBlob(blob, fileName)
    } catch (err) {
      notifyError(err)
    } finally {
      setExporting(false)
    }
  }

  return {
    table,
    keyword,
    trangThai,
    range,
    exporting,
    handleSearch,
    handleKeywordChange,
    handleTrangThaiChange,
    handleRangeChange,
    handleExport,
  }
}
