ALTER TABLE phieu_kho ADD COLUMN la_he_thong BOOLEAN NOT NULL DEFAULT FALSE;

-- Đánh dấu phiếu cũ do hệ thống tự sinh (tái chế, tháo dỡ, xử lý thêm, đóng pallet, mua lại phế liệu)
UPDATE phieu_kho SET la_he_thong = TRUE
WHERE phieu_tai_che_id IS NOT NULL
   OR ghi_chu LIKE 'Xuất để phân loại theo phiếu tái chế%'
   OR ghi_chu LIKE 'Nhập kết quả phân loại theo phiếu tái chế%'
   OR ghi_chu LIKE 'Xuất pallet để tháo dỡ theo phiếu%'
   OR ghi_chu LIKE 'Nhập kết quả tháo dỡ theo phiếu%'
   OR ghi_chu LIKE 'Xuất hàng cần xử lý thêm theo phiếu%'
   OR ghi_chu LIKE 'Nhập kết quả xử lý thêm theo phiếu%'
   OR ghi_chu LIKE 'Xuất vật tư đóng pallet theo phiếu%'
   OR ghi_chu LIKE 'Nhập pallet tái chế thành phẩm theo phiếu%'
   OR ghi_chu LIKE 'Mua lại % kg phế liệu của khách%';
