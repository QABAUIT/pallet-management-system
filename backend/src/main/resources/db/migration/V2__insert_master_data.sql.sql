-- =====================================================================
-- PALLETTRACK PRO - SEED DATA MẪU
-- Mật khẩu mẫu cho mọi tài khoản demo: "Password@123"
-- Hash bcrypt bên dưới là ví dụ minh họa (10 rounds) - hãy tự sinh lại
-- bằng BCryptPasswordEncoder khi triển khai thật, đừng dùng hash mẫu này ở production.
-- =====================================================================

-- 1. Vai trò
INSERT INTO vai_tro (id, ma_vai_tro, ten_vai_tro, cap_duyet) VALUES
 (1,'GD','Giám đốc',2),
 (2,'PGD','Phó giám đốc',2),
 (3,'NVKHO','Nhân viên kho',0),
 (4,'NVBANHANG','Nhân viên bán hàng',0);
SELECT setval('vai_tro_id_seq', 4);

-- 2. Quyền theo module
INSERT INTO vai_tro_quyen (vai_tro_id, module_code, duoc_xem, duoc_them, duoc_sua, duoc_xoa, duoc_duyet) VALUES
 (1,'bao_cao',       TRUE,TRUE,TRUE,TRUE,TRUE),
 (1,'hoa_don',       TRUE,TRUE,TRUE,TRUE,TRUE),
 (1,'pallet',        TRUE,TRUE,TRUE,TRUE,TRUE),
 (1,'nha_cung_cap',  TRUE,TRUE,TRUE,TRUE,TRUE),
 (1,'nhan_vien',     TRUE,TRUE,TRUE,TRUE,TRUE),
 (1,'khach_hang',    TRUE,TRUE,TRUE,TRUE,TRUE),
 (1,'kho',           TRUE,TRUE,TRUE,TRUE,TRUE),
 (1,'tai_chinh',     TRUE,TRUE,TRUE,TRUE,TRUE),
 (2,'bao_cao',       TRUE,TRUE,TRUE,TRUE,TRUE),
 (2,'hoa_don',       TRUE,TRUE,TRUE,TRUE,TRUE),
 (2,'pallet',        TRUE,TRUE,TRUE,TRUE,TRUE),
 (2,'nha_cung_cap',  TRUE,TRUE,TRUE,TRUE,TRUE),
 (2,'nhan_vien',     TRUE,TRUE,TRUE,TRUE,TRUE),
 (2,'khach_hang',    TRUE,TRUE,TRUE,TRUE,TRUE),
 (2,'kho',           TRUE,TRUE,TRUE,TRUE,TRUE),
 (2,'tai_chinh',     TRUE,TRUE,TRUE,TRUE,TRUE),
 (3,'pallet',        TRUE,TRUE,TRUE,FALSE,FALSE),
 (3,'nha_cung_cap',  TRUE,TRUE,TRUE,FALSE,FALSE),
 (3,'kho',           TRUE,TRUE,TRUE,FALSE,FALSE),
 (3,'thong_tin_chi_tiet', TRUE,FALSE,TRUE,FALSE,FALSE),
 (4,'hoa_don',       TRUE,TRUE,TRUE,FALSE,FALSE),
 (4,'bao_cao',       TRUE,FALSE,FALSE,FALSE,FALSE),
 (4,'khach_hang',    TRUE,TRUE,TRUE,FALSE,FALSE),
 (4,'tai_chinh',     TRUE,TRUE,FALSE,FALSE,FALSE);

-- 3. Bộ phận & ca làm việc
INSERT INTO bo_phan (id, ten_bo_phan) VALUES
 (1,'Ban Giám đốc'),
 (2,'Khối Vận hành Kho bãi'),
 (3,'Khối Kinh doanh - Bán hàng'),
 (4,'Tài chính - Kế toán');
SELECT setval('bo_phan_id_seq', 4);

INSERT INTO ca_lam_viec (id, ten_ca, gio_bat_dau, gio_ket_thuc) VALUES
 (1,'Ca 1 (Sáng)','06:00','14:30'),
 (2,'Ca 2 (Chiều)','14:30','22:00'),
 (3,'Ca Hành chính','08:00','17:00');
SELECT setval('ca_lam_viec_id_seq', 3);

