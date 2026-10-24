-- V5: Phiếu tháo dỡ (từng đợt) cho pallet gỗ cũ đã phân loại vào nhóm "tháo dỡ"

CREATE TABLE phieu_thao_do (
    id                       BIGSERIAL PRIMARY KEY,
    ma_phieu                 VARCHAR(20) NOT NULL UNIQUE,
    phieu_tai_che_id         BIGINT NOT NULL REFERENCES phieu_tai_che(id) ON DELETE RESTRICT,
    kho_id                   BIGINT NOT NULL REFERENCES kho(id) ON DELETE RESTRICT,
    nhan_vien_thuc_hien_id   BIGINT NOT NULL REFERENCES nhan_vien(id) ON DELETE RESTRICT,
    nguoi_duyet_id           BIGINT REFERENCES nhan_vien(id) ON DELETE SET NULL,
    so_pallet_thao_do        INTEGER NOT NULL CHECK (so_pallet_thao_do > 0),
    so_thanh_nguyen          INTEGER NOT NULL DEFAULT 0 CHECK (so_thanh_nguyen >= 0),
    so_cuc_vuong             INTEGER NOT NULL DEFAULT 0 CHECK (so_cuc_vuong >= 0),
    so_thanh_can_xu_ly       INTEGER NOT NULL DEFAULT 0 CHECK (so_thanh_can_xu_ly >= 0),
    so_cuc_can_xu_ly         INTEGER NOT NULL DEFAULT 0 CHECK (so_cuc_can_xu_ly >= 0),
    kg_phe_lieu              INTEGER NOT NULL DEFAULT 0 CHECK (kg_phe_lieu >= 0),
    ngay_thuc_hien           DATE NOT NULL DEFAULT CURRENT_DATE,
    trang_thai               VARCHAR(20) NOT NULL DEFAULT 'nhap'
                             CHECK (trang_thai IN ('nhap','da_duyet','da_huy')),
    thoi_gian_duyet          TIMESTAMP,
    ly_do_huy                VARCHAR(255),
    ghi_chu                  VARCHAR(255),
    created_at               TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMP NOT NULL DEFAULT NOW(),
    -- TC10: mỗi đợt tháo dỡ phải thu được ít nhất một thứ, không tạo linh kiện giả
    CONSTRAINT ck_thao_do_co_ket_qua CHECK (
        so_thanh_nguyen + so_cuc_vuong + so_thanh_can_xu_ly + so_cuc_can_xu_ly + kg_phe_lieu > 0),
    CONSTRAINT ck_thao_do_da_duyet_co_nguoi_duyet CHECK (
        trang_thai <> 'da_duyet' OR (nguoi_duyet_id IS NOT NULL AND thoi_gian_duyet IS NOT NULL))
);

CREATE INDEX idx_phieu_thao_do_phieu_tai_che ON phieu_thao_do(phieu_tai_che_id);

CREATE TRIGGER trg_block_delete_phieu_thao_do
    BEFORE DELETE ON phieu_thao_do
    FOR EACH ROW EXECUTE FUNCTION fn_block_hard_delete();

CREATE TRIGGER trg_set_updated_at_phieu_thao_do
    BEFORE UPDATE ON phieu_thao_do
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();