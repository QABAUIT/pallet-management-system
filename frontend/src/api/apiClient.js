import axios from 'axios';

// Lấy từ file .env (Vite) - KHÔNG hardcode URL trong code để mỗi người
// chạy backend port khác nhau vẫn dùng chung 1 file .env riêng của máy mình.
const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

const apiClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Nếu sau này có đăng nhập (JWT), gắn token vào mọi request tại đây -
// chỉ cần sửa 1 chỗ này, không ai phải tự thêm header ở từng service.
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Toàn bộ response BE đều bọc trong ApiResponse<T> = { status, message, data, timestamp }.
// Interceptor này tự "bóc vỏ", nơi gọi chỉ nhận thẳng phần `data`.
apiClient.interceptors.response.use(
  (response) => response.data.data ?? response.data,
  (error) => {
    const message = error.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại!';
    return Promise.reject(new Error(message));
  }
);

export default apiClient;
