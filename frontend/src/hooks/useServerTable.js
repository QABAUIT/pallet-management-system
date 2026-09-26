import { useCallback, useEffect, useState } from 'react';
import { notifyError } from '../utils/notify';

/**
 * Hook dùng chung cho MỌI bảng danh sách có phân trang phía server (khớp
 * với Page<T> của Spring Data: content, totalElements, number, size).
 *
 * Dùng trong 1 trang:
 *   const table = useServerTable((params) => khachHangService.getAll(params));
 *   <Table
 *     dataSource={table.data}
 *     loading={table.loading}
 *     rowKey="id"
 *     pagination={table.pagination}
 *     onChange={table.handleTableChange}
 *   />
 *
 * @param fetchFn (params) => Promise<PageResponse> - thường là service.getAll
 * @param extraParams object các tham số lọc thêm (mã, trạng thái, khoảng ngày...),
 *        đổi giá trị object này (state ở component cha) sẽ tự fetch lại.
 */
export function useServerTable(fetchFn, extraParams = {}) {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [pageIndex, setPageIndex] = useState(0); // 0-based, khớp Spring Data
  const [pageSize, setPageSize] = useState(10);
  const [total, setTotal] = useState(0);
  const [sortField, setSortField] = useState(undefined);
  const [sortOrder, setSortOrder] = useState(undefined);

  const load = useCallback(() => {
    setLoading(true);
    const params = {
      page: pageIndex,
      size: pageSize,
      ...(sortField ? { sort: `${sortField},${sortOrder === 'descend' ? 'desc' : 'asc'}` } : {}),
      ...extraParams,
    };
    fetchFn(params)
      .then((pageResponse) => {
        setData(pageResponse?.content ?? []);
        setTotal(pageResponse?.totalElements ?? 0);
      })
      .catch((err) => notifyError(err))
      .finally(() => setLoading(false));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [pageIndex, pageSize, sortField, sortOrder, JSON.stringify(extraParams)]);

  useEffect(() => {
    load();
  }, [load]);

  // antd Table.onChange trả (pagination, filters, sorter) - antd dùng
  // pagination.current 1-based nên phải trừ 1 để khớp Spring Data (0-based).
  const handleTableChange = (paginationInfo, _filters, sorter) => {
    setPageIndex((paginationInfo.current || 1) - 1);
    setPageSize(paginationInfo.pageSize || 10);
    setSortField(sorter?.field);
    setSortOrder(sorter?.order);
  };

  return {
    data,
    loading,
    pagination: {
      current: pageIndex + 1,
      pageSize,
      total,
      showSizeChanger: true,
      showTotal: (t) => `Tổng ${t} bản ghi`,
    },
    handleTableChange,
    reload: load,
  };
}
