import React, { useState, useEffect } from 'react';
import { Table, Card, Button, Input, Tag, Space, Typography, Avatar, Form, message, Popconfirm, Select } from 'antd';
import { SearchOutlined, PlusOutlined, EditOutlined, UserOutlined, DeleteOutlined } from '@ant-design/icons';
import dayjs from 'dayjs';
import { nhanVienService } from '../services/nhanVienService';
import EmployeeStats from '../components/employee/EmployeeStats';
import EmployeeModal from '../components/employee/EmployeeModal';
import EmployeeFilterBar from '../components/employee/EmployeeFilterBar';
import { getEmployeeColumns } from '../components/employee/EmployeeTableColumns';

const { Title, Text } = Typography;
const { Option } = Select;

export default function EmployeeManagement() {
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState('');
  const [filterRole, setFilterRole] = useState(null);
  const [filterStatus, setFilterStatus] = useState(null);

  // Modal state
  const [isModalVisible, setIsModalVisible] = useState(false);
  const [modalMode, setModalMode] = useState('add'); // 'add' or 'edit'
  const [editingId, setEditingId] = useState(null);
  const [form] = Form.useForm();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const response = await nhanVienService.getAll();
      const list = response?.data ? response.data : Array.isArray(response) ? response : [];
      setData(list);
    } catch (error) {
      console.error('Lỗi khi tải dữ liệu nhân viên:', error);
      message.error('Không thể tải danh sách nhân viên');
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setModalMode('add');
    setEditingId(null);
    form.resetFields();
    setIsModalVisible(true);
  };

  const handleEdit = (record) => {
    setModalMode('edit');
    setEditingId(record.id);
    form.setFieldsValue({
      maNv: record.maNv,
      hoTen: record.hoTen,
      vaiTroId: record.vaiTroId,
      sdt: record.sdt,
      diaChi: record.diaChi,
      cccd: record.cccd || '',
      email: record.email || '',
      tenDangNhap: record.tenDangNhap,
      chucVu: record.chucVu,
      ngayVaoLam: record.ngayVaoLam ? dayjs(record.ngayVaoLam) : null,
      ngaySinh: record.ngaySinh ? dayjs(record.ngaySinh) : null,
      trangThai: record.trangThai || 'dang_lam_viec'
    });
    setIsModalVisible(true);
  };

  const handleDelete = async (id) => {
    try {
      await nhanVienService.remove(id);
      message.success('Xóa nhân viên thành công');
      fetchData();
    } catch (error) {
      message.error(error.message || 'Lỗi khi xóa nhân viên');
    }
  };

  const handleModalSubmit = async (values) => {
    try {
      // Dùng values từ tham số onFinish để luôn đảm bảo có data mới nhất
      const payload = {
        ...values,
        // Chuyển đổi dayjs object thành chuỗi YYYY-MM-DD cho backend
        ngayVaoLam: values.ngayVaoLam ? values.ngayVaoLam.format('YYYY-MM-DD') : null,
        ngaySinh: values.ngaySinh ? values.ngaySinh.format('YYYY-MM-DD') : null,
        // Dùng tên đăng nhập cũ nếu có, nếu không thì lấy sdt hoặc tạo ngẫu nhiên
        tenDangNhap: values.tenDangNhap || form.getFieldValue('tenDangNhap') || (values.sdt ? values.sdt : `nv${Math.floor(Math.random() * 100000)}`),
        // Mật khẩu mặc định khi tạo mới
        matKhau: modalMode === 'add' ? 'Password@123' : undefined,
      };

      if (modalMode === 'add') {
        await nhanVienService.create(payload);
        message.success('Thêm nhân viên thành công');
      } else {
        await nhanVienService.update(editingId, payload);
        message.success('Cập nhật nhân viên thành công');
      }

      setIsModalVisible(false);
      fetchData();
    } catch (error) {
      if (error.errorFields) {
        return; // Lỗi validate form
      }
      console.error(error);
      message.error(error.message || 'Có lỗi xảy ra khi lưu nhân viên');
    }
  };

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

  return (
    <div style={{ padding: '24px', backgroundColor: '#fcfcfc', minHeight: '100vh', textAlign: 'left' }}>
      <div style={{ marginBottom: '24px' }}>
        <Title level={3} style={{ margin: 0, fontWeight: 600 }}>Quản lí nhân viên</Title>
        <Text type="secondary">Danh sách nhân sự, phân quyền vai trò vận hành và thông tin liên hệ</Text>
      </div>

      <EmployeeStats data={data} />

      <EmployeeFilterBar 
        searchText={searchText}
        setSearchText={setSearchText}
        filterStatus={filterStatus}
        setFilterStatus={setFilterStatus}
        filterRole={filterRole}
        setFilterRole={setFilterRole}
        handleAdd={handleAdd}
      />

      <Card bodyStyle={{ padding: 0 }} style={{ borderRadius: '8px', overflow: 'hidden', border: '1px solid #f0f0f0' }}>
        <Table
          columns={columns}
          dataSource={filteredData}
          loading={loading}
          rowKey="id"
          pagination={{
            total: filteredData.length,
            showTotal: (total, range) => `Hiển thị ${range[0]} - ${range[1]} trên tổng số ${total} nhân viên`,
            defaultPageSize: 5,
            showSizeChanger: true
          }}
        />
      </Card>

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