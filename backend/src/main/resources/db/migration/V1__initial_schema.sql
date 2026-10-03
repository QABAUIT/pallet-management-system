-- =====================================================================
-- PALLETTRACK PRO - INIT SCHEMA (PostgreSQL)
-- Công ty TNHH TM SX Pallet Hoàng Phát
-- =====================================================================

-- ============ 1. NGƯỜI DÙNG & PHÂN QUYỀN ============

CREATE TABLE vai_tro (
    id              BIGSERIAL PRIMARY KEY,
    ma_vai_tro      VARCHAR(30)  NOT NULL UNIQUE,
    ten_vai_tro     VARCHAR(100) NOT NULL,
    cap_duyet       SMALLINT     NOT NULL DEFAULT 0
);

CREATE TABLE vai_tro_quyen (
    id              BIGSERIAL PRIMARY KEY,
    vai_tro_id      BIGINT NOT NULL REFERENCES vai_tro(id) ON DELETE CASCADE,
    module_code     VARCHAR(50) NOT NULL,
    duoc_xem        BOOLEAN NOT NULL DEFAULT FALSE,
    duoc_them       BOOLEAN NOT NULL DEFAULT FALSE,
    duoc_sua        BOOLEAN NOT NULL DEFAULT FALSE,
    duoc_xoa        BOOLEAN NOT NULL DEFAULT FALSE,
    duoc_duyet      BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (vai_tro_id, module_code)
);

CREATE TABLE bo_phan (
    id              BIGSERIAL PRIMARY KEY,
    ten_bo_phan     VARCHAR(150) NOT NULL UNIQUE
);

CREATE TABLE ca_lam_viec (
    id              BIGSERIAL PRIMARY KEY,
    ten_ca          VARCHAR(100) NOT NULL,
    gio_bat_dau     TIME NOT NULL,
    gio_ket_thuc    TIME NOT NULL
);

CREATE TABLE kho (
    id              BIGSERIAL PRIMARY KEY,
    ma_kho          VARCHAR(20)  NOT NULL UNIQUE,
    ten_kho         VARCHAR(150) NOT NULL,
    dia_chi         VARCHAR(255),
    loai_dia_diem   VARCHAR(20) NOT NULL DEFAULT 'kho'
                    CHECK (loai_dia_diem IN ('tru_so','kho','cang','khac')),
    dien_tich_m2    NUMERIC(12,2) CHECK (dien_tich_m2 IS NULL OR dien_tich_m2 >= 0),
    quan_ly_id      BIGINT,
    trang_thai      VARCHAR(20) NOT NULL DEFAULT 'hoat_dong'
                    CHECK (trang_thai IN ('hoat_dong','ngung_hoat_dong'))
);

CREATE TABLE nhan_vien (
    id              BIGSERIAL PRIMARY KEY,
    ma_nv           VARCHAR(20)  NOT NULL UNIQUE,
    ho_ten          VARCHAR(150) NOT NULL,
    email           VARCHAR(150) UNIQUE,
    sdt             VARCHAR(20),
    ngay_sinh       DATE,
    gioi_tinh       VARCHAR(10) CHECK (gioi_tinh IN ('Nam','Nu','Khac')),
    dia_chi         VARCHAR(255),
    anh_dai_dien    VARCHAR(255),
    vai_tro_id      BIGINT NOT NULL REFERENCES vai_tro(id) ON DELETE RESTRICT,
    kho_id          BIGINT REFERENCES kho(id) ON DELETE SET NULL,
    bo_phan_id      BIGINT REFERENCES bo_phan(id) ON DELETE SET NULL,
    chuc_vu         VARCHAR(150),
    loai_hop_dong   VARCHAR(100),
    so_hop_dong     VARCHAR(50),
    ten_dang_nhap   VARCHAR(50)  NOT NULL UNIQUE,
    mat_khau_hash   VARCHAR(255) NOT NULL,
    ngay_vao_lam    DATE,
    ngay_nghi_viec  DATE,
    trang_thai      VARCHAR(20) NOT NULL DEFAULT 'dang_lam_viec'
                    CHECK (trang_thai IN ('dang_lam_viec','cong_tac','da_nghi_viec')),
    created_at      TIMESTAMP NOT NULL DEFAULT now(),
    updated_at      TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_nhanvien_ngaynghi CHECK (
        ngay_nghi_viec IS NULL OR ngay_vao_lam IS NULL OR ngay_nghi_viec >= ngay_vao_lam
    )
);

ALTER TABLE kho ADD CONSTRAINT fk_kho_quan_ly
    FOREIGN KEY (quan_ly_id) REFERENCES nhan_vien(id) ON DELETE SET NULL;

CREATE TABLE lich_lam_viec (
    id                  BIGSERIAL PRIMARY KEY,
    nhan_vien_id        BIGINT NOT NULL REFERENCES nhan_vien(id) ON DELETE CASCADE,
    ngay                DATE NOT NULL,
    ca_lam_viec_id      BIGINT NOT NULL REFERENCES ca_lam_viec(id) ON DELETE RESTRICT,
    kho_id              BIGINT REFERENCES kho(id) ON DELETE SET NULL,
    nguoi_xep_lich_id   BIGINT REFERENCES nhan_vien(id) ON DELETE SET NULL,
    ghi_chu             VARCHAR(255),
    UNIQUE (nhan_vien_id, ngay)
);

CREATE TABLE phien_dang_nhap (
    id                  BIGSERIAL PRIMARY KEY,
    nhan_vien_id        BIGINT NOT NULL REFERENCES nhan_vien(id) ON DELETE CASCADE,
    refresh_token       VARCHAR(500) NOT NULL,
    ghi_nho_dang_nhap   BOOLEAN NOT NULL DEFAULT FALSE,
    thiet_bi            VARCHAR(255),
    ip_address          VARCHAR(45),
    thoi_gian_tao       TIMESTAMP NOT NULL DEFAULT now(),
    thoi_gian_het_han   TIMESTAMP NOT NULL,
    CONSTRAINT chk_phiendn_hethan CHECK (thoi_gian_het_han > thoi_gian_tao)
);

CREATE TABLE token_dat_lai_mat_khau (
    id                  BIGSERIAL PRIMARY KEY,
    nhan_vien_id        BIGINT NOT NULL REFERENCES nhan_vien(id) ON DELETE CASCADE,
    token               VARCHAR(255) NOT NULL UNIQUE,
    thoi_gian_tao       TIMESTAMP NOT NULL DEFAULT now(),
    thoi_gian_het_han   TIMESTAMP NOT NULL,
    da_su_dung          BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_token_hethan CHECK (thoi_gian_het_han > thoi_gian_tao)
);

