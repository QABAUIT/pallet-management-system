import { message } from 'antd';

// Dùng thống nhất 1 chỗ để hiện thông báo, tránh mỗi người tự import message
// và viết text khác nhau cho cùng 1 tình huống.
export const notifySuccess = (msg) => message.success(msg || 'Thao tác thành công!');
export const notifyError = (err) =>
  message.error(err?.message || err || 'Có lỗi xảy ra, vui lòng thử lại!');
