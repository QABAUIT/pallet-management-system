-- V8: Dịch vụ tháo dỡ cho khách + sổ tồn hàng của khách (tách khỏi ton_kho công ty)

CREATE TABLE ton_kho_khach (
    id             BIGSERIAL PRIMARY KEY,
    khach_hang_id  BIGINT NOT NULL REFERENCES khach_hang(id) ON DELETE RESTRICT,
    mat_hang_id    BIGINT NOT NULL REFERENCES mat_hang(id) ON DELETE RESTRICT,
    kho_id         BIGINT NOT NULL REFERENCES kho(id) ON DELETE RESTRICT,
    so_luong       INTEGER NOT NULL DEFAULT 0 CHECK (so_luong >= 0),
    updated_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_ton_kho_khach UNIQUE (khach_hang_id, mat_hang_id, kho_id)
);

CREATE TABLE phieu_dich_vu_thao_do (
    id                       BIGSERIAL PRIMARY KEY,
    ma_phieu                 VARCHAR(20) NOT NULL UNIQUE,
    khach_hang_id            BIGINT NOT NULL REFERENCES khach_hang(id) ON DELETE RESTRICT,
    kho_id                   BIGINT NOT NULL REFERENCES kho(id) ON DELETE RESTRICT,
    nhan_vien_tiep_nhan_id   BIGINT NOT NULL REFERENCES nhan_vien(id) ON DELETE RESTRICT,
    nguoi_duyet_id           BIGINT REFERENCES nhan_vien(id) ON DELETE SET NULL,
    so_pallet_nhan           INTEGER NOT NULL CHECK (so_pallet_nhan > 0),
    don_gia_dich_vu          NUMERIC(18,2) NOT NULL CHECK (don_gia_dich_vu >= 0),  -- giá thỏa thuận / pallet
    so_pallet_thao_do        INTEGER NOT NULL DEFAULT 0 CHECK (so_pallet_thao_do >= 0),
    so_thanh_nguyen          INTEGER NOT NULL DEFAULT 0 CHECK (so_thanh_nguyen >= 0),
    so_cuc_vuong             INTEGER NOT NULL DEFAULT 0 CHECK (so_cuc_vuong >= 0),
    so_thanh_can_xu_ly       INTEGER NOT NULL DEFAULT 0 CHECK (so_thanh_can_xu_ly >= 0),
    so_cuc_can_xu_ly         INTEGER NOT NULL DEFAULT 0 CHECK (so_cuc_can_xu_ly >= 0),
    kg_phe_lieu              INTEGER NOT NULL DEFAULT 0 CHECK (kg_phe_lieu >= 0),
    phi_dich_vu              NUMERIC(18,2) CHECK (phi_dich_vu >= 0),   -- chốt khi duyệt
    ngay_tiep_nhan           DATE NOT NULL DEFAULT CURRENT_DATE,
    trang_thai               VARCHAR(20) NOT NULL DEFAULT 'da_tiep_nhan'
                             CHECK (trang_thai IN ('da_tiep_nhan','cho_duyet','da_thao_do','da_tra_hang','da_huy')),
    thoi_gian_duyet          TIMESTAMP,
    thoi_gian_tra_hang       TIMESTAMP,
    ly_do_huy                VARCHAR(255),
    ghi_chu                  VARCHAR(255),
    created_at               TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_dvtd_thao_do_khong_vuot CHECK (so_pallet_thao_do <= so_pallet_nhan),
    -- TC10: đã ghi nhận kết quả thì phải thu được ít nhất một thứ
    CONSTRAINT ck_dvtd_co_ket_qua CHECK (
        trang_thai NOT IN ('cho_duyet','da_thao_do','da_tra_hang')
        OR (so_pallet_thao_do > 0
            AND so_thanh_nguyen + so_cuc_vuong + so_thanh_can_xu_ly + so_cuc_can_xu_ly + kg_phe_lieu > 0)),
    CONSTRAINT ck_dvtd_da_duyet CHECK (
        trang_thai NOT IN ('da_thao_do','da_tra_hang')
        OR (nguoi_duyet_id IS NOT NULL AND thoi_gian_duyet IS NOT NULL AND phi_dich_vu IS NOT NULL))
);

CREATE INDEX idx_dvtd_khach ON phieu_dich_vu_thao_do(khach_hang_id, trang_thai);

CREATE TRIGGER trg_set_updated_at_ton_kho_khach
    BEFORE UPDATE ON ton_kho_khach FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();
CREATE TRIGGER trg_block_delete_phieu_dvtd
    BEFORE DELETE ON phieu_dich_vu_thao_do FOR EACH ROW EXECUTE FUNCTION fn_block_hard_delete();
CREATE TRIGGER trg_set_updated_at_phieu_dvtd
    BEFORE UPDATE ON phieu_dich_vu_thao_do FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();