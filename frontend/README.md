# PALLET — Frontend

Giao diện quản trị hệ thống pallet, xây dựng bằng **React 19 + Vite 8 + Ant Design 6**.

## Yêu cầu

- Node.js 20+ (khuyến nghị 22/24)
- Backend Spring Boot đang chạy tại `http://localhost:8080`

## Cài đặt & chạy

```bash
cd frontend
copy .env.example .env      # Windows
# cp .env.example .env      # macOS/Linux
npm install
npm run dev
```

Mặc định mở `http://localhost:5173`.

### Biến môi trường

| Biến | Mô tả | Mặc định |
| --- | --- | --- |
| `VITE_API_BASE_URL` | URL gốc API backend | `http://localhost:8080/api/v1` |

> Không đặt tài khoản/mật khẩu demo trong `.env`. Tài khoản mẫu nằm trong migration
> `V2__insert_master_data.sql` của backend (chỉ dùng cho môi trường dev).

## Scripts

| Lệnh | Chức năng |
| --- | --- |
| `npm run dev` | Chạy dev server có HMR |
| `npm run build` | Build production vào `dist/` |
| `npm run preview` | Xem thử bản build |
| `npm run lint` | Chạy Oxlint |
| `npm test` | Unit test các hàm tiện ích bằng `node --test` |

## Cấu trúc chính

```
src/
  api/          # Gọi API, axios client + tự refresh access token
  components/   # UI dùng chung, layout, modal, bảng...
  hooks/        # Logic tải dữ liệu / lọc / phân trang
  pages/        # Trang theo từng module
  routes/       # Khai báo route + ProtectedRoute
  store/        # AuthContext, NotificationContext, ThemeContext, tokenStore
  utils/        # Hàm thuần (matchTime, format tiền, CSV...)
tests/          # Unit test chạy bằng Node test runner
```

## Ghi chú kỹ thuật

- **Access token** gửi qua header `Authorization: Bearer ...`. Không dùng query param.
- **Refresh token** được backend rotate mỗi lần refresh; `apiClient` tự xếp hàng các
  request 401 trong lúc refresh và retry sau khi có token mới.
- **Thông báo realtime (SSE)** dùng `fetch` + `Authorization` header thay cho
  `EventSource` (EventSource không gửi được header). Khi mất kết nối sẽ tự thử lại
  với backoff 3s → 30s.
- **Badge thông báo** được reset khi đăng xuất.
- Menu sidebar ẩn/hiện theo `maVaiTro` (`GD`, `PGD`, `NVKHO`, `NVBANHANG`); backend
  vẫn là nơi chặn quyền thật bằng `@PreAuthorize`.

## Kiểm tra trước khi commit

```bash
npm run lint
npm test
npm run build
```
