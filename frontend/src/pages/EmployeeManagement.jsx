import React, { useState, useEffect } from 'react';
import { Button } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import EmployeeStats from '../components/employee/EmployeeStats';
import EmployeeModal from '../components/employee/EmployeeModal';
import { getEmployeeColumns } from '../components/employee/EmployeeTableColumns';
import { useEmployees } from '../hooks/useEmployees';

import PageHeader from '../components/common/PageHeader';
import SearchInput from '../components/common/SearchInput';
import OtherTypeFilter from '../components/common/OtherTypeFilter';
import AppTable from '../components/common/AppTable';
import Pagination from '../components/common/Pagination';

import '../components/layout/style.css';

export default function EmployeeManagement() {
  const {
    data,
    loading,
    isModalVisible,
    setIsModalVisible,
    modalMode,
    form,
    handleAdd,
    handleEdit,
    handleDelete,
    handleModalSubmit
  } = useEmployees();

  const [searchText, setSearchText] = useState('');
  const [filterRole, setFilterRole] = useState(null);
  const [filterStatus, setFilterStatus] = useState(null);
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 8;

  const columns = getEmployeeColumns(handleEdit, handleDelete);

  const filteredData = data.filter(item => {
    const matchSearch = item.hoTen?.toLowerCase().includes(searchText.toLowerCase()) ||
      item.maNv?.toLowerCase().includes(searchText.toLowerCase()) ||
      item.sdt?.includes(searchText);

    const matchStatus = filterStatus ?
      (item.trangThai === filterStatus ||
        (filterStatus === 'dang_lam_viec' && item.trangThai === 'Đang làm việc') ||
        (filterStatus === 'da_nghi_viec' && item.trangThai === 'Đã nghỉ việc')) : true;

    const matchRole = filterRole ? (item.tenVaiTro === filterRole) : true;

    return matchSearch && matchStatus && matchRole;
  });

  const paginatedData = filteredData.slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage);

  // Khi search/lọc thì quay về trang 1
  useEffect(() => {
    setCurrentPage(1);
  }, [searchText, filterStatus, filterRole]);

  return (
    <div style={{ padding: '24px', backgroundColor: '#fcfcfc', minHeight: '100vh', textAlign: 'left' }}>
      
      <EmployeeStats data={data} />

      <div style={{ display: 'flex', gap: '16px', marginBottom: '16px', alignItems: 'center' }}>
        <SearchInput 
          value={searchText} 
          onChange={setSearchText} 
          placeholder="Tìm theo mã NV, họ tên, số điện thoại..." 
        />
        <OtherTypeFilter
          label="Lọc nhân viên"
          groups={[
            {
              key: 'status',
              label: 'Trạng thái',
              type: 'radio',
              options: [
                { value: 'dang_lam_viec', label: 'Đang làm việc' },
                { value: 'da_nghi_viec', label: 'Đã nghỉ việc' }
              ]
            },
            {
              key: 'role',
              label: 'Vai trò',
              type: 'radio',
              options: [
                { value: 'Giám đốc', label: 'Giám đốc' },
                { value: 'Phó giám đốc', label: 'Phó giám đốc' },
                { value: 'Nhân viên kho', label: 'Nhân viên kho' },
                { value: 'Nhân viên bán hàng', label: 'Nhân viên bán hàng' }
              ]
            }
          ]}
          value={{ status: filterStatus, role: filterRole }}
          defaultValue={{ status: null, role: null }}
          onApply={(val) => {
            setFilterStatus(val.status);
            setFilterRole(val.role);
          }}
        />
          <Button
            type="primary"
            icon={<PlusOutlined />}
            size="large"
            style={{ backgroundColor: '#d46b08', borderColor: '#d46b08', borderRadius: '6px', fontWeight: 500 }}
            onClick={handleAdd}
          >
            Thêm nhân viên mới
          </Button>
      </div>

      <AppTable
        columns={columns}
        data={paginatedData}
        rowKey={(row) => row.id}
        emptyText={loading ? "Đang tải dữ liệu..." : "Không có dữ liệu"}
      />

      <Pagination
        currentPage={currentPage}
        totalItems={filteredData.length}
        itemLabel="nhân viên"
        itemsPerPage={itemsPerPage}
        onPageChange={setCurrentPage}
      />

      <EmployeeModal
        isModalVisible={isModalVisible}
        setIsModalVisible={setIsModalVisible}
        modalMode={modalMode}
        form={form}
        handleModalSubmit={handleModalSubmit}
      />
    </div>
  );
}