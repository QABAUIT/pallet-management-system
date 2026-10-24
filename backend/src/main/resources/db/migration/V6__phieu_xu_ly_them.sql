-- V6: Phiếu xử lý hàng "cần xử lý thêm" (thanh/cục) -> dùng lại được hoặc phế liệu

CREATE TABLE phieu_xu_ly_them (
    id                       BIGSERIAL PRIMARY KEY,
    ma_phieu                 VARCHAR(20) NOT NULL UNIQUE,
    kho_id                   BIGINT NOT NULL REFERENCES kho(id) ON DELETE RESTRICT,
    nhan_vien_thuc_hien_id   BIGINT NOT NULL REFERENCES nhan_vien(id) ON DELETE RESTRICT,
    nguoi_duyet_id           BIGINT REFERENCES nhan_vien(id) ON DELETE SET NULL,
    loai_nguon               VARCHAR(10) NOT NULL CHECK (loai_nguon IN ('thanh','cuc')),
    so_dat_lai               INTEGER NOT NULL DEFAULT 0 CHECK (so_dat_lai >= 0),
    so_phe_lieu              INTEGER NOT NULL DEFAULT 0 CHECK (so_phe_lieu >= 0),
    kg_phe_lieu              INTEGER NOT NULL DEFAULT 0 CHECK (kg_phe_lieu >= 0),
    ly_do                    VARCHAR(255) NOT NULL,
    ngay_thuc_hien           DATE NOT NULL DEFAULT CURRENT_DATE,
    trang_thai               VARCHAR(20) NOT NULL DEFAULT 'nhap'
                             CHECK (trang_thai IN ('nhap','da_duyet','da_huy')),
    thoi_gian_duyet          TIMESTAMP,
    ly_do_huy                VARCHAR(255),
    created_at               TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_xu_ly_them_co_so_luong CHECK (so_dat_lai + so_phe_lieu > 0),
    -- TC10: kg phế liệu chỉ phát sinh khi có hàng bị bỏ, và có hàng bỏ thì phải có kg
    CONSTRAINT ck_xu_ly_them_kg CHECK ((so_phe_lieu > 0) = (kg_phe_lieu > 0)),
    CONSTRAINT ck_xu_ly_them_da_duyet CHECK (
        trang_thai <> 'da_duyet' OR (nguoi_duyet_id IS NOT NULL AND thoi_gian_duyet IS NOT NULL))
);

CREATE INDEX idx_phieu_xu_ly_them_kho ON phieu_xu_ly_them(kho_id, trang_thai);

CREATE TRIGGER trg_block_delete_phieu_xu_ly_them
    BEFORE DELETE ON phieu_xu_ly_them
    FOR EACH ROW EXECUTE FUNCTION fn_block_hard_delete();

CREATE TRIGGER trg_set_updated_at_phieu_xu_ly_them
    BEFORE UPDATE ON phieu_xu_ly_them
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();