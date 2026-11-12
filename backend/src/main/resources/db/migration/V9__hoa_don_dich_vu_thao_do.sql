-- V9: Hóa đơn dịch vụ tháo dỡ + mua lại phế liệu của khách

-- 1. Loại mặt hàng "dich_vu" (không có tồn kho)
ALTER TABLE mat_hang DROP CONSTRAINT mat_hang_loai_mat_hang_check;
ALTER TABLE mat_hang ADD CONSTRAINT mat_hang_loai_mat_hang_check
    CHECK (loai_mat_hang IN ('pallet','linh_kien','cho_tai_che','phe_lieu','dich_vu'));

INSERT INTO mat_hang (id, ma_mat_hang, ten_mat_hang, loai_mat_hang, don_gia_ban, mo_ta, trang_thai)
SELECT COALESCE(MAX(id), 0) + 1, 'DV-TD-99', 'Dịch vụ tháo dỡ pallet', 'dich_vu', 0,
       'Dịch vụ tháo dỡ pallet cho khách, tính theo số pallet đã tháo', 'dang_kinh_doanh'
FROM mat_hang;
SELECT setval('mat_hang_id_seq', (SELECT MAX(id) FROM mat_hang));

-- 2. Loại giao dịch mới trên hóa đơn (gồm cả loại bán phế liệu cho bước sau)
ALTER TABLE hoa_don DROP CONSTRAINT hoa_don_loai_giao_dich_check;
ALTER TABLE hoa_don ADD CONSTRAINT hoa_don_loai_giao_dich_check CHECK (loai_giao_dich IN (
    'thu_ban_pallet','thu_ban_linh_kien','thu_khac',
    'thu_dich_vu_thao_do','thu_ban_phe_lieu',
    'chi_mua_pallet_cu','chi_nguyen_lieu_phu_tro','chi_sua_chua','chi_van_chuyen','chi_khac',
    'chi_mua_phe_lieu_khach'));

ALTER TABLE hoa_don DROP CONSTRAINT chk_hoadon_doi_tuong;
ALTER TABLE hoa_don ADD CONSTRAINT chk_hoadon_doi_tuong CHECK (
    CASE
        WHEN loai_giao_dich IN ('thu_ban_pallet','thu_ban_linh_kien','thu_dich_vu_thao_do',
                                'thu_ban_phe_lieu','chi_mua_phe_lieu_khach')
            THEN (khach_hang_id IS NOT NULL AND ncc_id IS NULL)
        WHEN loai_giao_dich = 'chi_mua_pallet_cu'
            THEN (ncc_id IS NOT NULL AND khach_hang_id IS NULL)
        WHEN loai_giao_dich IN ('chi_nguyen_lieu_phu_tro','chi_sua_chua','chi_van_chuyen')
            THEN (khach_hang_id IS NULL)
        ELSE (khach_hang_id IS NULL AND ncc_id IS NULL)
    END);

-- 3. Liên kết phiếu dịch vụ với hóa đơn + theo dõi phế liệu công ty đã mua lại
ALTER TABLE phieu_dich_vu_thao_do
    ADD COLUMN hoa_don_id BIGINT UNIQUE REFERENCES hoa_don(id) ON DELETE RESTRICT,
    ADD COLUMN kg_phe_lieu_da_mua INTEGER NOT NULL DEFAULT 0 CHECK (kg_phe_lieu_da_mua >= 0);

ALTER TABLE phieu_dich_vu_thao_do
    ADD CONSTRAINT ck_dvtd_kg_da_mua CHECK (kg_phe_lieu_da_mua <= kg_phe_lieu);

CREATE TABLE mua_lai_phe_lieu_khach (
    id                    BIGSERIAL PRIMARY KEY,
    phieu_dich_vu_id      BIGINT NOT NULL REFERENCES phieu_dich_vu_thao_do(id) ON DELETE RESTRICT,
    hoa_don_id            BIGINT NOT NULL REFERENCES hoa_don(id) ON DELETE RESTRICT,
    phieu_kho_id          BIGINT NOT NULL REFERENCES phieu_kho(id) ON DELETE RESTRICT,
    nhan_vien_id          BIGINT NOT NULL REFERENCES nhan_vien(id) ON DELETE RESTRICT,
    so_kg                 INTEGER NOT NULL CHECK (so_kg > 0),
    don_gia_kg            NUMERIC(18,2) NOT NULL CHECK (don_gia_kg > 0),
    created_at            TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TRIGGER trg_block_delete_mua_lai_phe_lieu
    BEFORE DELETE ON mua_lai_phe_lieu_khach FOR EACH ROW EXECUTE FUNCTION fn_block_hard_delete();