-- 4. Kho (chưa gắn quan_ly_id vội vì nhan_vien chưa tồn tại)
INSERT INTO kho (id, ma_kho, ten_kho, dia_chi, loai_dia_diem, dien_tich_m2, trang_thai) VALUES
 (1,'KHO-TB','Kho Tổng Tân Bình','Đường số 7, KCN Tân Bình, TP.HCM','kho',20000,'hoat_dong'),
 (2,'KHO-BD','Kho Chi nhánh Bình Dương','Khu CN Sóng Thần, Bình Dương','kho',8000,'hoat_dong');
SELECT setval('kho_id_seq', 2);

-- 5. Nhân viên (mật khẩu demo: Password@123)
INSERT INTO nhan_vien (id, ma_nv, ho_ten, email, sdt, ngay_sinh, gioi_tinh, dia_chi,
    vai_tro_id, kho_id, bo_phan_id, chuc_vu, loai_hop_dong, so_hop_dong,
    ten_dang_nhap, mat_khau_hash, ngay_vao_lam, ngay_nghi_viec, trang_thai) VALUES
 (1,'NV-GD-001','Âu Hoàng Hải','auhoanghai@hoangphatpallet.vn','0909832981','1978-05-10','Nam',
   'S23 KP2, Tôn Thất Thuyết, P.Xóm Chiếu, TP.HCM',
   1,1,1,'Giám đốc','Không xác định thời hạn','HDLD-2020-001',
   'au.hoanghai', '$2a$10$7EqJtq98hPqEX7fNZaFWoO6L1LqXlOMbXjF3RLXFPuKmb2G.9LhCK',
   '2020-01-01', NULL, 'dang_lam_viec'),
 (2,'NV-KHO-042','Nguyễn Văn Hùng','hung.nv@hoangphatpallet.vn','0938123456','1990-03-15','Nam',
   'Q.Tân Bình, TP.HCM',
   3,1,2,'Tổ trưởng kho ca sáng','Xác định thời hạn 2 năm','HDLD-2023-042',
   'hung.kho', '$2a$10$7EqJtq98hPqEX7fNZaFWoO6L1LqXlOMbXjF3RLXFPuKmb2G.9LhCK',
   '2023-06-01', NULL, 'dang_lam_viec'),
 (3,'NV-BH-015','Trần Hải Nam','nam.tran@hoangphatpallet.vn','0977888999','1995-07-20','Nam',
   'Q.4, TP.HCM',
   4,1,3,'Nhân viên kinh doanh','Xác định thời hạn 1 năm','HDLD-2024-015',
   'nam.tran', '$2a$10$7EqJtq98hPqEX7fNZaFWoO6L1LqXlOMbXjF3RLXFPuKmb2G.9LhCK',
   '2024-02-01', NULL, 'dang_lam_viec'),
 (4,'NV-KHO-007','Lê Thị Mai','mai.le@hoangphatpallet.vn','0912345678','1992-11-02','Nu',
   'Q.Bình Tân, TP.HCM',
   3,1,2,'Nhân viên kho','Thời vụ','HDLD-2022-007',
   'mai.le', '$2a$10$7EqJtq98hPqEX7fNZaFWoO6L1LqXlOMbXjF3RLXFPuKmb2G.9LhCK',
   '2022-03-10', '2026-01-15', 'da_nghi_viec');
SELECT setval('nhan_vien_id_seq', 4);

UPDATE kho SET quan_ly_id = 2 WHERE id = 1;

-- 6. Lịch làm việc mẫu
INSERT INTO lich_lam_viec (nhan_vien_id, ngay, ca_lam_viec_id, kho_id, nguoi_xep_lich_id, ghi_chu) VALUES
 (2,'2026-10-01',1,1,1,NULL),
 (2,'2026-10-02',2,1,1,'Đổi ca theo lịch tuần'),
 (4,'2026-10-01',3,1,1,NULL);

-- 7. Vị trí lưu trữ & phương tiện
INSERT INTO vi_tri_luu_tru (id, kho_id, khu_vuc, ke, tang, ma_vi_tri) VALUES
 (1,1,'A','04','2','TB-A04-T2'),
 (2,1,'B','01','1','TB-B01-T1'),
 (3,2,'A','01','1','BD-A01-T1');
SELECT setval('vi_tri_luu_tru_id_seq', 3);

INSERT INTO phuong_tien (id, bien_so, loai_xe, tai_xe_id, trang_thai) VALUES
 (1,'51C-123.45','Xe tải 5 tấn',2,'hoat_dong');
SELECT setval('phuong_tien_id_seq', 1);

