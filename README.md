# PALLET — Hệ thống quản trị pallet

Ứng dụng quản lý kho pallet gồm 2 phần:

| Thành phần | Công nghệ | Thư mục |
| --- | --- | --- |
| Backend | Spring Boot 4.1.1, Java 21, PostgreSQL, Flyway | [`backend/`](backend/) |
| Frontend | React 19, Vite 8, Ant Design 6 | [`frontend/`](frontend/) |

## Chạy nhanh

```bash
# 1. Database
cd backend
copy .env.example .env
docker compose up -d

# 2. Backend (terminal 1)
.\mvnw.cmd spring-boot:run

# 3. Frontend (terminal 2)
cd frontend
copy .env.example .env
npm install
npm run dev
```

Mở `http://localhost:5173`. Hướng dẫn chi tiết và biến môi trường xem:

- [`backend/HELP.md`](backend/HELP.md)
- [`frontend/README.md`](frontend/README.md)

## Kiểm tra

```bash
cd backend  && .\mvnw.cmd test
cd frontend && npm run lint && npm test && npm run build
```

## Lưu ý

- Không commit file `.env` hay thư mục `dist/`, `target/`, `node_modules/`.
- Không sửa migration Flyway đã chạy; thêm file `V13__...sql` mới khi đổi schema.
- Tài khoản mẫu trong migration `V2` chỉ dùng cho môi trường phát triển.
