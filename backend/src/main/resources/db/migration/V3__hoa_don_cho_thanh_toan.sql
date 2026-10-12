ALTER TABLE hoa_don DROP CONSTRAINT IF EXISTS hoa_don_trang_thai_check;
ALTER TABLE hoa_don ADD CONSTRAINT hoa_don_trang_thai_check
    CHECK (trang_thai IN ('nhap','cho_thanh_toan','cho_duyet','da_thanh_toan','qua_han','da_huy'));