-- 8. Nhà cung cấp & khách hàng (dữ liệu thật từ hồ sơ công ty)
INSERT INTO nha_cung_cap (id, ma_ncc, ten_ncc, mst, dia_chi, nguoi_lien_he, nhom_hang, ghi_chu, trang_thai, created_by) VALUES
 (1,'NCC-001','Công ty TNHH Gỗ Đại Phú Minh','3604012606',
   'Số 29, đường 7, ấp Trà Cổ, xã Bình Minh, Đồng Nai', NULL,
   'Gỗ tràm xẻ, thanh gỗ các loại','Cung cấp nguyên liệu gỗ đầu vào','dang_hop_tac',1),
 (2,'NCC-002','Công ty TNHH SX TM DV Mai Lợi','3603438751',
   'Đường N10 KDC An Thuận, ấp Xóm Gốc, xã Long Thành, Đồng Nai','Phạm Văn Lợi',
   'Pallet nhựa','Giá 200.000đ/pallet chưa VAT, đối chiếu công nợ cuối tháng','dang_hop_tac',1);
SELECT setval('nha_cung_cap_id_seq', 2);

INSERT INTO khach_hang (id, ma_kh, ten_kh, loai_kh, trang_thai, created_by) VALUES
 (1,'KH-001','Công ty CP Nông sản thực phẩm Quảng Ngãi','doanh_nghiep','dang_hop_tac',1);
SELECT setval('khach_hang_id_seq', 1);

-- 9. Thông tin công ty
INSERT INTO company (id, ten_cong_ty, mst, dia_chi, sdt, logo,
    so_tai_khoan_ngan_hang, ten_ngan_hang, chu_tai_khoan, nguoi_dai_dien_phap_ly, ty_le_vat_mac_dinh) VALUES
 (1,'CÔNG TY TNHH TM SX PALLET HOÀNG PHÁT','0319071929',
   'S23, Khu phố 2, Tôn Thất Thuyết, Phường Xóm Chiếu, TP.HCM','0903357440 / 0909832981',
   'assets/logo/ICON_Pallet.png','31459977','TMCP Á Châu (ACB) - TP.HCM',
   'CÔNG TY TNHH TM SX PALLET HOÀNG PHÁT','Âu Hoàng Hải',0.08);

-- 10. Mặt hàng (đúng theo bảng báo giá thật)
INSERT INTO mat_hang (id, ma_mat_hang, ten_mat_hang, loai_mat_hang, chat_lieu,
    kich_thuoc_dai, kich_thuoc_rong, kich_thuoc_cao, tai_trong_tinh, tai_trong_dong,
    tieu_chuan, ncc_mac_dinh_id, don_gia_ban, mo_ta, trang_thai, created_by) VALUES
 (1,'PL-GO-01','Pallet Gỗ','pallet','go',1000,1000,150,1500,800,
   'Đã phun trùng',1,150000,'Gỗ đẹp, chắc chắn, đã bao gồm phun trùng','dang_kinh_doanh',1),
 (2,'PL-NH-02','Pallet Nhựa','pallet','nhua',1000,1200,150,2000,1000,
   NULL,NULL,260000,NULL,'dang_kinh_doanh',1),
 (3,'PL-PN-03','Pallet phun nhiệt','pallet','go',1000,1200,150,1800,900,
   'Xử lý nhiệt ISPM-15',1,260000,NULL,'dang_kinh_doanh',1),
 (4,'LK-VG-04','Vĩ gỗ','linh_kien','go',NULL,NULL,NULL,NULL,NULL,
   NULL,NULL,80000,'Vĩ gỗ rời','dang_kinh_doanh',1);
SELECT setval('mat_hang_id_seq', 4);

-- 11. Tồn kho
INSERT INTO ton_kho (mat_hang_id, kho_id, vi_tri_id, so_luong_ton_kho, so_luong_da_giu_cho, so_luong_toi_thieu) VALUES
 (1,1,1,850,50,100),
 (2,1,2,420,0,80),
 (3,1,1,60,0,100),   -- dưới ngưỡng tối thiểu -> minh họa cảnh báo tồn kho thấp
 (4,2,3,300,0,50);

-- 12. Lô hàng
INSERT INTO lo_hang (id, ma_lo, mat_hang_id, kho_id, so_luong_nhap, so_luong_con_lai, ngay_nhap, ncc_id, trang_thai) VALUES
 (1,'TB-881',1,1,500,300,'2026-09-01',1,'con_hang');
