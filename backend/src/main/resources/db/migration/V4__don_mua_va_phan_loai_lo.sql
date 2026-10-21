-- =====================================================================
-- V4: Đơn mua, giá nhập theo lô, loại mặt hàng mới, phân loại lô tái chế
-- =====================================================================

-- ---------- 1. mat_hang: thêm loại phế liệu + đơn vị tính ----------
ALTER TABLE mat_hang DROP CONSTRAINT IF EXISTS mat_hang_loai_mat_hang_check;
ALTER TABLE mat_hang ADD CONSTRAINT mat_hang_loai_mat_hang_check
    CHECK (loai_mat_hang IN ('pallet','linh_kien','cho_tai_che','phe_lieu'));

ALTER TABLE mat_hang ADD COLUMN don_vi_tinh VARCHAR(10) NOT NULL DEFAULT 'cai'
    CHECK (don_vi_tinh IN ('cai','kg'));

-- phế liệu luôn tính theo kg, mặt hàng khác luôn tính theo cái
ALTER TABLE mat_hang ADD CONSTRAINT chk_mathang_phelieu_kg
    CHECK ((loai_mat_hang = 'phe_lieu') = (don_vi_tinh = 'kg'));

-- Mặt hàng cho quy trình tái chế (giá bán 2 pallet là giá DEMO, chỉnh lại ở trang Pallet)
INSERT INTO mat_hang (ma_mat_hang, ten_mat_hang, loai_mat_hang, chat_lieu, don_gia_ban, don_vi_tinh, mo_ta, created_by) VALUES
 ('PL-CPL-05','Pallet gỗ cũ chờ phân loại','cho_tai_che','go',0,'cai','Pallet gỗ cũ mới nhập, chưa kiểm tra',1),
 ('PL-CTD-06','Pallet gỗ cũ chờ tháo dỡ','cho_tai_che','go',0,'cai','Đã phân loại, đang chờ tháo dỡ',1),
 ('PL-DC-07','Pallet gỗ cũ đạt chuẩn','pallet','go',90000,'cai','Pallet gỗ cũ đủ chuẩn, bán nguyên trạng',1),
 ('PL-TC-08','Pallet tái chế','pallet','go',110000,'cai','Pallet lắp ráp từ linh kiện thu hồi',1),
 ('LK-TN-09','Thanh gỗ nguyên (thu hồi)','linh_kien','go',0,'cai','Thanh gỗ còn nguyên sau tháo dỡ',1),
 ('LK-CV-10','Cục gỗ vuông (thu hồi)','linh_kien','go',0,'cai','Cục gỗ vuông còn dùng được sau tháo dỡ',1),
 ('LK-TX-11','Thanh gỗ cần xử lý thêm','linh_kien','go',0,'cai','Thanh gỗ thu hồi cần cắt/sửa trước khi dùng',1),
 ('LK-CX-12','Cục gỗ cần xử lý thêm','linh_kien','go',0,'cai','Cục gỗ thu hồi cần xử lý trước khi dùng',1),
 ('LK-TM-13','Thanh gỗ mới (vật tư)','linh_kien','go',0,'cai','Vật tư mới mua bổ sung khi lắp ráp',1),
 ('LK-CM-14','Cục gỗ mới (vật tư)','linh_kien','go',0,'cai','Vật tư mới mua bổ sung khi lắp ráp',1);

INSERT INTO mat_hang (ma_mat_hang, ten_mat_hang, loai_mat_hang, chat_lieu, don_gia_ban, don_vi_tinh, mo_ta, created_by) VALUES
 ('PE-GO-15','Phế liệu gỗ','phe_lieu','go',0,'kg','Thanh gãy, cục vỡ vụn, tính theo kg',1);

-- ---------- 2. Đơn mua hàng ----------
CREATE TABLE don_mua (
    id                  BIGSERIAL PRIMARY KEY,
    ma_don_mua          VARCHAR(20) NOT NULL UNIQUE,
    ncc_id              BIGINT NOT NULL REFERENCES nha_cung_cap(id) ON DELETE RESTRICT,
    kho_id              BIGINT NOT NULL REFERENCES kho(id) ON DELETE RESTRICT,
    nhan_vien_lap_id    BIGINT NOT NULL REFERENCES nhan_vien(id) ON DELETE RESTRICT,
    nguoi_xac_nhan_id   BIGINT REFERENCES nhan_vien(id) ON DELETE SET NULL,
    ngay_dat            DATE NOT NULL DEFAULT CURRENT_DATE,
    ngay_du_kien_nhan   DATE,
    trang_thai          VARCHAR(20) NOT NULL DEFAULT 'nhap'
                        CHECK (trang_thai IN ('nhap','da_xac_nhan','da_nhan_mot_phan','da_nhan_du','da_huy')),
    tong_tien           NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (tong_tien >= 0),
    ghi_chu             VARCHAR(255),
    ly_do_huy           VARCHAR(255),
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_donmua_ngay CHECK (ngay_du_kien_nhan IS NULL OR ngay_du_kien_nhan >= ngay_dat),
    CONSTRAINT chk_donmua_xacnhan CHECK (trang_thai IN ('nhap','da_huy') OR nguoi_xac_nhan_id IS NOT NULL)
);

