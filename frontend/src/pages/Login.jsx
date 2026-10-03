import { useState } from 'react'
import { Alert, Button, Checkbox, ConfigProvider, Form, Input, message } from 'antd'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../store/AuthContext'
import bgLogin from '../assets/login-bg.jpg'
import '../components/layout/Login.css';

export default function LoginPage() {
  const { daDangNhap, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [dangGui, setDangGui] = useState(false)
  const [loi, setLoi] = useState('')

  const trangSauLogin = location.state?.from?.pathname || '/'

  if (daDangNhap) return <Navigate to={trangSauLogin} replace />

  const onFinish = async ({ tenDangNhap, matKhau, ghiNho }) => {
    setDangGui(true)
    setLoi('')
    try {
      await login(tenDangNhap.trim(), matKhau, !!ghiNho)
      navigate(trangSauLogin, { replace: true })
    } catch (err) {
      // err = { message, fieldErrors, status } do apiClient chuẩn hóa
      const chiTiet =
        err.fieldErrors && typeof err.fieldErrors === 'object'
          ? Object.values(err.fieldErrors)[0]
          : null
      setLoi(chiTiet || err.message)
    } finally {
      setDangGui(false)
    }
  }

  const quenMatKhau = () =>
    message.info('Vui lòng liên hệ Giám đốc hoặc Phó giám đốc để được cấp lại mật khẩu')

  return (
    // Ô checkbox trong thiết kế màu navy, kích thước 20px (khác màu chủ đạo cam của app)
    <ConfigProvider
      theme={{ token: { colorPrimary: '#0f2942', controlInteractiveSize: 20 } }}
    >
      <div className="login-page" style={{ '--login-bg': `url(${bgLogin})` }}>
        <div className="login-card">
          <h1 className="login-title">Đăng nhập hệ thống</h1>
          <p className="login-subtitle">Vui lòng nhập thông tin để tiếp tục</p>

          <Form
            className="login-form"
            layout="vertical"
            requiredMark={false}
            onFinish={onFinish}
            initialValues={{ ghiNho: false }}
          >
            <Form.Item
              className="login-field-user"
              name="tenDangNhap"
              label="Tên đăng nhập"
              rules={[{ required: true, whitespace: true, message: 'Vui lòng nhập tên đăng nhập' }]}
            >
              <Input placeholder="admin" autoFocus autoComplete="username" />
            </Form.Item>

            <Form.Item
              className="login-field-pass"
              name="matKhau"
              label={
                <span className="login-label-row">
                  <span>Mật khẩu</span>
                  <button type="button" className="login-link" onClick={quenMatKhau}>
                    Quên mật khẩu?
                  </button>
                </span>
              }
              rules={[{ required: true, message: 'Vui lòng nhập mật khẩu' }]}
            >
              <Input.Password
                placeholder="0123456789"
                autoComplete="current-password"
                visibilityToggle={false}
              />
            </Form.Item>

            <Form.Item className="login-field-remember" name="ghiNho" valuePropName="checked">
              <Checkbox>Ghi nhớ đăng nhập</Checkbox>
            </Form.Item>

            {loi && <Alert className="login-alert" type="error" message={loi} showIcon />}

            <Button className="login-btn" type="primary" htmlType="submit" loading={dangGui} block>
              Đăng nhập
            </Button>
          </Form>
        </div>

        <span className="login-brand">Út Xíu Pallet</span>
      </div>
    </ConfigProvider>
  )
}