-- ============ 2. KHO & VỊ TRÍ ============

CREATE TABLE vi_tri_luu_tru (
    id          BIGSERIAL PRIMARY KEY,
    kho_id      BIGINT NOT NULL REFERENCES kho(id) ON DELETE CASCADE,
    khu_vuc     VARCHAR(20) NOT NULL,
    ke          VARCHAR(20),
    tang        VARCHAR(20),
    ma_vi_tri   VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE phuong_tien (
    id          BIGSERIAL PRIMARY KEY,
    bien_so     VARCHAR(20) NOT NULL UNIQUE,
    loai_xe     VARCHAR(50),
    tai_xe_id   BIGINT REFERENCES nhan_vien(id) ON DELETE SET NULL,
    trang_thai  VARCHAR(20) NOT NULL DEFAULT 'hoat_dong'
                CHECK (trang_thai IN ('hoat_dong','ngung_hoat_dong','bao_tri'))
);

-- ============ 3. ĐỐI TÁC ============

CREATE TABLE nha_cung_cap (
    id              BIGSERIAL PRIMARY KEY,
    ma_ncc          VARCHAR(20)  NOT NULL UNIQUE,
    ten_ncc         VARCHAR(200) NOT NULL,
    mst             VARCHAR(20),
    dia_chi         VARCHAR(255),
    sdt             VARCHAR(20),
    email           VARCHAR(150),
    nguoi_lien_he   VARCHAR(150),
    nhom_hang       VARCHAR(150),
    ghi_chu         TEXT,
    trang_thai      VARCHAR(20) NOT NULL DEFAULT 'dang_hop_tac'
                    CHECK (trang_thai IN ('dang_hop_tac','ngung_hop_tac')),
    created_by      BIGINT REFERENCES nhan_vien(id) ON DELETE SET NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE khach_hang (
    id              BIGSERIAL PRIMARY KEY,
    ma_kh           VARCHAR(20)  NOT NULL UNIQUE,
    ten_kh          VARCHAR(200) NOT NULL,
    loai_kh         VARCHAR(20) DEFAULT 'doanh_nghiep'
                    CHECK (loai_kh IN ('ca_nhan','doanh_nghiep')),
    mst             VARCHAR(20),
    dia_chi         VARCHAR(255),
    sdt             VARCHAR(20),
    email           VARCHAR(150),
    nguoi_dai_dien  VARCHAR(150),
    trang_thai      VARCHAR(20) NOT NULL DEFAULT 'dang_hop_tac'
                    CHECK (trang_thai IN ('dang_hop_tac','ngung_hop_tac')),
    created_by      BIGINT REFERENCES nhan_vien(id) ON DELETE SET NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT now()
);

-- ============ 4. THÔNG TIN CÔNG TY ============

CREATE TABLE company (
    id                      BIGSERIAL PRIMARY KEY CHECK (id = 1),
    ten_cong_ty             VARCHAR(255) NOT NULL,
    mst                     VARCHAR(20)  NOT NULL,
    dia_chi                 VARCHAR(255),
    sdt                     VARCHAR(100),
    email                   VARCHAR(150),
    website                 VARCHAR(150),
    logo                    VARCHAR(255),
    so_tai_khoan_ngan_hang  VARCHAR(50),
    ten_ngan_hang           VARCHAR(150),
    chu_tai_khoan           VARCHAR(255),
    nguoi_dai_dien_phap_ly  VARCHAR(150),
    ty_le_vat_mac_dinh      NUMERIC(5,4) NOT NULL DEFAULT 0.08
                            CHECK (ty_le_vat_mac_dinh >= 0 AND ty_le_vat_mac_dinh <= 1)
);

-- ============ 5. MẶT HÀNG & TỒN KHO ============
-- loai_mat_hang có 'cho_tai_che' (pallet cũ chờ tháo dỡ) TÁCH RIÊNG khỏi 'pallet'
-- (thành phẩm bán được), để không làm phình tồn kho bán được bằng hàng chưa xử lý.

CREATE TABLE mat_hang (
    id                  BIGSERIAL PRIMARY KEY,
    ma_mat_hang         VARCHAR(20)  NOT NULL UNIQUE,
    ten_mat_hang        VARCHAR(200) NOT NULL,
    loai_mat_hang       VARCHAR(20) NOT NULL DEFAULT 'pallet'
                        CHECK (loai_mat_hang IN ('pallet','linh_kien','cho_tai_che')),
    chat_lieu           VARCHAR(20) CHECK (chat_lieu IN ('go','nhua','sat','khac')),
    kich_thuoc_dai      INTEGER CHECK (kich_thuoc_dai IS NULL OR kich_thuoc_dai > 0),
    kich_thuoc_rong     INTEGER CHECK (kich_thuoc_rong IS NULL OR kich_thuoc_rong > 0),
    kich_thuoc_cao      INTEGER CHECK (kich_thuoc_cao IS NULL OR kich_thuoc_cao > 0),
    tai_trong_tinh      NUMERIC(10,2) CHECK (tai_trong_tinh IS NULL OR tai_trong_tinh >= 0),
    tai_trong_dong      NUMERIC(10,2) CHECK (tai_trong_dong IS NULL OR tai_trong_dong >= 0),
    tieu_chuan          VARCHAR(150),
    ncc_mac_dinh_id     BIGINT REFERENCES nha_cung_cap(id) ON DELETE SET NULL,
    don_gia_ban         NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (don_gia_ban >= 0),
    hinh_anh            VARCHAR(255),
    mo_ta               TEXT,
    trang_thai          VARCHAR(20) NOT NULL DEFAULT 'dang_kinh_doanh'
                        CHECK (trang_thai IN ('dang_kinh_doanh','ngung_kinh_doanh')),
    created_by          BIGINT REFERENCES nhan_vien(id) ON DELETE SET NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE ton_kho (
    id                      BIGSERIAL PRIMARY KEY,
    mat_hang_id             BIGINT NOT NULL REFERENCES mat_hang(id) ON DELETE CASCADE,
    kho_id                  BIGINT NOT NULL REFERENCES kho(id) ON DELETE CASCADE,
    vi_tri_id               BIGINT REFERENCES vi_tri_luu_tru(id) ON DELETE SET NULL,
    so_luong_ton_kho        INTEGER NOT NULL DEFAULT 0 CHECK (so_luong_ton_kho >= 0),
    so_luong_da_giu_cho     INTEGER NOT NULL DEFAULT 0 CHECK (so_luong_da_giu_cho >= 0),
    so_luong_toi_thieu      INTEGER NOT NULL DEFAULT 0 CHECK (so_luong_toi_thieu >= 0),
    updated_at              TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (mat_hang_id, kho_id),
    CONSTRAINT chk_tonkho_khadung CHECK (so_luong_ton_kho >= so_luong_da_giu_cho)
);

-- muc_dich_nhap: 'ban_thanh_pham' (mặc định) hoặc 'cho_tai_che' - quyết định lô này
-- có hiện ở tab "Tái chế" không. Đây là thuộc tính của LÔ (không phải của phiếu kho),
-- vì 1 lô có thể được tháo dỡ dần qua nhiều đợt khác nhau.
CREATE TABLE lo_hang (
    id                  BIGSERIAL PRIMARY KEY,
    ma_lo               VARCHAR(30) NOT NULL UNIQUE,
    mat_hang_id         BIGINT NOT NULL REFERENCES mat_hang(id) ON DELETE CASCADE,
    kho_id              BIGINT NOT NULL REFERENCES kho(id) ON DELETE CASCADE,
    so_luong_nhap       INTEGER NOT NULL CHECK (so_luong_nhap > 0),
    so_luong_con_lai    INTEGER NOT NULL CHECK (so_luong_con_lai >= 0),
    ngay_nhap           DATE NOT NULL,
    ncc_id              BIGINT REFERENCES nha_cung_cap(id) ON DELETE SET NULL,
    trang_thai          VARCHAR(20) NOT NULL DEFAULT 'con_hang'
                        CHECK (trang_thai IN ('con_hang','da_het')),
    muc_dich_nhap       VARCHAR(20) NOT NULL DEFAULT 'ban_thanh_pham'
                        CHECK (muc_dich_nhap IN ('ban_thanh_pham','cho_tai_che')),
    CONSTRAINT chk_lohang_conlai CHECK (so_luong_con_lai <= so_luong_nhap)
);

-- ============ 6. TÁI CHẾ PALLET ============
-- Mỗi "phiếu tái chế" = 1 đợt tháo dỡ thật, gắn với 1 lô nguồn (lo_hang.muc_dich_nhap
-- = 'cho_tai_che'). Ra nhiều loại vật liệu khác nhau -> cần bảng chi tiết riêng.
-- Khi hoàn tất, backend phải: (1) trừ lo_hang.so_luong_con_lai + ton_kho của pallet cũ,
-- (2) cộng ton_kho cho từng vật liệu trong chi_tiet_tai_che, (3) tự sinh 1 phiếu xuất +
-- 1 phiếu nhập kho tương ứng (xem phieu_kho.phieu_tai_che_id ở mục 8) để giữ đúng
-- nguyên tắc "mọi biến động tồn kho đều có phiếu kho tương ứng".

CREATE TABLE phieu_tai_che (
    id                      BIGSERIAL PRIMARY KEY,
    ma_phieu                VARCHAR(20) NOT NULL UNIQUE,
    lo_hang_id              BIGINT NOT NULL REFERENCES lo_hang(id) ON DELETE RESTRICT,
    kho_id                  BIGINT NOT NULL REFERENCES kho(id) ON DELETE RESTRICT,
    nhan_vien_thuc_hien_id  BIGINT NOT NULL REFERENCES nhan_vien(id) ON DELETE RESTRICT,
    so_luong_da_xu_ly       INTEGER NOT NULL CHECK (so_luong_da_xu_ly > 0),
    ngay_thuc_hien          DATE NOT NULL DEFAULT CURRENT_DATE,
    trang_thai              VARCHAR(20) NOT NULL DEFAULT 'nhap'
                            CHECK (trang_thai IN ('nhap','da_hoan_tat','da_huy')),
    ghi_chu                 VARCHAR(255),
    created_at              TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE chi_tiet_tai_che (
    id                  BIGSERIAL PRIMARY KEY,
    phieu_tai_che_id    BIGINT NOT NULL REFERENCES phieu_tai_che(id) ON DELETE CASCADE,
    mat_hang_id         BIGINT NOT NULL REFERENCES mat_hang(id) ON DELETE RESTRICT,
    so_luong            INTEGER NOT NULL CHECK (so_luong > 0),
    ghi_chu             VARCHAR(255)
);

-- ============ 7. HÓA ĐƠN ============

CREATE TABLE hoa_don (
    id                      BIGSERIAL PRIMARY KEY,
    ma_hoa_don              VARCHAR(20) NOT NULL UNIQUE,
    loai_giao_dich          VARCHAR(30) NOT NULL
        CHECK (loai_giao_dich IN (
            'thu_ban_pallet','thu_ban_linh_kien','thu_khac',
            'chi_mua_pallet_cu','chi_nguyen_lieu_phu_tro','chi_sua_chua','chi_van_chuyen','chi_khac'
        )),
    khach_hang_id           BIGINT REFERENCES khach_hang(id) ON DELETE RESTRICT,
    ncc_id                  BIGINT REFERENCES nha_cung_cap(id) ON DELETE RESTRICT,
    kho_id                  BIGINT REFERENCES kho(id) ON DELETE SET NULL,
    nhan_vien_lap_id        BIGINT NOT NULL REFERENCES nhan_vien(id) ON DELETE RESTRICT,
    ngay_lap                TIMESTAMP NOT NULL DEFAULT now(),
    trang_thai              VARCHAR(20) NOT NULL DEFAULT 'nhap'
        CHECK (trang_thai IN ('nhap','cho_duyet','da_thanh_toan','qua_han','da_huy')),
    tong_tien_hang          NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (tong_tien_hang >= 0),
    tong_tien_chiet_khau    NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (tong_tien_chiet_khau >= 0),
    ty_le_giam_gia_tong     NUMERIC(5,2)  NOT NULL DEFAULT 0
                            CHECK (ty_le_giam_gia_tong BETWEEN 0 AND 100),
    tien_giam_gia_tong      NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (tien_giam_gia_tong >= 0),
    tien_truoc_thue         NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (tien_truoc_thue >= 0),
    phi_giao_hang           NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (phi_giao_hang >= 0),
    ty_le_vat               NUMERIC(5,2)  NOT NULL DEFAULT 8 CHECK (ty_le_vat BETWEEN 0 AND 100),
    tien_vat                NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (tien_vat >= 0),
    tong_thanh_toan         NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (tong_thanh_toan >= 0),
    tien_khach_dua          NUMERIC(18,2),
    tien_thoi_lai           NUMERIC(18,2),
    phuong_thuc_thanh_toan  VARCHAR(20)
        CHECK (phuong_thuc_thanh_toan IN ('tien_mat','chuyen_khoan','qr_code')),
    han_thanh_toan          DATE,
    ngay_thanh_toan         TIMESTAMP,
    da_chot_so              BOOLEAN NOT NULL DEFAULT FALSE,
    ghi_chu                 TEXT,
    file_pdf                VARCHAR(255),
    created_at              TIMESTAMP NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_hoadon_doi_tuong CHECK (
        CASE
            WHEN loai_giao_dich IN ('thu_ban_pallet','thu_ban_linh_kien')
                THEN khach_hang_id IS NOT NULL AND ncc_id IS NULL
            WHEN loai_giao_dich = 'chi_mua_pallet_cu'
                THEN ncc_id IS NOT NULL AND khach_hang_id IS NULL
            WHEN loai_giao_dich IN ('chi_nguyen_lieu_phu_tro','chi_sua_chua','chi_van_chuyen')
                THEN khach_hang_id IS NULL
            ELSE
                khach_hang_id IS NULL AND ncc_id IS NULL
        END
    )
);

CREATE TABLE chi_tiet_hoa_don (
    id                  BIGSERIAL PRIMARY KEY,
    hoa_don_id          BIGINT NOT NULL REFERENCES hoa_don(id) ON DELETE CASCADE,
    mat_hang_id         BIGINT NOT NULL REFERENCES mat_hang(id) ON DELETE RESTRICT,
    so_luong            INTEGER NOT NULL CHECK (so_luong > 0),
    don_gia             NUMERIC(18,2) NOT NULL CHECK (don_gia >= 0),
    ty_le_chiet_khau    NUMERIC(5,2)  NOT NULL DEFAULT 0 CHECK (ty_le_chiet_khau BETWEEN 0 AND 100),
    tien_chiet_khau     NUMERIC(18,2)
        GENERATED ALWAYS AS (ROUND(so_luong * don_gia * ty_le_chiet_khau / 100, 2)) STORED,
    thanh_tien          NUMERIC(18,2)
        GENERATED ALWAYS AS (so_luong * don_gia - ROUND(so_luong * don_gia * ty_le_chiet_khau / 100, 2)) STORED
);

-- ============ 8. PHIẾU KHO ============

CREATE TABLE phieu_kho (
    id                      BIGSERIAL PRIMARY KEY,
    ma_phieu                VARCHAR(20) NOT NULL UNIQUE,
    loai_phieu              VARCHAR(20) NOT NULL
        CHECK (loai_phieu IN ('nhap_kho','xuat_kho','chuyen_kho','dieu_chinh_kiem_ke')),
    kho_id                  BIGINT NOT NULL REFERENCES kho(id) ON DELETE RESTRICT,
    kho_doi_ung_id          BIGINT REFERENCES kho(id) ON DELETE RESTRICT,
    vi_tri_id               BIGINT REFERENCES vi_tri_luu_tru(id) ON DELETE SET NULL,
    hoa_don_id              BIGINT REFERENCES hoa_don(id) ON DELETE SET NULL,
    phieu_tai_che_id        BIGINT REFERENCES phieu_tai_che(id) ON DELETE SET NULL,
    nhan_vien_giao_nhan_id  BIGINT REFERENCES nhan_vien(id) ON DELETE SET NULL,
    nguoi_duyet_id          BIGINT REFERENCES nhan_vien(id) ON DELETE SET NULL,
    bien_so_xe_doi_tac      VARCHAR(20),
    phuong_tien_id          BIGINT REFERENCES phuong_tien(id) ON DELETE SET NULL,
    so_bien_ban_kiem_tra    VARCHAR(50),
    ngay_gio                TIMESTAMP NOT NULL DEFAULT now(),
    ghi_chu                 TEXT,
    trang_thai              VARCHAR(20) NOT NULL DEFAULT 'cho_xu_ly'
        CHECK (trang_thai IN ('cho_xu_ly','da_hoan_thanh','da_huy')),
    CONSTRAINT chk_phieukho_chuyenkho CHECK (
        (loai_phieu = 'chuyen_kho' AND kho_doi_ung_id IS NOT NULL AND kho_doi_ung_id <> kho_id)
        OR
        (loai_phieu <> 'chuyen_kho' AND kho_doi_ung_id IS NULL)
    )
);

CREATE TABLE chi_tiet_phieu_kho (
    id              BIGSERIAL PRIMARY KEY,
    phieu_kho_id    BIGINT NOT NULL REFERENCES phieu_kho(id) ON DELETE CASCADE,
    mat_hang_id     BIGINT NOT NULL REFERENCES mat_hang(id) ON DELETE RESTRICT,
    lo_id           BIGINT REFERENCES lo_hang(id) ON DELETE SET NULL,
    so_luong        INTEGER NOT NULL CHECK (so_luong > 0),
    ghi_chu         VARCHAR(255)
);

CREATE TABLE sua_chua_pallet (
    id                      BIGSERIAL PRIMARY KEY,
    mat_hang_id             BIGINT NOT NULL REFERENCES mat_hang(id) ON DELETE RESTRICT,
    kho_id                  BIGINT NOT NULL REFERENCES kho(id) ON DELETE RESTRICT,
    so_luong                INTEGER NOT NULL CHECK (so_luong > 0),
    mo_ta_cong_viec         VARCHAR(255),
    chi_phi                 NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (chi_phi >= 0),
    nhan_vien_thuc_hien_id  BIGINT REFERENCES nhan_vien(id) ON DELETE SET NULL,
    hoa_don_id              BIGINT REFERENCES hoa_don(id) ON DELETE SET NULL,
    ngay                    DATE NOT NULL
);

CREATE TABLE bang_gia (
    id                          BIGSERIAL PRIMARY KEY,
    mat_hang_id                 BIGINT NOT NULL REFERENCES mat_hang(id) ON DELETE CASCADE,
    khach_hang_id                BIGINT REFERENCES khach_hang(id) ON DELETE CASCADE,
    don_gia                     NUMERIC(18,2) NOT NULL CHECK (don_gia >= 0),
    ngay_bat_dau_hieu_luc       DATE NOT NULL,
    ngay_ket_thuc_hieu_luc      DATE,
    CONSTRAINT chk_banggia_ngay CHECK (
        ngay_ket_thuc_hieu_luc IS NULL OR ngay_ket_thuc_hieu_luc >= ngay_bat_dau_hieu_luc
    )
);

-- ============ 9. KIỂM KÊ ============

CREATE TABLE phien_kiem_ke (
    id              BIGSERIAL PRIMARY KEY,
    ma_phien        VARCHAR(20) NOT NULL UNIQUE,
    kho_id          BIGINT NOT NULL REFERENCES kho(id) ON DELETE RESTRICT,
    nguoi_tao_id    BIGINT NOT NULL REFERENCES nhan_vien(id) ON DELETE RESTRICT,
    nguoi_chot_id   BIGINT REFERENCES nhan_vien(id) ON DELETE SET NULL,
    ngay_bat_dau    DATE NOT NULL,
    ngay_hoan_tat   DATE,
    trang_thai      VARCHAR(20) NOT NULL DEFAULT 'dang_dien_ra'
                    CHECK (trang_thai IN ('dang_dien_ra','da_hoan_tat')),
    ghi_chu         VARCHAR(255),
    CONSTRAINT chk_phienkiemke_ngay CHECK (ngay_hoan_tat IS NULL OR ngay_hoan_tat >= ngay_bat_dau)
);

CREATE TABLE chi_tiet_kiem_ke (
    id                      BIGSERIAL PRIMARY KEY,
    phien_kiem_ke_id        BIGINT NOT NULL REFERENCES phien_kiem_ke(id) ON DELETE CASCADE,
    mat_hang_id             BIGINT NOT NULL REFERENCES mat_hang(id) ON DELETE RESTRICT,
    vi_tri_id               BIGINT REFERENCES vi_tri_luu_tru(id) ON DELETE SET NULL,
    ton_he_thong            INTEGER NOT NULL,
    ton_thuc_te             INTEGER,
    chenh_lech              INTEGER GENERATED ALWAYS AS (ton_thuc_te - ton_he_thong) STORED,
    trang_thai_kiem_ke      VARCHAR(20) NOT NULL DEFAULT 'cho_kiem'
        CHECK (trang_thai_kiem_ke IN ('khop','lech','cho_kiem')),
    nguoi_kiem_id           BIGINT REFERENCES nhan_vien(id) ON DELETE SET NULL,
    ghi_chu_giai_trinh      VARCHAR(255),
    CONSTRAINT chk_kiemke_khoplech CHECK (
        (ton_thuc_te IS NULL AND trang_thai_kiem_ke = 'cho_kiem')
        OR (ton_thuc_te IS NOT NULL AND ton_thuc_te = ton_he_thong AND trang_thai_kiem_ke = 'khop')
        OR (ton_thuc_te IS NOT NULL AND ton_thuc_te <> ton_he_thong AND trang_thai_kiem_ke = 'lech')
    )
);

-- ============ 10. THANH TOÁN ============

CREATE TABLE thanh_toan (
    id                      BIGSERIAL PRIMARY KEY,
    hoa_don_id              BIGINT NOT NULL REFERENCES hoa_don(id) ON DELETE CASCADE,
    phuong_thuc             VARCHAR(20) NOT NULL
                            CHECK (phuong_thuc IN ('tien_mat','chuyen_khoan','qr_code')),
    so_tien                 NUMERIC(18,2) NOT NULL CHECK (so_tien > 0),
    ma_giao_dich_ngan_hang  VARCHAR(100),
    trang_thai              VARCHAR(20) NOT NULL DEFAULT 'cho_xu_ly'
                            CHECK (trang_thai IN ('cho_xu_ly','thanh_cong','that_bai')),
    nguoi_xu_ly_id          BIGINT REFERENCES nhan_vien(id) ON DELETE SET NULL,
    tu_dong_xac_nhan        BOOLEAN NOT NULL DEFAULT FALSE,
    thoi_gian               TIMESTAMP NOT NULL DEFAULT now(),
    ghi_chu                 VARCHAR(255)
);

CREATE TABLE ma_qr_thanh_toan (
    id                      BIGSERIAL PRIMARY KEY,
    hoa_don_id              BIGINT NOT NULL REFERENCES hoa_don(id) ON DELETE CASCADE,
    noi_dung_qr             TEXT NOT NULL,
    so_tien                 NUMERIC(18,2) NOT NULL CHECK (so_tien > 0),
    trang_thai              VARCHAR(20) NOT NULL DEFAULT 'dang_cho'
                            CHECK (trang_thai IN ('dang_cho','da_thanh_toan','het_han')),
    thoi_gian_tao           TIMESTAMP NOT NULL DEFAULT now(),
    thoi_gian_het_han       TIMESTAMP NOT NULL,
    thoi_gian_thanh_toan    TIMESTAMP,
    CONSTRAINT chk_qr_hethan CHECK (thoi_gian_het_han > thoi_gian_tao)
);

CREATE TABLE webhook_ngan_hang_log (
    id                      BIGSERIAL PRIMARY KEY,
    ma_qr_thanh_toan_id     BIGINT REFERENCES ma_qr_thanh_toan(id) ON DELETE SET NULL,
    thanh_toan_id           BIGINT REFERENCES thanh_toan(id) ON DELETE SET NULL,
    payload_tho             JSONB NOT NULL,
    da_xu_ly                BOOLEAN NOT NULL DEFAULT FALSE,
    loi_xu_ly               VARCHAR(255),
    thoi_gian_nhan          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE lich_su_hoa_don (
    id                  BIGSERIAL PRIMARY KEY,
    hoa_don_id          BIGINT NOT NULL REFERENCES hoa_don(id) ON DELETE CASCADE,
    nguoi_sua_id        BIGINT NOT NULL REFERENCES nhan_vien(id) ON DELETE RESTRICT,
    thoi_gian           TIMESTAMP NOT NULL DEFAULT now(),
    hanh_dong           VARCHAR(30) NOT NULL
        CHECK (hanh_dong IN ('tao_moi','cap_nhat','duyet','huy','xoa_dong_hang')),
    truong_thay_doi     VARCHAR(100),
    gia_tri_cu          TEXT,
    gia_tri_moi         TEXT,
    ly_do               VARCHAR(255)
);

-- ============ 11. HỢP ĐỒNG & BÁO GIÁ ============

CREATE TABLE hop_dong (
    id              BIGSERIAL PRIMARY KEY,
    ma_hop_dong     VARCHAR(30) NOT NULL UNIQUE,
    loai_doi_tac    VARCHAR(20) NOT NULL CHECK (loai_doi_tac IN ('khach_hang','nha_cung_cap')),
    khach_hang_id   BIGINT REFERENCES khach_hang(id) ON DELETE RESTRICT,
    ncc_id          BIGINT REFERENCES nha_cung_cap(id) ON DELETE RESTRICT,
    ngay_ky         DATE,
    ngay_hieu_luc   DATE,
    ngay_het_han    DATE,
    noi_dung        TEXT,
    file_dinh_kem   VARCHAR(255),
    nguoi_tao_id    BIGINT NOT NULL REFERENCES nhan_vien(id) ON DELETE RESTRICT,
    trang_thai      VARCHAR(20) NOT NULL DEFAULT 'du_thao'
        CHECK (trang_thai IN ('du_thao','hieu_luc','het_han','da_thanh_ly')),
    CONSTRAINT chk_hopdong_doi_tuong CHECK (
        (loai_doi_tac = 'khach_hang' AND khach_hang_id IS NOT NULL AND ncc_id IS NULL) OR
        (loai_doi_tac = 'nha_cung_cap' AND ncc_id IS NOT NULL AND khach_hang_id IS NULL)
    ),
    CONSTRAINT chk_hopdong_ngay CHECK (
        ngay_hieu_luc IS NULL OR ngay_het_han IS NULL OR ngay_het_han >= ngay_hieu_luc
    )
);

CREATE TABLE bao_gia (
    id                  BIGSERIAL PRIMARY KEY,
    ma_bao_gia          VARCHAR(20) NOT NULL UNIQUE,
    khach_hang_id       BIGINT NOT NULL REFERENCES khach_hang(id) ON DELETE RESTRICT,
    nguoi_tao_id        BIGINT NOT NULL REFERENCES nhan_vien(id) ON DELETE RESTRICT,
    ngay_tao            DATE NOT NULL DEFAULT CURRENT_DATE,
    trang_thai          VARCHAR(20) NOT NULL DEFAULT 'nhap'
        CHECK (trang_thai IN ('nhap','da_gui','da_chap_nhan','tu_choi','het_han')),
    ty_le_vat           NUMERIC(5,2) NOT NULL DEFAULT 8 CHECK (ty_le_vat BETWEEN 0 AND 100),
    tong_tien           NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (tong_tien >= 0),
    tien_giam_gia_tong  NUMERIC(18,2) NOT NULL DEFAULT 0 CHECK (tien_giam_gia_tong >= 0),
    file_excel          VARCHAR(255),
    ghi_chu             VARCHAR(255)
);

CREATE TABLE chi_tiet_bao_gia (
    id                  BIGSERIAL PRIMARY KEY,
    bao_gia_id          BIGINT NOT NULL REFERENCES bao_gia(id) ON DELETE CASCADE,
    mat_hang_id         BIGINT NOT NULL REFERENCES mat_hang(id) ON DELETE RESTRICT,
    so_luong            INTEGER NOT NULL CHECK (so_luong > 0),
    don_gia             NUMERIC(18,2) NOT NULL CHECK (don_gia >= 0),
    ty_le_chiet_khau    NUMERIC(5,2)  NOT NULL DEFAULT 0 CHECK (ty_le_chiet_khau BETWEEN 0 AND 100),
    tien_chiet_khau     NUMERIC(18,2)
        GENERATED ALWAYS AS (ROUND(so_luong * don_gia * ty_le_chiet_khau / 100, 2)) STORED,
    thanh_tien          NUMERIC(18,2)
        GENERATED ALWAYS AS (so_luong * don_gia - ROUND(so_luong * don_gia * ty_le_chiet_khau / 100, 2)) STORED
);

-- ============ 12. TÀI CHÍNH ============

CREATE TABLE chi_phi_van_hanh (
    id              BIGSERIAL PRIMARY KEY,
    ma_chi_phi      VARCHAR(20) NOT NULL UNIQUE,
    loai_chi_phi    VARCHAR(30) NOT NULL
        CHECK (loai_chi_phi IN ('van_tai_logistics','nhan_su','bao_tri_thiet_bi','dien_nang_luong','khac')),
    kho_id          BIGINT NOT NULL REFERENCES kho(id) ON DELETE RESTRICT,
    mo_ta           VARCHAR(255),
    so_tien         NUMERIC(18,2) NOT NULL CHECK (so_tien > 0),
    ky              VARCHAR(10) NOT NULL DEFAULT 'thang' CHECK (ky IN ('thang','quy','nam')),
    ngay_chi        DATE NOT NULL,
    nguoi_tao_id    BIGINT REFERENCES nhan_vien(id) ON DELETE SET NULL,
    ghi_chu         VARCHAR(255)
);

CREATE TABLE muc_tieu_doanh_thu (
    id                  BIGSERIAL PRIMARY KEY,
    kho_id              BIGINT REFERENCES kho(id) ON DELETE CASCADE,
    ky                  VARCHAR(10) NOT NULL CHECK (ky IN ('thang','quy','nam')),
    nam_ky              INTEGER NOT NULL,
    thang_ky            SMALLINT,
    so_tien_muc_tieu    NUMERIC(18,2) NOT NULL CHECK (so_tien_muc_tieu >= 0),
    nguoi_tao_id        BIGINT REFERENCES nhan_vien(id) ON DELETE SET NULL,
    ghi_chu             VARCHAR(255),
    CONSTRAINT chk_muctieu_thang CHECK (
        (ky = 'thang' AND thang_ky BETWEEN 1 AND 12) OR
        (ky <> 'thang' AND thang_ky IS NULL)
    )
);

-- ============ 13. THÔNG BÁO, PHÊ DUYỆT, HỆ THỐNG ============

CREATE TABLE thong_bao (
    id              BIGSERIAL PRIMARY KEY,
    loai_thong_bao  VARCHAR(50) NOT NULL,
    tieu_de         VARCHAR(200) NOT NULL,
    noi_dung        TEXT,
    nguoi_nhan_id   BIGINT REFERENCES nhan_vien(id) ON DELETE CASCADE,
    vai_tro_nhan_id BIGINT REFERENCES vai_tro(id) ON DELETE CASCADE,
    doi_tuong_loai  VARCHAR(50),
    doi_tuong_id    BIGINT,
    da_doc          BOOLEAN NOT NULL DEFAULT FALSE,
    thoi_gian_tao   TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_thongbao_nguoinhan CHECK (
        nguoi_nhan_id IS NOT NULL OR vai_tro_nhan_id IS NOT NULL
    )
);

CREATE TABLE phe_duyet (
    id                  BIGSERIAL PRIMARY KEY,
    loai_yeu_cau        VARCHAR(30) NOT NULL
                        CHECK (loai_yeu_cau IN ('giam_gia_dac_biet','huy_hoa_don','khac')),
    doi_tuong_loai      VARCHAR(50) NOT NULL,
    doi_tuong_id        BIGINT NOT NULL,
    nguoi_yeu_cau_id    BIGINT NOT NULL REFERENCES nhan_vien(id) ON DELETE RESTRICT,
    nguoi_duyet_id      BIGINT REFERENCES nhan_vien(id) ON DELETE SET NULL,
    trang_thai          VARCHAR(20) NOT NULL DEFAULT 'cho_duyet'
                        CHECK (trang_thai IN ('cho_duyet','da_duyet','tu_choi')),
    ly_do_yeu_cau       VARCHAR(255),
    ly_do_xu_ly         VARCHAR(255),
    thoi_gian_yeu_cau   TIMESTAMP NOT NULL DEFAULT now(),
    thoi_gian_xu_ly     TIMESTAMP
);

CREATE TABLE nhat_ky_he_thong (
    id              BIGSERIAL PRIMARY KEY,
    nhan_vien_id    BIGINT REFERENCES nhan_vien(id) ON DELETE SET NULL,
    module          VARCHAR(50) NOT NULL,
    hanh_dong       VARCHAR(50) NOT NULL,
    doi_tuong_loai  VARCHAR(50),
    doi_tuong_id    BIGINT,
    thoi_gian       TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE cau_hinh_he_thong (
    id                  BIGSERIAL PRIMARY KEY,
    khoa                VARCHAR(100) NOT NULL UNIQUE,
    gia_tri             VARCHAR(255) NOT NULL,
    mo_ta               VARCHAR(255),
    nguoi_cap_nhat_id   BIGINT REFERENCES nhan_vien(id) ON DELETE SET NULL,
    updated_at          TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE bo_dem_chung_tu (
    loai_chung_tu   VARCHAR(20) NOT NULL,
    nam             INTEGER NOT NULL,
    so_hien_tai     INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (loai_chung_tu, nam)
);

-- Khởi tạo bộ đếm cho mã phiếu tái chế (PTC) của năm hiện tại
INSERT INTO bo_dem_chung_tu (loai_chung_tu, nam, so_hien_tai)
VALUES ('PTC', EXTRACT(YEAR FROM now())::int, 0);

-- ============ 14. INDEXES ============

CREATE INDEX idx_nhanvien_vaitro        ON nhan_vien(vai_tro_id);
CREATE INDEX idx_nhanvien_trangthai     ON nhan_vien(trang_thai);
CREATE INDEX idx_nhanvien_kho           ON nhan_vien(kho_id);
CREATE INDEX idx_nhanvien_bophan        ON nhan_vien(bo_phan_id);
CREATE INDEX idx_phiendangnhap_nv       ON phien_dang_nhap(nhan_vien_id);
CREATE INDEX idx_tokendatlai_nv         ON token_dat_lai_mat_khau(nhan_vien_id);
CREATE INDEX idx_vitriluutru_kho        ON vi_tri_luu_tru(kho_id);
CREATE INDEX idx_phuongtien_taixe       ON phuong_tien(tai_xe_id);
CREATE INDEX idx_tonkho_mathang         ON ton_kho(mat_hang_id);
CREATE INDEX idx_tonkho_kho             ON ton_kho(kho_id);
CREATE INDEX idx_lohang_mathang         ON lo_hang(mat_hang_id);
CREATE INDEX idx_lohang_kho             ON lo_hang(kho_id);
CREATE INDEX idx_lohang_ncc             ON lo_hang(ncc_id);
CREATE INDEX idx_lohang_mucdich         ON lo_hang(muc_dich_nhap) WHERE muc_dich_nhap = 'cho_tai_che';
CREATE INDEX idx_phieutaiche_lohang     ON phieu_tai_che(lo_hang_id);
CREATE INDEX idx_phieutaiche_kho        ON phieu_tai_che(kho_id);
CREATE INDEX idx_chitiettaiche_phieu    ON chi_tiet_tai_che(phieu_tai_che_id);
CREATE INDEX idx_hoadon_khachhang       ON hoa_don(khach_hang_id);
CREATE INDEX idx_hoadon_ncc             ON hoa_don(ncc_id);
CREATE INDEX idx_hoadon_trangthai       ON hoa_don(trang_thai);
CREATE INDEX idx_hoadon_ngaylap         ON hoa_don(ngay_lap);
CREATE INDEX idx_chitiethoadon_hoadon   ON chi_tiet_hoa_don(hoa_don_id);
CREATE INDEX idx_chitiethoadon_mathang  ON chi_tiet_hoa_don(mat_hang_id);
CREATE INDEX idx_phieukho_kho           ON phieu_kho(kho_id);
CREATE INDEX idx_phieukho_hoadon        ON phieu_kho(hoa_don_id);
CREATE INDEX idx_phieukho_phieutaiche   ON phieu_kho(phieu_tai_che_id);
CREATE INDEX idx_chitietphieukho_phieu  ON chi_tiet_phieu_kho(phieu_kho_id);
CREATE INDEX idx_chitietphieukho_mathang ON chi_tiet_phieu_kho(mat_hang_id);
CREATE INDEX idx_chitietphieukho_lo     ON chi_tiet_phieu_kho(lo_id);
CREATE INDEX idx_suachua_mathang        ON sua_chua_pallet(mat_hang_id);
CREATE INDEX idx_suachua_kho            ON sua_chua_pallet(kho_id);
CREATE INDEX idx_banggia_mathang_kh     ON bang_gia(mat_hang_id, khach_hang_id);
CREATE INDEX idx_phienkiemke_kho        ON phien_kiem_ke(kho_id);
CREATE INDEX idx_chitietkiemke_phien    ON chi_tiet_kiem_ke(phien_kiem_ke_id);
CREATE INDEX idx_thanhtoan_hoadon       ON thanh_toan(hoa_don_id);
CREATE INDEX idx_maqr_hoadon            ON ma_qr_thanh_toan(hoa_don_id);
CREATE INDEX idx_webhook_qr             ON webhook_ngan_hang_log(ma_qr_thanh_toan_id);
CREATE INDEX idx_webhook_thanhtoan      ON webhook_ngan_hang_log(thanh_toan_id);
CREATE INDEX idx_lichsuhoadon_hoadon    ON lich_su_hoa_don(hoa_don_id);
CREATE INDEX idx_hopdong_khachhang      ON hop_dong(khach_hang_id);
CREATE INDEX idx_hopdong_ncc            ON hop_dong(ncc_id);
CREATE INDEX idx_baogia_khachhang       ON bao_gia(khach_hang_id);
CREATE INDEX idx_chitietbaogia_baogia   ON chi_tiet_bao_gia(bao_gia_id);
CREATE INDEX idx_chitietbaogia_mathang  ON chi_tiet_bao_gia(mat_hang_id);
CREATE INDEX idx_chiphivanhanh_kho      ON chi_phi_van_hanh(kho_id);
CREATE INDEX idx_muctieu_kho            ON muc_tieu_doanh_thu(kho_id);
CREATE INDEX idx_thongbao_nguoinhan     ON thong_bao(nguoi_nhan_id, da_doc);
CREATE INDEX idx_thongbao_vaitronhan    ON thong_bao(vai_tro_nhan_id, da_doc);
CREATE INDEX idx_pheduyet_doituong      ON phe_duyet(doi_tuong_loai, doi_tuong_id);
CREATE INDEX idx_nhatky_doituong        ON nhat_ky_he_thong(doi_tuong_loai, doi_tuong_id);

CREATE UNIQUE INDEX uq_banggia_dangmo
    ON bang_gia (mat_hang_id, COALESCE(khach_hang_id, 0))
    WHERE ngay_ket_thuc_hieu_luc IS NULL;

-- ============ 15. CHẶN HARD DELETE ============

CREATE OR REPLACE FUNCTION fn_block_hard_delete() RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION
        'Không được xóa cứng bản ghi trong bảng "%": id=%. Hãy UPDATE cột trang_thai thay vì DELETE.',
        TG_TABLE_NAME, OLD.id;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_block_delete_nhan_vien
    BEFORE DELETE ON nhan_vien
    FOR EACH ROW EXECUTE FUNCTION fn_block_hard_delete();

CREATE TRIGGER trg_block_delete_nha_cung_cap
    BEFORE DELETE ON nha_cung_cap
    FOR EACH ROW EXECUTE FUNCTION fn_block_hard_delete();

CREATE TRIGGER trg_block_delete_khach_hang
    BEFORE DELETE ON khach_hang
    FOR EACH ROW EXECUTE FUNCTION fn_block_hard_delete();

CREATE TRIGGER trg_block_delete_mat_hang
    BEFORE DELETE ON mat_hang
    FOR EACH ROW EXECUTE FUNCTION fn_block_hard_delete();

CREATE TRIGGER trg_block_delete_kho
    BEFORE DELETE ON kho
    FOR EACH ROW EXECUTE FUNCTION fn_block_hard_delete();

CREATE OR REPLACE FUNCTION fn_block_edit_hoadon_chotso() RETURNS trigger AS $$
BEGIN
    IF current_setting('pallettrack.allow_edit_locked', true) = 'on' THEN
        RETURN COALESCE(NEW, OLD);
    END IF;
    IF (TG_OP = 'DELETE' AND OLD.da_chot_so) THEN
        RAISE EXCEPTION 'Hóa đơn % đã chốt sổ, không được xóa.', OLD.ma_hoa_don;
    ELSIF (TG_OP = 'UPDATE' AND OLD.da_chot_so) THEN
        RAISE EXCEPTION 'Hóa đơn % đã chốt sổ, không được sửa (cần mở khóa thủ công).', OLD.ma_hoa_don;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_block_edit_hoadon_chotso
    BEFORE UPDATE OR DELETE ON hoa_don
    FOR EACH ROW EXECUTE FUNCTION fn_block_edit_hoadon_chotso();

-- Cùng cơ chế khóa như hóa đơn chốt sổ: phiếu tái chế đã hoàn tất (đã đụng vào
-- tồn kho thật) không được sửa/xóa qua luồng thường, dùng chung session var
-- pallettrack.allow_edit_locked để mở khóa thủ công khi thật sự cần.
CREATE OR REPLACE FUNCTION fn_block_edit_phieutaiche_hoantat() RETURNS trigger AS $$
BEGIN
    IF current_setting('pallettrack.allow_edit_locked', true) = 'on' THEN
        RETURN COALESCE(NEW, OLD);
    END IF;
    IF (TG_OP = 'DELETE' AND OLD.trang_thai = 'da_hoan_tat') THEN
        RAISE EXCEPTION 'Phiếu tái chế % đã hoàn tất, không được xóa.', OLD.ma_phieu;
    ELSIF (TG_OP = 'UPDATE' AND OLD.trang_thai = 'da_hoan_tat') THEN
        RAISE EXCEPTION 'Phiếu tái chế % đã hoàn tất, không được sửa (cần mở khóa thủ công).', OLD.ma_phieu;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_block_edit_phieutaiche_hoantat
    BEFORE UPDATE OR DELETE ON phieu_tai_che
    FOR EACH ROW EXECUTE FUNCTION fn_block_edit_phieutaiche_hoantat();

-- ============ 16. TỰ ĐỘNG CẬP NHẬT updated_at ============

CREATE OR REPLACE FUNCTION fn_set_updated_at() RETURNS trigger AS $$
BEGIN
    NEW.updated_at := now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_set_updated_at_nhan_vien
    BEFORE UPDATE ON nhan_vien
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_set_updated_at_mat_hang
    BEFORE UPDATE ON mat_hang
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_set_updated_at_ton_kho
    BEFORE UPDATE ON ton_kho
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_set_updated_at_hoa_don
    BEFORE UPDATE ON hoa_don
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

CREATE TRIGGER trg_set_updated_at_cau_hinh_he_thong
    BEFORE UPDATE ON cau_hinh_he_thong
    FOR EACH ROW EXECUTE FUNCTION fn_set_updated_at();

-- ============ 17. SINH MÃ CHỨNG TỪ AN TOÀN ĐỒNG THỜI ============

CREATE OR REPLACE FUNCTION fn_lay_ma_chung_tu(
    p_loai_chung_tu VARCHAR,
    p_tien_to       VARCHAR
) RETURNS VARCHAR AS $$
DECLARE
    v_nam INTEGER := EXTRACT(YEAR FROM CURRENT_DATE)::INTEGER;
    v_so  INTEGER;
BEGIN
    INSERT INTO bo_dem_chung_tu (loai_chung_tu, nam, so_hien_tai)
    VALUES (p_loai_chung_tu, v_nam, 1)
    ON CONFLICT (loai_chung_tu, nam)
    DO UPDATE SET so_hien_tai = bo_dem_chung_tu.so_hien_tai + 1
    RETURNING so_hien_tai INTO v_so;

    RETURN p_tien_to || v_nam::text || LPAD(v_so::text, 5, '0');
END;
$$ LANGUAGE plpgsql;

-- ============ 18. SEQUENCE SINH MÃ NHÂN VIÊN (NV0001, NV0002...) ============
CREATE SEQUENCE ma_nv_seq START 1;