CREATE TABLE chi_tiet_don_mua (
    id                  BIGSERIAL PRIMARY KEY,
    don_mua_id          BIGINT NOT NULL REFERENCES don_mua(id) ON DELETE CASCADE,
    mat_hang_id         BIGINT NOT NULL REFERENCES mat_hang(id) ON DELETE RESTRICT,
    so_luong_dat        INTEGER NOT NULL CHECK (so_luong_dat > 0),
    so_luong_da_nhan    INTEGER NOT NULL DEFAULT 0 CHECK (so_luong_da_nhan >= 0),
    don_gia             NUMERIC(18,2) NOT NULL CHECK (don_gia >= 0),
    thanh_tien          NUMERIC(18,2) GENERATED ALWAYS AS (so_luong_dat * don_gia) STORED,
    UNIQUE (don_mua_id, mat_hang_id),
    CONSTRAINT chk_ctdonmua_danhan CHECK (so_luong_da_nhan <= so_luong_dat)
);

CREATE INDEX idx_donmua_ncc        ON don_mua(ncc_id);
CREATE INDEX idx_donmua_kho        ON don_mua(kho_id);
CREATE INDEX idx_donmua_trangthai  ON don_mua(trang_thai);
CREATE INDEX idx_ctdonmua_donmua   ON chi_tiet_don_mua(don_mua_id);
CREATE INDEX idx_ctdonmua_mathang  ON chi_tiet_don_mua(mat_hang_id);

CREATE TRIGGER trg_block_delete_don_mua
    BEFORE DELETE ON don_mua
    FOR EACH ROW EXECUTE FUNCTION fn_block_hard_delete();

CREATE TRIGGER trg_set_updated_at_don_mua
    BEFORE UPDATE ON don_mua
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- ---------- 3. Giá nhập theo lô + liên kết đơn mua ----------
ALTER TABLE lo_hang
    ADD COLUMN don_gia_nhap NUMERIC(18,2) CHECK (don_gia_nhap IS NULL OR don_gia_nhap >= 0),
    ADD COLUMN chi_tiet_don_mua_id BIGINT REFERENCES chi_tiet_don_mua(id) ON DELETE SET NULL;

ALTER TABLE phieu_kho
    ADD COLUMN don_mua_id BIGINT REFERENCES don_mua(id) ON DELETE SET NULL;

CREATE INDEX idx_lohang_ctdonmua ON lo_hang(chi_tiet_don_mua_id);
CREATE INDEX idx_phieukho_donmua ON phieu_kho(don_mua_id);

-- ---------- 4. Phiếu tái chế: bước phân loại 3 nhóm ----------
-- nhap -> da_phan_loai (đã duyệt phân loại, còn pallet chờ tháo dỡ) -> da_hoan_tat | da_huy
ALTER TABLE phieu_tai_che DROP CONSTRAINT IF EXISTS phieu_tai_che_trang_thai_check;
ALTER TABLE phieu_tai_che ADD CONSTRAINT phieu_tai_che_trang_thai_check
    CHECK (trang_thai IN ('nhap','da_phan_loai','da_hoan_tat','da_huy'));

ALTER TABLE phieu_tai_che
    ADD COLUMN so_ban_lai          INTEGER NOT NULL DEFAULT 0 CHECK (so_ban_lai >= 0),
    ADD COLUMN so_thao_do          INTEGER NOT NULL DEFAULT 0 CHECK (so_thao_do >= 0),
    ADD COLUMN so_loai_bo          INTEGER NOT NULL DEFAULT 0 CHECK (so_loai_bo >= 0),
    ADD COLUMN kg_phe_lieu_loai_bo INTEGER NOT NULL DEFAULT 0 CHECK (kg_phe_lieu_loai_bo >= 0),
    ADD COLUMN nguoi_duyet_id      BIGINT REFERENCES nhan_vien(id) ON DELETE SET NULL,
    ADD COLUMN thoi_gian_duyet     TIMESTAMP,
    ADD COLUMN ly_do_huy           VARCHAR(255);

-- đã duyệt phân loại thì 3 nhóm phải cộng đúng bằng số pallet xử lý
ALTER TABLE phieu_tai_che ADD CONSTRAINT chk_phieutaiche_phanloai CHECK (
    trang_thai IN ('nhap','da_huy')
    OR (so_ban_lai + so_thao_do + so_loai_bo = so_luong_da_xu_ly)
);
-- kg phế liệu của nhóm loại bỏ chỉ có khi có pallet loại bỏ
ALTER TABLE phieu_tai_che ADD CONSTRAINT chk_phieutaiche_kg_loaibo CHECK (
    so_loai_bo > 0 OR kg_phe_lieu_loai_bo = 0
);
-- đã duyệt thì phải có người duyệt
ALTER TABLE phieu_tai_che ADD CONSTRAINT chk_phieutaiche_nguoiduyet CHECK (
    trang_thai IN ('nhap','da_huy') OR nguoi_duyet_id IS NOT NULL
);