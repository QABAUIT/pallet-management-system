-- V7: Định mức vật tư (BOM) + phiếu đóng pallet tái chế

CREATE TABLE dinh_muc (
    id                      BIGSERIAL PRIMARY KEY,
    ma_dinh_muc             VARCHAR(20) NOT NULL UNIQUE,
    ten_dinh_muc            VARCHAR(150) NOT NULL,
    mat_hang_thanh_pham_id  BIGINT NOT NULL REFERENCES mat_hang(id) ON DELETE RESTRICT,
    trang_thai              VARCHAR(20) NOT NULL DEFAULT 'hoat_dong'
                            CHECK (trang_thai IN ('hoat_dong','ngung')),
    ghi_chu                 VARCHAR(255),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE chi_tiet_dinh_muc (
    id            BIGSERIAL PRIMARY KEY,
    dinh_muc_id   BIGINT NOT NULL REFERENCES dinh_muc(id) ON DELETE CASCADE,
    mat_hang_id   BIGINT NOT NULL REFERENCES mat_hang(id) ON DELETE RESTRICT,
    so_luong      INTEGER NOT NULL CHECK (so_luong > 0),   -- vật tư cho 1 pallet
    CONSTRAINT uq_chi_tiet_dinh_muc UNIQUE (dinh_muc_id, mat_hang_id)
);

CREATE TABLE phieu_dong_pallet (
    id                      BIGSERIAL PRIMARY KEY,
    ma_phieu                VARCHAR(20) NOT NULL UNIQUE,
    dinh_muc_id             BIGINT NOT NULL REFERENCES dinh_muc(id) ON DELETE RESTRICT,
    mat_hang_thanh_pham_id  BIGINT NOT NULL REFERENCES mat_hang(id) ON DELETE RESTRICT,
    kho_id                  BIGINT NOT NULL REFERENCES kho(id) ON DELETE RESTRICT,
    nhan_vien_thuc_hien_id  BIGINT NOT NULL REFERENCES nhan_vien(id) ON DELETE RESTRICT,
    nguoi_duyet_id          BIGINT REFERENCES nhan_vien(id) ON DELETE SET NULL,
    so_luong_san_xuat       INTEGER NOT NULL CHECK (so_luong_san_xuat > 0),
    ngay_thuc_hien          DATE NOT NULL DEFAULT CURRENT_DATE,
    trang_thai              VARCHAR(20) NOT NULL DEFAULT 'nhap'
                            CHECK (trang_thai IN ('nhap','da_duyet','da_huy')),
    thoi_gian_duyet         TIMESTAMP,
    ly_do_huy               VARCHAR(255),
    ghi_chu                 VARCHAR(255),
    created_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_dong_pallet_da_duyet CHECK (
        trang_thai <> 'da_duyet' OR (nguoi_duyet_id IS NOT NULL AND thoi_gian_duyet IS NOT NULL))
);

CREATE TABLE chi_tiet_dong_pallet (
    id                    BIGSERIAL PRIMARY KEY,
    phieu_dong_pallet_id  BIGINT NOT NULL REFERENCES phieu_dong_pallet(id) ON DELETE CASCADE,
    mat_hang_id           BIGINT NOT NULL REFERENCES mat_hang(id) ON DELETE RESTRICT,
    so_luong_dinh_muc     INTEGER NOT NULL CHECK (so_luong_dinh_muc >= 0),
    so_luong_thuc_te      INTEGER NOT NULL CHECK (so_luong_thuc_te >= 0),
    chenh_lech            INTEGER GENERATED ALWAYS AS (so_luong_thuc_te - so_luong_dinh_muc) STORED,
    ly_do_chenh_lech      VARCHAR(255),
    CONSTRAINT uq_chi_tiet_dong_pallet UNIQUE (phieu_dong_pallet_id, mat_hang_id),
    -- TC12: có chênh lệch thì phải có lý do, không được âm thầm bỏ qua
    CONSTRAINT ck_chenh_lech_co_ly_do CHECK (
        so_luong_thuc_te = so_luong_dinh_muc OR ly_do_chenh_lech IS NOT NULL)
);

CREATE INDEX idx_phieu_dong_pallet_kho ON phieu_dong_pallet(kho_id, trang_thai);

CREATE TRIGGER trg_block_delete_dinh_muc
    BEFORE DELETE ON dinh_muc FOR EACH ROW EXECUTE FUNCTION fn_block_hard_delete();
CREATE TRIGGER trg_set_updated_at_dinh_muc
    BEFORE UPDATE ON dinh_muc FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_block_delete_phieu_dong_pallet
    BEFORE DELETE ON phieu_dong_pallet FOR EACH ROW EXECUTE FUNCTION fn_block_hard_delete();
CREATE TRIGGER trg_set_updated_at_phieu_dong_pallet
    BEFORE UPDATE ON phieu_dong_pallet FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- Định mức mẫu: 1 pallet tái chế = 6 thanh gỗ nguyên + 9 cục gỗ vuông (số demo, sửa được qua API)
INSERT INTO dinh_muc (ma_dinh_muc, ten_dinh_muc, mat_hang_thanh_pham_id, ghi_chu)
SELECT 'DM-MAC-DINH', 'Định mức pallet tái chế tiêu chuẩn', id, 'Định mức mẫu'
FROM mat_hang WHERE ma_mat_hang = 'PL-TC-08';

INSERT INTO chi_tiet_dinh_muc (dinh_muc_id, mat_hang_id, so_luong)
SELECT d.id, m.id, v.sl
FROM dinh_muc d
JOIN (VALUES ('LK-TN-09', 6), ('LK-CV-10', 9)) AS v(ma, sl) ON TRUE
JOIN mat_hang m ON m.ma_mat_hang = v.ma
WHERE d.ma_dinh_muc = 'DM-MAC-DINH';