import React, { useState } from 'react'
import { Form, Input, Button, Checkbox, Card, Typography, Alert } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import { useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../../store/AuthContext'

const { Title, Text } = Typography

// Đặt true để bấm Đăng nhập bỏ qua kiểm tra/API
const IS_DEV_MODE = true

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [loi, setLoi] = useState(null)
  const [dangTai, setDangTai] = useState(false)

  const onFinish = async (values) => {
    setLoi(null)
    setDangTai(true)

    try {
      if (IS_DEV_MODE) {
        console.log('DEV MODE: Bỏ qua API login')

        const veTrang = location.state?.from || '/'

        navigate(veTrang, { replace: true })

        return
      }

      // Production: login thật
      await login(
        values.tenDangNhap,
        values.matKhau,
        values.ghiNhoDangNhap ?? false
      )

      const veTrang = location.state?.from || '/'

      navigate(veTrang, { replace: true })

    } catch (err) {
      setLoi(err.message || 'Đăng nhập thất bại')
    } finally {
      setDangTai(false)
    }
  }


  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%)',
      }}
    >
      <Card style={{ width: 380, boxShadow: '0 8px 24px rgba(0,0,0,0.12)' }}>
        <div style={{ textAlign: 'center', marginBottom: 24 }}>
          <Title level={3} style={{ marginBottom: 0, color: '#e08e1a' }}>
            HOÀNG PHÁT PALLET
          </Title>
          <Text type="secondary">
            Đăng nhập hệ thống quản lý {IS_DEV_MODE && <Text type="danger">(DEV MODE)</Text>}
          </Text>
        </div>

        {loi && <Alert type="error" message={loi} showIcon style={{ marginBottom: 16 }} />}

        <Form layout="vertical" onFinish={onFinish} autoComplete="off">
          <Form.Item name="tenDangNhap" label="Tên đăng nhập">
            <Input prefix={<UserOutlined />} placeholder="Để trống hoặc nhập bất kỳ" size="large" />
          </Form.Item>

          <Form.Item name="matKhau" label="Mật khẩu">
            <Input.Password prefix={<LockOutlined />} placeholder="••••••••" size="large" />
          </Form.Item>

          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
            <Form.Item name="ghiNhoDangNhap" valuePropName="checked" noStyle>
              <Checkbox>Ghi nhớ đăng nhập</Checkbox>
            </Form.Item>
            <a href="/quen-mat-khau">Quên mật khẩu?</a>
          </div>

          <Form.Item>
            <Button type="primary" htmlType="submit" block size="large" loading={dangTai}>
              {IS_DEV_MODE ? 'Đăng nhập (Skip Dev)' : 'Đăng nhập'}
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}