SELECT setval('lo_hang_id_seq', 1);

-- 13. Hóa đơn bán hàng mẫu (đã thanh toán qua QR)
INSERT INTO hoa_don (id, ma_hoa_don, loai_giao_dich, khach_hang_id, kho_id, nhan_vien_lap_id, ngay_lap,
    trang_thai, tong_tien_hang, phi_giao_hang, ty_le_vat, tien_vat, tien_truoc_thue, tong_thanh_toan,
    tien_khach_dua, tien_thoi_lai, phuong_thuc_thanh_toan, han_thanh_toan, ngay_thanh_toan,
    da_chot_so, file_pdf) VALUES
 (1,'HD-2026-0001','thu_ban_pallet',1,1,3,'2026-05-20 10:00',
   'da_thanh_toan',28000000,500000,8,2280000,28500000,30780000,
   31000000,220000,'chuyen_khoan','2026-06-19','2026-05-25 09:12',
   TRUE,'invoices/2026/HD-2026-0001.pdf');
SELECT setval('hoa_don_id_seq', 1);

INSERT INTO chi_tiet_hoa_don
    (hoa_don_id, mat_hang_id, so_luong, don_gia)
VALUES
    (1,1,100,150000),
    (1,2,50,260000);

-- 14. Phiếu nhập kho mẫu
INSERT INTO phieu_kho (id, ma_phieu, loai_phieu, kho_id, vi_tri_id, nhan_vien_giao_nhan_id,
    nguoi_duyet_id, bien_so_xe_doi_tac, so_bien_ban_kiem_tra, ngay_gio, ghi_chu, trang_thai) VALUES
 (1,'PN-2026-0001','nhap_kho',1,1,2,1,'60C-892.41','BB-2026-001','2026-09-01 09:00',
   'Nhập từ NCC Đại Phú Minh','da_hoan_thanh');
SELECT setval('phieu_kho_id_seq', 1);

INSERT INTO chi_tiet_phieu_kho (phieu_kho_id, mat_hang_id, lo_id, so_luong) VALUES
 (1,1,1,500);

-- 15. Thanh toán, QR, webhook
INSERT INTO thanh_toan (id, hoa_don_id, phuong_thuc, so_tien, ma_giao_dich_ngan_hang,
    trang_thai, tu_dong_xac_nhan, thoi_gian) VALUES
 (1,1,'chuyen_khoan',30780000,'FT26052500123456','thanh_cong',TRUE,'2026-05-25 09:12');
SELECT setval('thanh_toan_id_seq', 1);

INSERT INTO ma_qr_thanh_toan (id, hoa_don_id, noi_dung_qr, so_tien, trang_thai,
    thoi_gian_tao, thoi_gian_het_han, thoi_gian_thanh_toan) VALUES
 (1,1,'00020101021238570010A00000072...',30780000,'da_thanh_toan',
   '2026-05-25 08:00','2026-05-25 20:00','2026-05-25 09:12');
SELECT setval('ma_qr_thanh_toan_id_seq', 1);

INSERT INTO webhook_ngan_hang_log (ma_qr_thanh_toan_id, thanh_toan_id, payload_tho, da_xu_ly, thoi_gian_nhan) VALUES
 (1,1,'{"amount":30780000,"content":"HD20260001","bank":"ACB"}'::jsonb, TRUE,'2026-05-25 09:11');

INSERT INTO lich_su_hoa_don (hoa_don_id, nguoi_sua_id, thoi_gian, hanh_dong, truong_thay_doi, gia_tri_cu, gia_tri_moi, ly_do) VALUES
 (1,3,'2026-05-20 10:05','tao_moi',NULL,NULL,NULL,'Lập hóa đơn bán hàng'),
 (1,1,'2026-05-25 09:12','cap_nhat','trang_thai','cho_duyet','da_thanh_toan',
   'Khách chuyển khoản qua QR, hệ thống tự xác nhận');

-- 16. Hợp đồng & báo giá mẫu
INSERT INTO hop_dong (id, ma_hop_dong, loai_doi_tac, ncc_id, ngay_ky, ngay_hieu_luc, ngay_het_han,
    noi_dung, file_dinh_kem, nguoi_tao_id, trang_thai) VALUES
 (1,'06/APFCO-HOANGPHAT','nha_cung_cap',2,'2026-03-11','2026-03-11','2026-12-31',
   'Hợp đồng mua bán pallet nhựa, đơn giá 200.000đ/pallet chưa VAT, thanh toán đối chiếu cuối tháng',
   'contracts/2026/HD-NCC-06.docx',1,'hieu_luc');
SELECT setval('hop_dong_id_seq', 1);

INSERT INTO bao_gia (id, ma_bao_gia, khach_hang_id, nguoi_tao_id, ngay_tao, trang_thai,
    ty_le_vat, tong_tien, file_excel, ghi_chu) VALUES
 (1,'BG-2026-0014',1,3,'2026-05-14','da_gui',8,93000000,
   'quotes/2026/BG-2026-0014.xlsx','Giá chưa bao gồm VAT 8%');
SELECT setval('bao_gia_id_seq', 1);

INSERT INTO chi_tiet_bao_gia
(bao_gia_id, mat_hang_id, so_luong, don_gia)
VALUES
(1,1,200,150000),
(1,2,100,260000),
(1,3,50,260000),
(1,4,300,80000);

-- 17. Chi phí vận hành & mục tiêu doanh thu
INSERT INTO chi_phi_van_hanh (id, ma_chi_phi, loai_chi_phi, kho_id, mo_ta, so_tien, ky, ngay_chi, nguoi_tao_id) VALUES
 (1,'CP-2026-09-001','dien_nang_luong',1,'Tiền điện tháng 9/2026',8500000,'thang','2026-09-30',1),
 (2,'CP-2026-09-002','nhan_su',1,'Lương nhân viên kho tháng 9',45000000,'thang','2026-09-30',1);
SELECT setval('chi_phi_van_hanh_id_seq', 2);

INSERT INTO muc_tieu_doanh_thu (kho_id, ky, nam_ky, thang_ky, so_tien_muc_tieu, nguoi_tao_id, ghi_chu) VALUES
 (1,'thang',2026,9,500000000,1,'Mục tiêu tháng 9 - Kho Tân Bình'),
 (NULL,'nam',2026,NULL,6000000000,1,'Mục tiêu cả năm toàn công ty');

-- 18. Thông báo & phê duyệt mẫu
INSERT INTO thong_bao (loai_thong_bao, tieu_de, noi_dung, vai_tro_nhan_id, doi_tuong_loai, doi_tuong_id, da_doc, thoi_gian_tao) VALUES
 ('canh_bao_ton_kho_thap','Sắp hết Pallet phun nhiệt',
  'Tồn kho Pallet phun nhiệt tại Kho Tổng Tân Bình chỉ còn 60 cái, dưới ngưỡng tối thiểu 100.',
  3,'mat_hang',3,FALSE,'2026-09-21 08:00');

INSERT INTO thong_bao (loai_thong_bao, tieu_de, noi_dung, nguoi_nhan_id, doi_tuong_loai, doi_tuong_id, da_doc, thoi_gian_tao) VALUES
 ('don_hang_can_lap_hoa_don','Đơn hàng HD-2026-0001 chờ lập hóa đơn',
  'Khách Quảng Ngãi đã xác nhận đơn, vui lòng lập hóa đơn.',
  3,'hoa_don',1,TRUE,'2026-05-20 09:50');

INSERT INTO phe_duyet (loai_yeu_cau, doi_tuong_loai, doi_tuong_id, nguoi_yeu_cau_id, nguoi_duyet_id,
    trang_thai, ly_do_yeu_cau, ly_do_xu_ly, thoi_gian_yeu_cau, thoi_gian_xu_ly) VALUES
 ('giam_gia_dac_biet','hoa_don',1,3,1,'da_duyet',
  'Khách quen mua số lượng lớn, xin giảm 3%','Đồng ý giảm giá vì khách VIP',
  '2026-05-19 14:00','2026-05-19 15:00');

-- 19. Cấu hình hệ thống & bộ đếm chứng từ
INSERT INTO cau_hinh_he_thong (khoa, gia_tri, mo_ta, nguoi_cap_nhat_id) VALUES
 ('nguong_hoa_don_lon','200000000','Ngưỡng cảnh báo hóa đơn giá trị lớn bất thường',1),
 ('so_ngay_canh_bao_cong_no','15','Số ngày trước hạn thanh toán để nhắc nhở',1);

INSERT INTO bo_dem_chung_tu (loai_chung_tu, nam, so_hien_tai) VALUES
 ('HD',2026,1),
 ('BG',2026,14),
 ('PK',2026,1),
 ('HDNCC',2026,6);