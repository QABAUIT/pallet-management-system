  # Phân tích nghiệp vụ hệ thống PALLET từ mã nguồn

  > Mục đích: mô tả **đúng những gì mã nguồn đang làm**, làm cơ sở cấu hình một hệ thống ERP tương đương.
  > Không đề xuất cải tiến, không suy đoán. Chỗ nào không có xử lý trong code được ghi rõ **"không có trong code"**.

  - Mã nguồn tại thời điểm lập: nhánh `taiche`, commit `00283b1` (2026-11-07).
  - Backend: Spring Boot, `backend/src/main/java/com/pallet/backend`.
  - Cơ sở dữ liệu: PostgreSQL, migration Flyway `backend/src/main/resources/db/migration/V1..V12`.
  - Frontend: React, `frontend/src`.

  ## 0. Quy ước đọc tài liệu

  ### 0.1. Cách ghi căn cứ

  Mỗi hành vi được ghi kèm `đường_dẫn/tên_file.java` + tên hàm (và số dòng khi cần). Với SQL là file migration + số dòng. Khi một bước chỉ có ở giao diện, ghi tên component/page frontend.

  ### 0.2. Ý nghĩa "không có trong code"

  Không tìm thấy service, controller, endpoint, đoạn xử lý, ràng buộc DB hoặc màn hình tương ứng. Điều này **không** có nghĩa là nghiệp vụ đó bị cấm trên thực tế; chỉ có nghĩa hệ thống hiện tại không ghi nhận hoặc không thực hiện.

  ### 0.3. Mã hàng hệ thống dùng xuyên suốt

  | Mã | Tên | Loại | Đơn vị | Vai trò trong luồng |
  |---|---|---|---|---|
  | `PL-CPL-05` | Pallet gỗ cũ chờ phân loại | `cho_tai_che` | cái | Hàng mua lô cũ nhập kho, đầu vào phân loại |
  | `PL-CTD-06` | Pallet gỗ cũ chờ tháo dỡ | `cho_tai_che` | cái | Kết quả phân loại nhóm "tháo dỡ" |
  | `PL-DC-07` | Pallet gỗ cũ đạt chuẩn | `pallet` | cái | Kết quả phân loại nhóm "bán lại" |
  | `PL-TC-08` | Pallet tái chế | `pallet` | cái | Thành phẩm của phiếu đóng pallet |
  | `LK-TN-09` | Thanh gỗ nguyên (thu hồi) | `linh_kien` | cái | Kết quả tháo dỡ / xử lý thêm |
  | `LK-CV-10` | Cục gỗ vuông (thu hồi) | `linh_kien` | cái | Kết quả tháo dỡ / xử lý thêm |
  | `LK-TX-11` | Thanh gỗ cần xử lý thêm | `linh_kien` | cái | Nguồn của phiếu xử lý thêm |
  | `LK-CX-12` | Cục gỗ cần xử lý thêm | `linh_kien` | cái | Nguồn của phiếu xử lý thêm |
  | `LK-TM-13` | Thanh gỗ mới (vật tư) | `linh_kien` | cái | Vật tư mới mua ngoài |
  | `LK-CM-14` | Cục gỗ mới (vật tư) | `linh_kien` | cái | Vật tư mới mua ngoài |
  | `PE-GO-15` | Phế liệu gỗ | `phe_lieu` | kg | Phế liệu thu hồi, bán theo kg |
  | `DV-TD-99` | Dịch vụ tháo dỡ pallet | `dich_vu` | — | Dòng hàng của hóa đơn dịch vụ |

  Nguồn: `V4__don_mua_va_phan_loai_lo.sql` dòng 18–31; `V9__hoa_don_dich_vu_thao_do.sql` dòng 8–9; `V7__dinh_muc_dong_pallet.sql` dòng 69–79.

  ### 0.4. Nền chung về tồn kho

  - `ton_kho` chỉ có **một dòng cho mỗi cặp (mặt hàng, kho)**, UNIQUE `(mat_hang_id, kho_id)`; số lượng là `INTEGER`, có `so_luong_ton_kho`, `so_luong_da_giu_cho`, `so_luong_toi_thieu`.
  - Ràng buộc DB: `so_luong_ton_kho >= 0` và `so_luong_ton_kho >= so_luong_da_giu_cho` (`V1__initial_schema.sql` dòng 220–230).
  - Mọi nghiệp vụ cộng/trừ tồn đi qua `TonKhoNghiepVu.cong` / `TonKhoNghiepVu.tru`. Hàm `tru` tính khả dụng = `ton_kho - da_giu_cho` và chặn nếu không đủ (`service/TonKhoNghiepVu.java` dòng 24–53).
  - Lô hàng (`lo_hang`) lưu `so_luong_nhap`, `so_luong_con_lai`, `don_gia_nhap`, `muc_dich_nhap` (`ban_thanh_pham` hoặc `cho_tai_che`). Lô **không phải** là chiều lưu trữ tồn kho: tồn vẫn gộp theo mặt hàng + kho. `chi_tiet_phieu_kho.lo_id` là tùy chọn.
  - Phiếu kho (`phieu_kho`) có 4 loại: `nhap_kho`, `xuat_kho`, `chuyen_kho`, `dieu_chinh_kiem_ke`; trạng thái `cho_xu_ly`, `da_hoan_thanh`, `da_huy`; cột `la_he_thong` đánh dấu phiếu do hệ thống tự sinh (`V1__initial_schema.sql` dòng 349–368).
  - Các bước "duyệt" của phân loại/tháo dỡ/xử lý thêm/đóng pallet **không tạo phiếu nháp**: chúng tạo ngay một phiếu xuất và một phiếu nhập `da_hoan_thanh`, `la_he_thong = true`, rồi gọi trực tiếp `TonKhoNghiepVu` để đổi tồn.

  ### 0.5. Nền chung về vai trò và phê duyệt

  - 4 vai trò seed: `GD` (Giám đốc, `cap_duyet=2`), `PGD` (Phó giám đốc, `cap_duyet=2`), `NVKHO` (Nhân viên kho, `cap_duyet=0`), `NVBANHANG` (Nhân viên bán hàng, `cap_duyet=0`) — `V2__insert_master_data.sql` dòng 4–9.
  - Phân quyền thực tế khi chạy gồm hai lớp:
    1. `@PreAuthorize("hasAnyRole(...)")` trên controller.
    2. `PhanQuyenKho.kiemTra(nv, kho)`: `GD/PGD` được mọi kho; các vai trò còn lại chỉ kho được gán ở `nhan_vien.kho_id` (`service/PhanQuyenKho.java` dòng 11–25).
  - Bảng `vai_tro_quyen` (ma trận module × xem/them/sua/xoa/duyet) có dữ liệu seed nhưng **không được code đọc để cấp quyền**. Không có endpoint/service nào kiểm tra bảng này.
  - Bảng `phe_duyet` (yêu cầu duyệt giảm giá đặc biệt/hủy hóa đơn/khác) tồn tại trong schema (`V1` dòng 598–612) nhưng **không có service/controller/UI nào sử dụng**.
  - Các action "duyệt" chỉ kiểm tra role `GD/PGD` ở controller; không kiểm tra `cap_duyet`, không tạo bản ghi `phe_duyet`, không có bước duyệt nhiều cấp.

  ### 0.6. Bản đồ 13 quy trình và màn hình

  | # | Quy trình | Service/Controller chính | Màn hình chính |
  |---|---|---|---|
  | 1 | Mua pallet gỗ cũ theo lô | `DonMuaServiceImpl`, `DonMuaController` | `DonMuaFormModal.jsx`, `NhanHangModal.jsx` |
  | 2 | Mua pallet thường và vật tư mới | `DonMuaServiceImpl`, `PhieuNhapServiceImpl` | `DonMuaFormModal.jsx`, `ReceiptCreate.jsx` |
  | 3 | Nhận hàng, kiểm tra, phân loại | `PhieuTaiCheServiceImpl`, `QuyTacTaiChe` | `PhanLoaiFormModal.jsx` |
  | 4 | Chuyển kho và kiểm kê | `PhieuNhapServiceImpl.xepViTri`, `KiemKeServiceImpl` | `ImportInventory.jsx`, `Stocktaking.jsx` |
  | 5 | Tháo dỡ pallet cũ nội bộ | `PhieuThaoDoServiceImpl`, `QuyTacTaiChe` | `ThaoDoFormModal.jsx` |
  | 6 | Phân loại linh kiện thu hồi | `PhieuXuLyThemServiceImpl`, `QuyTacTaiChe` | `XuLyThemFormModal.jsx` |
  | 7 | Lắp ráp pallet tái chế | `PhieuDongPalletServiceImpl`, `QuyTacDongPallet`, `DinhMucServiceImpl` | `DongPalletFormModal.jsx`, `DinhMucFormModal.jsx` |
  | 8 | Bán pallet nguyên trạng và pallet mới | `HoaDonServiceImpl`, `BaoGiaBanHangService` | màn hình bán hàng/hóa đơn, báo giá |
  | 9 | Bán pallet tái chế | `HoaDonServiceImpl` | như quy trình 8 |
  | 10 | Bán phế liệu gỗ | `BanPheLieuService` | `BanPheLieu.jsx` |
  | 11 | Dịch vụ tháo dỡ cho khách | `PhieuDichVuThaoDoServiceImpl`, `DichVuThaoDoTaiChinhService` | `DichVuKhach.jsx`, `DichVuModals.jsx` |
  | 12 | Hóa đơn và thanh toán nhà cung cấp | **không có trong code** | **không có trong code** |
  | 13 | Hóa đơn và thanh toán khách hàng | `HoaDonServiceImpl` | màn hình hóa đơn, chi tiết khách hàng |

  ---

  ## 1. Mua pallet gỗ cũ theo lô

  ### (a) Các bước theo thứ tự

  1. **GD/PGD/NVKHO** mở màn hình đơn mua, bấm tạo đơn. Form gọi `GET /api/v1/don-mua/danh-muc`: trả về kho đang hoạt động (NVKHO chỉ thấy kho của mình), nhà cung cấp `dang_hop_tac`, mặt hàng `dang_kinh_doanh` **trừ** `phe_lieu`; kho mặc định là kho của nhân viên đang đăng nhập (`DonMuaServiceImpl.layDanhMuc`, dòng 100–129).
  2. Chọn nhà cung cấp, kho, ngày dự kiến nhận (tùy chọn), ghi chú; thêm dòng hàng: mặt hàng, số lượng đặt, đơn giá. Pallet cũ phải là mặt hàng loại `cho_tai_che` và chất liệu `go`.
  3. Bấm lưu → `POST /api/v1/don-mua`. Hệ thống sinh mã `DM-...`, trạng thái `nhap`, tính `tong_tien` (xem (d)).
  4. **GD/PGD** bấm xác nhận → `POST /api/v1/don-mua/{id}/xac-nhan`; lưu `nguoi_xac_nhan_id`, chuyển `da_xac_nhan`. Chỉ xác nhận được đơn `nhap`, có ít nhất 1 dòng, nhà cung cấp còn hợp tác.
  5. Khi hàng về, **GD/PGD/NVKHO** bấm nhận hàng → `POST /api/v1/don-mua/{id}/nhan-hang`. Chọn từng dòng và số lượng nhận (có thể nhận một phần), nhập biển số xe/ghi chú.
  6. Hệ thống tạo **một phiếu nhập PN** trạng thái `da_hoan_thanh`, mỗi dòng nhận tạo **một lô hàng** `LO-...` với `so_luong_nhap = so_luong_con_lai = so_luong_nhan`, `don_gia_nhap = đơn giá đặt`, `muc_dich_nhap` phụ thuộc loại mặt hàng; cộng tồn kho ngay; cập nhật số đã nhận; đơn chuyển `da_nhan_mot_phan` hoặc `da_nhan_du` (`DonMuaServiceImpl.nhanHang`, dòng 248–329).
  7. **Không có bước lập hóa đơn mua, thanh toán nhà cung cấp, công nợ phải trả trong code.**

  ### (b) Chứng từ/record tạo ra và trạng thái

  | Record | Bảng | Trạng thái | Ghi chú |
  |---|---|---|---|
  | Đơn mua | `don_mua` | `nhap` → `da_xac_nhan` → `da_nhan_mot_phan` → `da_nhan_du`; nhánh `da_huy` | Mã `DM-`; `tong_tien` theo số đặt, không đổi khi nhận một phần |
  | Dòng đơn mua | `chi_tiet_don_mua` | không có trạng thái riêng | `so_luong_dat`, `so_luong_da_nhan`, `don_gia` |
  | Phiếu nhập | `phieu_kho` (`loai_phieu=nhap_kho`) | tạo thẳng `da_hoan_thanh` | Có `don_mua_id`, `la_he_thong` mặc định false |
  | Lô hàng | `lo_hang` | `con_hang`; `da_het` khi còn 0 | `muc_dich_nhap=cho_tai_che` nếu mặt hàng loại `cho_tai_che`, ngược lại `ban_thanh_pham` |
  | Tồn kho | `ton_kho` | không có trạng thái | Cộng số nhận vào đúng kho của đơn |
  | Nhật ký | `nhat_ky_he_thong` | — | Ghi `tao_moi`, `xac_nhan`, `nhan_hang`, `huy` |
  | Hóa đơn mua/thanh toán NCC | — | — | **không có trong code** |

  ### (c) Dữ liệu đầu vào

  **Tạo/sửa đơn mua — `TaoDonMuaRequest`:**

  | Trường | Kiểu | Bắt buộc | Mặc định | Ghi chú |
  |---|---|---|---|---|
  | `nccId` | Long | Có (`@NotNull`) | — | Phải `dang_hop_tac` |
  | `khoId` | Long | Không | Kho của nhân viên | NVKHO chỉ được kho mình |
  | `ngayDuKienNhan` | LocalDate | Không | null | Nếu có phải >= ngày đặt |
  | `ghiChu` | String (255) | Không | null | Cắt khoảng trắng |
  | `chiTiet` | List | Có trên thực tế | — | Danh sách rỗng bị chặn khi xác nhận; code không có `@NotNull` trên list |
  | `chiTiet[].matHangId` | Long | Có | — | Phải đang kinh doanh, không phải `phe_lieu`, nếu `cho_tai_che` phải chất liệu gỗ |
  | `chiTiet[].soLuong` | Integer | Có | — | > 0 |
  | `chiTiet[].donGia` | BigDecimal | Có | — | >= 0 |

  **Nhận hàng — `NhanHangDonMuaRequest`:**

  | Trường | Kiểu | Bắt buộc | Mặc định | Ghi chú |
  |---|---|---|---|---|
  | `bienSoXe` | String (20) | Không | null | Cắt khoảng trắng |
  | `ghiChu` | String | Không | null | Ghép vào ghi chú phiếu nhập |
  | `chiTiet[].chiTietId` | Long | Có | — | Phải thuộc đơn mua |
  | `chiTiet[].soLuongNhan` | Integer | Có | — | > 0 và <= số đặt − số đã nhận |
  | `chiTiet[].ghiChu` | String | Không | null | Ghi chú dòng phiếu nhập |

  ### (d) Công thức số lượng/giá/tồn

  - Tổng đơn mua: `tong_tien = Σ(so_luong_dat × don_gia)` — `DonMuaServiceImpl.tinhTong`, dòng 363–367.
  - Số còn được nhận của dòng: `so_luong_dat − so_luong_da_nhan` — `nhanHang`, dòng 273.
  - Trạng thái nhận đủ: tất cả dòng có `so_luong_da_nhan >= so_luong_dat` — dòng 322–323.
  - Tồn kho sau nhận: `ton_kho += so_luong_nhan` — `TonKhoNghiepVu.cong`, được gọi tại dòng 317.
  - Lô: `so_luong_nhap = so_luong_con_lai = so_luong_nhan`; `don_gia_nhap = don_gia` của dòng đơn mua — dòng 299–309.
  - **Không có** công thức thuế, phí vận chuyển, chiết khấu, tỷ giá, chi phí phụ trên đơn mua. `tong_tien` chỉ là số lượng × đơn giá.

  ### (e) Ràng buộc và kiểm tra

  - Nhà cung cấp phải tồn tại và `dang_hop_tac` khi tạo, sửa, xác nhận (`timNccHopTac`, dòng 395–402).
  - Mặt hàng phải `dang_kinh_doanh`; `phe_lieu` bị chặn; `cho_tai_che` chỉ nhận chất liệu `go`; trùng mặt hàng trong đơn bị chặn (`taoDong`, dòng 333–361).
  - Số lượng đặt > 0, đơn giá >= 0 (Bean Validation trên DTO).
  - Ngày dự kiến nhận không trước ngày đặt (`kiemTraNgayDuKien`, dòng 369–373).
  - Chỉ sửa đơn `nhap`; chỉ xác nhận đơn `nhap`; chỉ hủy đơn `nhap`/`da_xac_nhan` và chưa nhận dòng nào; hủy bắt buộc lý do (`capNhat` dòng 164–200, `xacNhan` 202–219, `huy` 223–242).
  - Chỉ nhận khi đơn `da_xac_nhan` hoặc `da_nhan_mot_phan`; không nhận vượt số còn lại; một dòng không được gửi 2 lần (`nhanHang`, dòng 251–278).
  - Quyền kho: `PhanQuyenKho.kiemTra` khi tạo/sửa/nhận; xác nhận/hủy chỉ GD/PGD ở controller.
  - DB: `don_mua.trang_thai` chỉ nhận 5 giá trị; `tong_tien >= 0`; `lo_hang.so_luong_con_lai <= so_luong_nhap` (`V4` dòng 43–45; `V1` dòng 241–249).

  ### (f) Ngoại lệ đã xử lý

  - Nhà cung cấp ngừng hợp tác sau khi lập đơn: chặn xác nhận, nhưng **không chặn nhận hàng** (hàm `nhanHang` không kiểm tra trạng thái NCC).
  - Nhận một phần: đơn chuyển `da_nhan_mot_phan`; được nhận tiếp; khi đủ chuyển `da_nhan_du`.
  - Gửi trùng dòng nhận hoặc vượt số còn lại: báo lỗi, không ghi gì (kiểm tra toàn bộ trước khi tạo phiếu).
  - Hai người nhận cùng lúc: `khoaDonMua` dùng khóa bi quan trên đơn (`findForUpdate`), tuần tự hóa.
  - Hủy sau khi đã nhận: bị chặn.
  - Nhận hàng khi mặt hàng đã ngừng kinh doanh: **không bị chặn** (không kiểm tra lại trạng thái mặt hàng trong `nhanHang`).
  - Sai lệch số lượng/giá so với đơn: **không có** cơ chế kiểm tra hay phê duyệt; số nhận chỉ bị giới hạn bởi số đặt.
  - Trả hàng mua/giảm đơn sau nhận: **không có trong code**.

  ---

  ## 2. Mua pallet thường và vật tư mới

  ### (a) Các bước theo thứ tự

  Quy trình mua **dùng chung toàn bộ** đơn mua và nhận hàng của quy trình 1. Khác biệt duy nhất nằm ở loại mặt hàng và đích vào kho:

  1. **GD/PGD/NVKHO** tạo đơn mua với mặt hàng loại `pallet` (pallet gỗ/nhựa mới) hoặc `linh_kien` (vĩ gỗ, thanh/cục gỗ mới). Không được chọn `phe_lieu`; pallet cũ `cho_tai_che` vẫn có thể mua nhưng sẽ đi luồng phân loại.
  2. **GD/PGD** xác nhận đơn.
  3. **GD/PGD/NVKHO** nhận hàng; hệ thống tạo phiếu nhập `PN` hoàn thành ngay và lô hàng với `muc_dich_nhap = ban_thanh_pham` (vì mặt hàng không phải `cho_tai_che`).
  4. Hàng vào tồn kho ngay, **không có bước kiểm tra chất lượng, không có trạng thái "chờ kiểm tra"** cho pallet mới/vật tư.
  5. Có một đường nhập khác: **nhập kho thủ công** tại màn hình nhập kho (`PhieuNhapServiceImpl.taoPhieuNhap`), dùng khi không lập đơn mua:
    - Chọn kho, nhà cung cấp (tùy chọn), nhân viên tiếp nhận (tùy chọn), trạng thái `cho_xu_ly` hoặc `da_hoan_thanh`, ghi chú, biển số xe; thêm dòng mặt hàng + số lượng.
    - Nếu chọn `da_hoan_thanh`, hệ thống tạo lô và cộng tồn ngay; nếu `cho_xu_ly`, chỉ tạo lô, chưa cộng tồn cho đến khi cập nhật sang `da_hoan_thanh`.
    - Phiếu nhập thủ công **không có đơn giá**, nên `lo_hang.don_gia_nhap` để trống.
  6. **Không có hóa đơn mua/thanh toán nhà cung cấp** (xem quy trình 12).

  ### (b) Chứng từ/record tạo ra và trạng thái

  | Record | Bảng | Trạng thái | Ghi chú |
  |---|---|---|---|
  | Đơn mua | `don_mua` | như quy trình 1 | Mặt hàng `pallet`/`linh_kien` |
  | Phiếu nhập theo đơn | `phieu_kho` | `da_hoan_thanh` | Sinh từ `nhanHang` |
  | Lô hàng | `lo_hang` | `con_hang` / `da_het` | `muc_dich_nhap=ban_thanh_pham`; có `don_gia_nhap` |
  | Phiếu nhập thủ công | `phieu_kho` | `cho_xu_ly` → `da_hoan_thanh` hoặc `da_huy` | Không gắn đơn mua; lô có `don_gia_nhap = null` |
  | Tồn kho | `ton_kho` | — | Chỉ cộng khi phiếu nhập thủ công ở `da_hoan_thanh` |
  | Hóa đơn mua/thanh toán NCC | — | — | **không có trong code** |

  ### (c) Dữ liệu đầu vào

  **Nhập kho thủ công — `TaoPhieuNhapRequest`:**

  | Trường | Kiểu | Bắt buộc | Mặc định | Ghi chú |
  |---|---|---|---|---|
  | `khoId` | Long | Có (service kiểm tra) | — | Không kiểm tra quyền kho |
  | `nccId` | Long | Không | null | Chỉ lưu tham chiếu, không có công nợ |
  | `nhanVienTiepNhanId` | Long | Không | null | Lưu vào `nhan_vien_giao_nhan_id` |
  | `trangThai` | String | Không | `cho_xu_ly` | Chỉ `cho_xu_ly` hoặc `da_hoan_thanh` khi tạo |
  | `ghiChu` | String | Không | null | — |
  | `bienSoXe` | String | Không | null | — |
  | `chiTiet[]` | List | Có | — | Ít nhất 1 dòng |
  | `chiTiet[].matHangId` | Long | Có | — | — |
  | `chiTiet[].soLuong` | Integer | Có | — | > 0 |
  | `chiTiet[].ghiChu` | String | Không | null | — |

  **Sửa phiếu nhập thủ công:** cùng cấu trúc; chỉ khi phiếu `cho_xu_ly`; được đổi trạng thái sang `cho_xu_ly`, `da_hoan_thanh`, `da_huy`.

  ### (d) Công thức

  - Không có tổng tiền trên phiếu nhập thủ công.
  - `ton_kho += so_luong` cho từng dòng khi phiếu `da_hoan_thanh` — `PhieuNhapServiceImpl.ghiChiTietVaCapNhatTon`, dòng 229–259.
  - Lô: `so_luong_nhap = so_luong_con_lai = so_luong`; `don_gia_nhap` không được gán — cùng hàm, dòng 234–245.
  - Khi sửa phiếu nháp: xóa toàn bộ dòng và lô cũ, tạo lại dòng/lô mới (`capNhatPhieuNhap`, dòng 210–220). Khi chuyển `da_hoan_thanh`, cộng tồn theo dòng mới.
  - Khi hủy phiếu nháp: đặt các lô về `so_luong_con_lai = 0`, `da_het`, không đụng tồn (vì chưa cộng) — dòng 192–203.

  ### (e) Ràng buộc

  - Phiếu nhập thủ công phải có ít nhất 1 mặt hàng; mỗi dòng có mặt hàng và số lượng > 0 (`kiemTraChiTiet`, dòng 124–133).
  - Chỉ sửa phiếu `cho_xu_ly`; không sửa/hủy phiếu đã `da_hoan_thanh` (`capNhatPhieuNhap`, dòng 181–183).
  - Khi tạo, chỉ nhận trạng thái `cho_xu_ly`/`da_hoan_thanh`; khi sửa, thêm `da_huy` (dòng 141–143, 185–188).
  - Không kiểm tra nhà cung cấp còn hợp tác, không kiểm tra mặt hàng đang kinh doanh, **không kiểm tra quyền kho** ở service nhập thủ công; controller chỉ chặn theo role GD/PGD/NVKHO.
  - DB: `chi_tiet_phieu_kho.so_luong > 0`; `lo_hang.so_luong_con_lai >= 0` và `<= so_luong_nhap`.

  ### (f) Ngoại lệ

  - Nhập thủ công `cho_xu_ly` rồi mới chuyển `da_hoan_thanh`: tồn chỉ tăng ở lần chuyển; nếu sửa danh sách trước khi chuyển, lô cũ bị xóa và tạo lại.
  - Hủy phiếu nháp: lô bị vô hiệu hóa, không có phiếu xuất bù vì chưa cộng tồn.
  - Phiếu đã hoàn thành: không có thao tác sửa/hủy/đảo; **không có** nghiệp vụ trả hàng nhập.
  - Nhập trùng mặt hàng trong cùng phiếu: **không bị chặn**; hệ thống tạo 2 dòng, 2 lô và cộng tồn 2 lần (khác với đơn mua có chặn trùng).
  - Nhập vượt nhu cầu/không theo đơn: được phép, không có kiểm soát.

  ---

  ## 3. Nhận hàng, kiểm tra, phân loại pallet

  ### (a) Các bước theo thứ tự

  1. **Nhận hàng**: thực hiện ở quy trình 1 (nút "Nhận hàng" trên đơn mua). Pallet cũ được nhận vào mặt hàng `PL-CPL-05`, tồn kho tăng ngay, lô có `muc_dich_nhap=cho_tai_che`. **Không có phiếu kiểm tra riêng, không có trạng thái "chờ kiểm tra", không có checklist/tiêu chí chất lượng.**
  2. **GD/PGD/NVKHO** mở tab tái chế/phân loại, bấm tạo phiếu. Danh mục lô chỉ trả các lô `cho_tai_che`, còn hàng, và đúng mặt hàng `PL-CPL-05` (`PhieuTaiCheServiceImpl.layDanhMuc`, dòng 82–115).
  3. Chọn lô; nhập **số pallet xử lý** và chia vào 3 nhóm: bán lại nguyên trạng, tháo dỡ, loại bỏ; nếu nhóm loại bỏ > 0 thì nhập số kg phế liệu thu được. Ghi chú tùy chọn.
  4. Lưu → phiếu `PTC-...` trạng thái `nhap`.
  5. **GD/PGD** bấm duyệt → `POST /api/v1/tai-che/{id}/duyet`. Hệ thống:
    - kiểm tra lại tổng 3 nhóm = số xử lý và tồn lô;
    - trừ tồn `PL-CPL-05` đúng số xử lý;
    - cộng `PL-CTD-06` (tháo dỡ), `PL-DC-07` (bán lại), `PE-GO-15` kg (loại bỏ) theo số nhập;
    - tạo một phiếu xuất và một phiếu nhập hệ thống `da_hoan_thanh`;
    - giảm `lo_hang.so_luong_con_lai` đúng số xử lý; lô hết thì `da_het`;
    - phiếu chuyển `da_phan_loai` nếu có pallet tháo dỡ, ngược lại `da_hoan_tat`.
  6. Có thể tạo nhiều phiếu phân loại cho cùng một lô cho đến khi hết lô; số đang nằm trong phiếu nháp khác bị trừ khỏi số khả dụng khi lập phiếu mới.

  ### (b) Chứng từ/record tạo ra và trạng thái

  | Record | Bảng | Trạng thái | Ghi chú |
  |---|---|---|---|
  | Phiếu phân loại | `phieu_tai_che` | `nhap` → `da_phan_loai` hoặc `da_hoan_tat`; `da_huy` | Mã `PTC-` |
  | Dòng kết quả | `chi_tiet_tai_che` | — | Lưu mặt hàng đích + số lượng/kg |
  | Phiếu xuất hệ thống | `phieu_kho` | `da_hoan_thanh`, `la_he_thong=true` | Xuất `PL-CPL-05` khỏi kho |
  | Phiếu nhập hệ thống | `phieu_kho` | `da_hoan_thanh`, `la_he_thong=true` | Nhập `PL-CTD-06`, `PL-DC-07`, `PE-GO-15` |
  | Lô | `lo_hang` | `con_hang` → `da_het` | Giảm số còn lại, không đổi đơn giá |
  | Tồn kho | `ton_kho` | — | Trừ nguồn, cộng 3 đích |
  | Kiểm tra chất lượng | — | — | **không có trong code** |

  ### (c) Dữ liệu đầu vào

  **`TaoPhieuTaiCheRequest`:**

  | Trường | Kiểu | Bắt buộc | Mặc định | Ghi chú |
  |---|---|---|---|---|
  | `loHangId` | Long | Có | — | Lô `PL-CPL-05`, `cho_tai_che`, còn hàng |
  | `soLuongXuLy` | Integer | Có | — | > 0; <= số còn lại − số đang trong phiếu nháp khác |
  | `soBanLai` | Integer | Có | — | >= 0 |
  | `soThaoDo` | Integer | Có | — | >= 0 |
  | `soLoaiBo` | Integer | Có | — | >= 0 |
  | `kgPheLieuLoaiBo` | Integer | Không | 0 | >= 0; bắt buộc > 0 nếu `soLoaiBo > 0` |
  | `ghiChu` | String (255) | Không | null | — |

  ### (d) Công thức

  - Cân bằng nhóm: `soBanLai + soThaoDo + soLoaiBo = soLuongXuLy` — `QuyTacTaiChe.kiemTraPhanLoai`, dòng 21–39.
  - Tồn sau duyệt:
    - `PL-CPL-05 -= soLuongXuLy`
    - `PL-CTD-06 += soThaoDo`
    - `PL-DC-07 += soBanLai`
    - `PE-GO-15 += kgPheLieuLoaiBo`
    — `PhieuTaiCheServiceImpl.duyetPhanLoai`, dòng 238–242.
  - Lô: `so_luong_con_lai -= soLuongXuLy` — dòng 280.
  - Trạng thái sau duyệt: `soThaoDo > 0 ? da_phan_loai : da_hoan_tat` — `QuyTacTaiChe.trangThaiSauDuyet`, dòng 42–44.
  - **Không có tỷ lệ/định mức phân loại**: ba nhóm là số nguyên nhập tay, chỉ bị ràng buộc bởi tổng bằng số xử lý.

  ### (e) Ràng buộc

  - Chỉ phân loại lô `muc_dich_nhap=cho_tai_che`, `trang_thai=con_hang`, `so_luong_con_lai > 0`, và mặt hàng đúng `PL-CPL-05` (`kiemTraLo`, dòng 295–305).
  - Tổng 3 nhóm phải bằng số xử lý; các số không âm; `soLuongXuLy > 0`; nếu loại bỏ > 0 thì kg > 0; nếu kg > 0 thì phải có loại bỏ (`QuyTacTaiChe.kiemTraPhanLoai`).
  - Số xử lý không vượt `so_luong_con_lai − tổng số đang nằm trong phiếu nháp khác` (`kiemTraConDuHang`).
  - Chỉ sửa/hủy phiếu `nhap`; người sửa phải là người lập hoặc GD/PGD, và đúng kho (`kiemTraNguoiSua`).
  - Duyệt chỉ khi phiếu `nhap`; duyệt lại kiểm tra tồn lô; role GD/PGD ở controller. **Hàm duyệt không kiểm tra lại quyền kho của người duyệt** (`duyetPhanLoai`, dòng 208–291).
  - DB: `phieu_tai_che.trang_thai` chỉ `nhap/da_hoan_tat/da_huy`; các cột số lượng >= 0; `so_luong_da_xu_ly > 0` (`V1` dòng 263–283).

  ### (f) Ngoại lệ

  - Tổng nhóm lệch: chặn lưu và chặn duyệt lại.
  - Nhóm loại bỏ có pallet nhưng kg = 0: chặn; kg > 0 nhưng không có pallet loại bỏ: chặn.
  - Lô không đủ do phiếu nháp khác giữ: chặn tạo/sửa.
  - Lô bị thay đổi tồn giữa lúc lập và duyệt (phiếu khác duyệt trước): duyệt báo "lô chỉ còn ...", không ghi gì.
  - Hủy phiếu nháp: không đụng tồn; phiếu đã duyệt không hủy/đảo được.
  - Pallet hỏng hoàn toàn: phải đưa vào nhóm loại bỏ kèm kg; không có nhóm "hỏng chờ xử lý" riêng.
  - **Không có** tiêu chí "đạt chuẩn" (kích thước, số thanh, mức hư hỏng); "đạt chuẩn" chỉ là nhóm nhập tay.

  ---

  ## 4. Chuyển kho và kiểm kê

  ### (a) Các bước theo thứ tự

  **Chuyển kho (thực chất là "xếp vị trí" sau nhập):**

  1. **GD/PGD/NVKHO** mở danh sách phiếu nhập đã `da_hoan_thanh` và chưa xếp vị trí.
  2. Bấm xếp vị trí; với **từng mặt hàng** trong phiếu, chọn kho đích và vị trí đích. Phải chọn đủ tất cả mặt hàng; vị trí phải thuộc kho đích.
  3. Hệ thống:
    - nếu kho đích **khác** kho của phiếu nhập: trừ toàn bộ số lượng đã nhập của mặt hàng ở kho nguồn, cộng toàn bộ sang kho đích, và chuyển lô hàng sang kho đích;
    - nếu cùng kho: chỉ gán vị trí lưu trữ cho dòng tồn;
    - đánh dấu phiếu `da_xep_vi_tri = true`, chặn xếp lại.
  4. **Không có phiếu chuyển kho riêng, không có chuyển một phần, không có chọn lô khi chuyển.** Bảng `phieu_kho` cho phép `loai_phieu=chuyen_kho` nhưng **không có service/controller nào tạo loại này**.

  **Kiểm kê:**

  1. **GD/PGD/NVKHO** tạo phiên kiểm kê cho một kho: chọn kho, người tạo, ghi chú.
  2. Hệ thống chụp snapshot toàn bộ dòng `ton_kho` của kho, **bỏ qua mặt hàng loại `cho_tai_che`**, tạo các dòng `cho_kiem` với `ton_he_thong`.
  3. Nhân viên nhập `ton_thuc_te` và ghi chú giải trình cho từng dòng; có thể gán người kiểm. Dòng có thực tế = hệ thống → `khop`; khác → `lech`; chưa nhập → `cho_kiem`.
  4. Bấm hoàn tất: bắt buộc không còn dòng `cho_kiem`; nếu có lệch, hệ thống tạo **một phiếu điều chỉnh `dieu_chinh_kiem_ke`** `da_hoan_thanh`, `la_he_thong=true`, `nguoi_duyet` = người chốt, và đặt tồn kho bằng đúng `ton_thuc_te`; dòng chênh lệch được ghi vào phiếu điều chỉnh với số lượng tuyệt đối và ghi chú tăng/giảm.
  5. Phiên chuyển `da_hoan_tat`.

  ### (b) Chứng từ/record tạo ra và trạng thái

  | Record | Bảng | Trạng thái | Ghi chú |
  |---|---|---|---|
  | Phiếu nhập đã xếp vị trí | `phieu_kho` | vẫn `da_hoan_thanh` | Cờ `da_xep_vi_tri=true`; không sinh phiếu chuyển |
  | Lô sau chuyển kho | `lo_hang` | không đổi trạng thái | `kho_id` đổi sang kho đích nếu khác kho |
  | Phiên kiểm kê | `phien_kiem_ke` | `dang_dien_ra` → `da_hoan_tat` | Mã `PKK-`; mỗi kho chỉ 1 phiên mở |
  | Dòng kiểm kê | `chi_tiet_kiem_ke` | `cho_kiem`, `khop`, `lech` | Lưu `ton_he_thong`, `ton_thuc_te`, người kiểm, ghi chú |
  | Phiếu điều chỉnh | `phieu_kho` | `da_hoan_thanh`, `la_he_thong=true` | Mã `PDC-`; chỉ tạo khi có ít nhất 1 dòng lệch |
  | Tồn kho | `ton_kho` | — | Gán bằng `ton_thuc_te`; có thể tạo dòng mới nếu chưa có và thực tế > 0 |

  ### (c) Dữ liệu đầu vào

  **Xếp vị trí — `XepViTriRequest`:**

  | Trường | Kiểu | Bắt buộc | Ghi chú |
  |---|---|---|---|
  | `items[].matHangId` | Long | Có | Phải có trong phiếu nhập |
  | `items[].khoId` | Long | Có | Kho đích |
  | `items[].viTriId` | Long | Có | Phải thuộc kho đích |

  **Tạo phiên kiểm kê — `TaoPhienKiemKeRequest`:**

  | Trường | Kiểu | Bắt buộc | Ghi chú |
  |---|---|---|---|
  | `khoId` | Long | Có (service kiểm tra) | — |
  | `nguoiTaoId` | Long | Có (service kiểm tra) | Không đối chiếu với người đang đăng nhập |
  | `ghiChu` | String | Không | — |

  **Cập nhật dòng kiểm kê — `CapNhatChiTietKiemKeRequest`:**

  | Trường | Kiểu | Bắt buộc | Ghi chú |
  |---|---|---|---|
  | `tonThucTe` | Integer | Không | null = chưa kiểm; < 0 bị chặn |
  | `ghiChuGiaiTrinh` | String | Không | — |
  | `nguoiKiemId` | Long | Không | Nếu không tìm thấy thì để null |

  **Hoàn tất:** `nguoiChotId` là query param, bắt buộc; không kiểm tra người này có phải người đang đăng nhập hay không.

  ### (d) Công thức

  - Chuyển kho: `ton_kho(nguồn) -= tổng số lượng đã nhập của mặt hàng`; `ton_kho(đích) += đúng số đó`; toàn bộ, không chọn phần (`PhieuNhapServiceImpl.xepViTri`, dòng 302–343).
  - Kiểm kê:
    - `chenh_lech = ton_thuc_te − ton_he_thong` (`KiemKeServiceImpl.layDanhSachChiTiet`, dòng 96–99).
    - Tỷ lệ tiến độ = `số dòng đã kiểm / tổng số dòng × 100`, làm tròn 1 chữ số (`layThongKe`, dòng 135–140).
    - Khi chốt: `ton_kho = ton_thuc_te`; dòng điều chỉnh ghi `|chênh lệch|` kèm "Kiểm kê tăng/giảm" (`hoanTatKiemKe`, dòng 213–257).
  - Không có công thức giá trị tồn, không có giá vốn.

  ### (e) Ràng buộc

  - Xếp vị trí chỉ cho phiếu nhập `da_hoan_thanh`, chưa `da_xep_vi_tri`, phải chọn đủ tất cả mặt hàng, vị trí thuộc kho đích (`xepViTri`, dòng 270–321).
  - Mỗi kho chỉ có một phiên kiểm kê `dang_dien_ra` (`taoPhienKiemKe`, dòng 52–55).
  - Không sửa dòng kiểm kê sau khi phiên `da_hoan_tat`; `ton_thuc_te >= 0` (`capNhatChiTiet`, dòng 150–155).
  - Không chốt khi còn dòng `cho_kiem` (`hoanTatKiemKe`, dòng 190–193).
  - Khi chốt, nếu tồn thực tế < số đang giữ chỗ thì chặn (`hoanTatKiemKe`, dòng 235–238).
  - Snapshot **bỏ qua `cho_tai_che`**, nên pallet cũ chờ phân loại/chờ tháo dỡ không được kiểm kê.
  - Không kiểm tra quyền kho ở service kiểm kê; controller chỉ chặn role. `nguoiTaoId`/`nguoiChotId` do client gửi, không đối chiếu danh tính.
  - DB: `phien_kiem_ke.trang_thai` chỉ `dang_dien_ra/da_hoan_tat`; `chi_tiet_kiem_ke` ràng buộc `khop/lech` phải có `ton_thuc_te`, `cho_kiem` thì `ton_thuc_te` null (`V1` dòng 410–436).

  ### (f) Ngoại lệ

  - Phiếu nhập đã xếp vị trí: chặn xếp lần 2.
  - Kho đích chưa có dòng tồn: `xepViTri` báo lỗi "Chưa có tồn kho ... tại kho đích" (dòng 338–340). Trong khi đó kiểm kê có thể tạo dòng tồn mới nếu thực tế > 0 (dòng 221–233).
  - Chuyển kho khác kho làm lô đổi kho, nhưng **không tạo chứng từ xuất/nhập**; chỉ phiếu nhập gốc được đánh dấu.
  - Kiểm kê thiếu dòng: không chốt được.
  - Kiểm kê thực tế thấp hơn giữ chỗ: chặn chốt, phiên vẫn mở.
  - **Không có** kiểm kê theo lô, theo vị trí riêng, đếm lại, hay quy trình duyệt điều chỉnh độc lập.

  ---
  ## 5. Tháo dỡ pallet cũ nội bộ

  ### (a) Các bước theo thứ tự

  1. **Điều kiện vào luồng:** phiếu phân loại (quy trình 3) phải có `soThaoDo > 0` và đang ở trạng thái `da_phan_loai`; khi đó pallet `PL-CTD-06` đã được cộng vào tồn kho công ty. Màn hình tháo dỡ chỉ liệt kê các phiếu tái chế `da_phan_loai`, lọc theo kho người dùng được phép thao tác, và còn số pallet chưa tháo: `soKhaDung = soThaoDo − đã duyệt − đang nằm trong phiếu nháp` (`PhieuThaoDoServiceImpl.layDanhMuc`, dòng 79–96).
  2. **GD/PGD/NVKHO** mở tab **Tháo dỡ**, bấm tạo phiếu. Chọn phiếu tái chế nguồn, nhập **số pallet tháo dỡ của đợt** và **nhập tay** 5 kết quả: số thanh nguyên, số cục vuông, số thanh cần xử lý thêm, số cục cần xử lý thêm, số kg phế liệu; ghi chú tùy chọn. Không chọn lô đích, không chọn kho đích (dùng đúng kho của phiếu tái chế).
  3. Bấm lưu → `POST /api/v1/thao-do`, sinh phiếu `TD-...` (mã lấy qua `getMaChungTu("PTC","TD-")`), trạng thái `nhap`, `ngayThucHien = hôm nay`, `nhanVienThucHien = người đăng nhập` (`taoMoi`, dòng 100–125).
  4. Có thể sửa hoặc hủy khi còn `nhap`. Khi sửa, không được đổi phiếu tái chế nguồn; nếu muốn đổi phải hủy và tạo phiếu mới (`capNhat`, dòng 127–157). Hủy bắt buộc nhập lý do (`huy`, dòng 159–179).
  5. **GD/PGD** bấm duyệt → `POST /api/v1/thao-do/{id}/duyet`. Hệ thống kiểm tra lại số pallet còn được tháo, kiểm tra tồn `PL-CTD-06`, rồi:
    - trừ tồn `PL-CTD-06` đúng `soPalletThaoDo`;
    - tạo **một phiếu xuất kho hệ thống** `PX-...` trạng thái `da_hoan_thanh`, `la_he_thong=true`, dòng hàng `PL-CTD-06` tham chiếu lô của phiếu tái chế;
    - tạo **một phiếu nhập kho hệ thống** `PN-...` trạng thái `da_hoan_thanh`, `la_he_thong=true`, mỗi kết quả > 0 là một dòng: `LK-TN-09` (thanh nguyên), `LK-CV-10` (cục vuông), `LK-TX-11` (thanh cần xử lý), `LK-CX-12` (cục cần xử lý), `PE-GO-15` kg (phế liệu);
    - cộng tồn kho cho từng kết quả > 0;
    - phiếu tháo dỡ chuyển `da_duyet`, lưu người duyệt/thời gian duyệt;
    - phiếu tái chế cha chuyển `da_hoan_tat` khi **tổng số pallet đã duyệt (gồm đợt này) >= `soThaoDo`**, ngược lại vẫn `da_phan_loai` (`duyet`, dòng 183–246; `QuyTacTaiChe.trangThaiSauThaoDo`, dòng 61–63).
  6. Có thể tạo nhiều đợt tháo dỡ cho cùng một phiếu tái chế cho đến khi đủ số cần tháo.
  7. **Không có bước tính giá vốn, phân bổ chi phí, tạo lô mới, xuất bán linh kiện, hay chuyển linh kiện sang phân loại lại trong code.**

  ### (b) Chứng từ/record tạo ra và trạng thái

  | Record | Bảng | Trạng thái | Ghi chú |
  |---|---|---|---|
  | Phiếu tháo dỡ | `phieu_thao_do` | `nhap` → `da_duyet`; nhánh `da_huy` | Mã `TD-`; mỗi đợt là một phiếu |
  | Phiếu xuất hệ thống | `phieu_kho` (`xuat_kho`) | tạo thẳng `da_hoan_thanh` | Xuất `PL-CTD-06`; có `phieu_tai_che_id` |
  | Phiếu nhập hệ thống | `phieu_kho` (`nhap_kho`) | tạo thẳng `da_hoan_thanh` | Nhập 4 loại linh kiện + phế liệu |
  | Dòng phiếu kho | `chi_tiet_phieu_kho` | — | Dòng xuất gán `lo_id` của lô nguồn; dòng nhập cũng gán cùng `lo_id` |
  | Tồn kho | `ton_kho` | — | `PL-CTD-06` giảm; `LK-*`, `PE-GO-15` tăng |
  | Lô hàng | `lo_hang` | không đổi | **Không tạo lô mới, không trừ `so_luong_con_lai`** ở bước tháo dỡ; lô chỉ được tham chiếu |
  | Phiếu tái chế cha | `phieu_tai_che` | `da_phan_loai` → `da_hoan_tat` khi đủ | Không có trạng thái riêng cho "đang tháo dỡ" |

  ### (c) Dữ liệu đầu vào

  **`TaoPhieuThaoDoRequest`:**

  | Trường | Kiểu | Bắt buộc | Mặc định | Ghi chú |
  |---|---|---|---|---|
  | `phieuTaiCheId` | Long | Có (`@NotNull`) | — | Phải là phiếu `da_phan_loai` |
  | `soPalletThaoDo` | Integer | Có (`@NotNull`, `@Positive`) | — | > 0; không vượt số còn được tháo |
  | `soThanhNguyen` | Integer | Không | 0 khi null | `>= 0` |
  | `soCucVuong` | Integer | Không | 0 khi null | `>= 0` |
  | `soThanhCanXuLy` | Integer | Không | 0 khi null | `>= 0` |
  | `soCucCanXuLy` | Integer | Không | 0 khi null | `>= 0` |
  | `kgPheLieu` | Integer | Không | 0 khi null | `>= 0`, đơn vị kg |
  | `ghiChu` | String (255) | Không | null | Cắt khoảng trắng |

  ### (d) Công thức số lượng/giá/tồn

  - Số pallet còn được tháo khi tạo phiếu: `soThaoDo − Σ(số pallet các phiếu da_duyet) − Σ(số pallet các phiếu nhap khác)` (`kiemTraConDuSo`, dòng 265–275). Khi sửa, phiếu đang sửa được loại khỏi tổng phiếu nháp (`boQuaId = id`).
  - Khi duyệt: `daDuyet + soPalletThaoDo > soThaoDo` thì chặn (`duyet`, dòng 198–202).
  - Tồn kho sau duyệt:
    - `PL-CTD-06 -= soPalletThaoDo`;
    - `LK-TN-09 += soThanhNguyen`;
    - `LK-CV-10 += soCucVuong`;
    - `LK-TX-11 += soThanhCanXuLy`;
    - `LK-CX-12 += soCucCanXuLy`;
    - `PE-GO-15 += kgPheLieu`.
    — `duyet` dòng 208–234.
  - **Không có công thức quy đổi pallet → số thanh/cục/kg.** Cả 5 kết quả là số nguyên nhập tay, không có tỷ lệ, không có giới hạn trên, không có đối chiếu với số pallet. Ràng buộc duy nhất: tổng 5 kết quả > 0 (`QuyTacTaiChe.kiemTraThaoDo`, dòng 45–58).
  - Không có đơn giá, thành tiền, chi phí tháo dỡ trên phiếu; không có bút toán giá vốn.

  ### (e) Ràng buộc và kiểm tra

  - Phiếu tái chế phải `da_phan_loai` (`kiemTraPhieuTaiChe`, dòng 258–263).
  - Số pallet tháo dỡ > 0; các kết quả `>= 0`; tổng kết quả > 0 (`QuyTacTaiChe.kiemTraThaoDo`; DB `ck_thao_do_co_ket_qua`, `V5` dòng 24–26).
  - Không vượt số pallet còn lại sau khi trừ các đợt đã duyệt và các phiếu nháp khác.
  - Chỉ sửa/hủy phiếu `nhap`; người sửa phải là người lập hoặc GD/PGD; phải đúng kho của phiếu (`kiemTraNguoiSua`, dòng 292–298).
  - Duyệt chỉ khi phiếu `nhap`; controller chặn role `GD/PGD` (`PhieuThaoDoController` dòng 51–52). **Hàm `duyet` không gọi lại `PhanQuyenKho.kiemTra`**, nên người duyệt không bị kiểm tra kho.
  - DB: `so_pallet_thao_do > 0`; các cột kết quả `>= 0`; trạng thái chỉ `nhap/da_duyet/da_huy`; phiếu `da_duyet` bắt buộc có người duyệt và thời gian duyệt (`V5` dòng 10–28).

  ### (f) Ngoại lệ đã xử lý

  - Tổng kết quả = 0: chặn cả khi lưu và khi duyệt.
  - Số pallet vượt số còn lại: chặn khi tạo/sửa; nếu các phiếu khác duyệt trước làm thiếu, bước duyệt báo lại số còn lại.
  - Tồn `PL-CTD-06` không đủ khi duyệt: `tru` báo lỗi, toàn bộ giao dịch rollback.
  - Hủy phiếu nháp: không đụng tồn.
  - Phiếu đã duyệt: không sửa/hủy/đảo; **không có trong code** nghiệp vụ điều chỉnh kết quả tháo dỡ sau duyệt.
  - Pallet hỏng hoàn toàn: phải được đưa vào nhóm loại bỏ ngay từ bước phân loại (quy trình 3), vì bước tháo dỡ bắt buộc có ít nhất một kết quả.
  - Tháo dỡ vượt số pallet thực tế trong kho (do dữ liệu lệch): chỉ bị chặn bởi tồn `PL-CTD-06`, không đối chiếu lại với lô.

  ---

  ## 6. Phân loại linh kiện thu hồi

  ### (a) Các bước theo thứ tự

  1. Sau khi tháo dỡ, các mặt hàng `LK-TX-11` (thanh cần xử lý thêm) và `LK-CX-12` (cục cần xử lý thêm) nằm trong tồn kho công ty. **GD/PGD/NVKHO** mở tab **Xử lý thêm**; màn hình hiển thị tồn khả dụng theo từng kho và từng loại nguồn: `ton − da_giu_cho − số đang nằm trong phiếu nháp khác` (`PhieuXuLyThemServiceImpl.layTonCanXuLy`, dòng 78–102).
  2. Bấm tạo phiếu, chọn kho, chọn **loại nguồn** `thanh` hoặc `cuc`, nhập **số chiếc dùng lại được** (`soDatLai`), **số chiếc bỏ** (`soPheLieu`), **số kg phế liệu** (`kgPheLieu`) và **lý do xử lý** (bắt buộc). Lưu → phiếu `XL-...` trạng thái `nhap` (`taoMoi`, dòng 106–130).
  3. Có thể sửa/hủy khi còn `nhap`; không được đổi kho; người sửa phải là người lập hoặc GD/PGD (`capNhat` dòng 132–160; `huy` dòng 162–182).
  4. **GD/PGD** bấm duyệt → `POST /api/v1/xu-ly-them/{id}/duyet`. Hệ thống kiểm tra lại quy tắc và tồn nguồn, rồi:
    - trừ tồn nguồn đúng `soDatLai + soPheLieu` (`LK-TX-11` nếu nguồn `thanh`, `LK-CX-12` nếu nguồn `cuc`);
    - cộng `LK-TN-09` (nguồn `thanh`) hoặc `LK-CV-10` (nguồn `cuc`) đúng `soDatLai`;
    - cộng `PE-GO-15` đúng `kgPheLieu`;
    - tạo một phiếu xuất và một phiếu nhập hệ thống `da_hoan_thanh`, `la_he_thong=true`;
    - phiếu chuyển `da_duyet`, lưu người duyệt/thời gian (`duyet`, dòng 186–245).
  5. **Không có bước phân loại theo nhiều mức chất lượng, không có chọn lô đích, không có đơn giá/chi phí xử lý.**

  ### (b) Chứng từ/record tạo ra và trạng thái

  | Record | Bảng | Trạng thái | Ghi chú |
  |---|---|---|---|
  | Phiếu xử lý thêm | `phieu_xu_ly_them` | `nhap` → `da_duyet`; nhánh `da_huy` | Mã `XL-` |
  | Phiếu xuất hệ thống | `phieu_kho` (`xuat_kho`) | `da_hoan_thanh`, `la_he_thong=true` | Xuất hàng nguồn |
  | Phiếu nhập hệ thống | `phieu_kho` (`nhap_kho`) | `da_hoan_thanh`, `la_he_thong=true` | Nhập hàng dùng lại + phế liệu |
  | Dòng phiếu kho | `chi_tiet_phieu_kho` | — | Không gán `lo_id` |
  | Tồn kho | `ton_kho` | — | Trừ nguồn, cộng đích/phế liệu |
  | Lô hàng | `lo_hang` | không đổi | **Không có trong code** việc tạo/trừ lô ở bước này |

  ### (c) Dữ liệu đầu vào

  **`TaoPhieuXuLyThemRequest`:**

  | Trường | Kiểu | Bắt buộc | Mặc định | Ghi chú |
  |---|---|---|---|---|
  | `khoId` | Long | Có (`@NotNull`) | — | Phải đúng kho của phiếu khi sửa |
  | `loaiNguon` | String | Có (`@NotBlank`) | — | Chỉ `thanh` hoặc `cuc` |
  | `soDatLai` | Integer | Không | 0 khi null | `>= 0`, số chiếc dùng lại |
  | `soPheLieu` | Integer | Không | 0 khi null | `>= 0`, số chiếc bỏ |
  | `kgPheLieu` | Integer | Không | 0 khi null | `>= 0`, kg phế liệu |
  | `lyDo` | String (255) | Có (`@NotBlank`) | — | Bắt buộc cả khi tạo, sửa, duyệt |

  ### (d) Công thức số lượng/giá/tồn

  - Lượng hàng nguồn bị xuất: `tongXuat = soDatLai + soPheLieu` (`duyet`, dòng 204).
  - Tồn sau duyệt:
    - `LK-TX-11` hoặc `LK-CX-12 -= (soDatLai + soPheLieu)`;
    - `LK-TN-09` hoặc `LK-CV-10 += soDatLai`;
    - `PE-GO-15 += kgPheLieu`.
    — `duyet` dòng 200–236.
  - Tồn khả dụng khi lập/sửa phiếu: `ton_kho − so_luong_da_giu_cho − Σ(soDatLai + soPheLieu của các phiếu nhap khác)`; nếu `canDung > khaDung` thì chặn (`kiemTraDuTon`, dòng 272–286).
  - **Không có tỷ lệ thu hồi, không có quy đổi 1 thanh/cục ra bao nhiêu kg, không có phân bổ chi phí.** `soDatLai`, `soPheLieu`, `kgPheLieu` là số nguyên nhập tay.
  - Không có đơn giá/giá vốn trên phiếu.

  ### (e) Ràng buộc và kiểm tra

  - `loaiNguon` chỉ `thanh`/`cuc`; lý do bắt buộc; các số `>= 0`; `soDatLai + soPheLieu > 0`; `(soPheLieu > 0) = (kgPheLieu > 0)` (`QuyTacTaiChe.kiemTraXuLyThem`, dòng 78–96; DB `ck_xu_ly_them_co_so_luong`, `ck_xu_ly_them_kg`, `V6` dòng 21–23).
  - Không vượt tồn khả dụng đã trừ phiếu nháp khác khi tạo/sửa.
  - Chỉ sửa/hủy phiếu `nhap`; người sửa là người lập hoặc GD/PGD; đúng kho (`kiemTraNguoiSua`, dòng 249–255).
  - Duyệt chỉ khi `nhap`; role `GD/PGD` ở controller (`PhieuXuLyThemController` dòng 52–53). **Hàm duyệt không kiểm tra lại quyền kho.**
  - DB: trạng thái chỉ `nhap/da_duyet/da_huy`; phiếu `da_duyet` bắt buộc có người duyệt/thời gian (`V6` dòng 15–25).

  ### (f) Ngoại lệ đã xử lý

  - `soPheLieu > 0` nhưng `kgPheLieu = 0`: chặn; ngược lại `kg > 0` mà không có chiếc bỏ: chặn.
  - Không có chiếc nào được xử lý (`soDatLai + soPheLieu = 0`): chặn.
  - Tồn khả dụng không đủ do phiếu nháp khác: chặn khi lưu.
  - Hai phiếu nháp cùng giữ một lượng hàng: khi duyệt lần lượt, phiếu sau có thể thất bại nếu tồn thực không đủ; hệ thống không giữ chỗ thật ở `so_luong_da_giu_cho`.
  - Hủy phiếu nháp: không đụng tồn.
  - Phiếu đã duyệt: không sửa/hủy/đảo; **không có trong code** điều chỉnh sau duyệt.
  - Hàng nguồn đã ngừng kinh doanh: **không bị chặn** ở bước xử lý thêm (service chỉ lấy mặt hàng theo mã).

  ---

  ## 7. Lắp ráp pallet tái chế

  ### (a) Các bước theo thứ tự

  **Bước chuẩn bị — định mức (BOM):**

  1. **GD/PGD** (NVKHO chỉ xem) mở màn hình **Định mức**, tạo định mức: tên, thành phẩm (bắt buộc loại `pallet`), ghi chú, danh sách vật tư + số lượng cho **một** pallet. Vật tư phải loại `linh_kien`, không được là `LK-TX-11`/`LK-CX-12` (phải qua xử lý thêm trước), không được trùng thành phẩm, không trùng dòng, số lượng > 0 (`DinhMucServiceImpl.taoMoi` dòng 52–70; `kiemTraVatTu` dòng 135–147; `QuyTacDongPallet.kiemTraDinhMuc` dòng 45–58).
  2. Định mức có trạng thái `hoat_dong`/`ngung`; GD/PGD được sửa, ngừng, kích hoạt. Sửa định mức xóa và tạo lại toàn bộ dòng; phiếu đóng pallet cũ đã lưu snapshot số định mức riêng nên không bị đổi lịch sử (`capNhat` dòng 72–94; `doiTrangThai` dòng 96–106).

  **Bước lắp ráp:**

  3. **GD/PGD/NVKHO** mở tab **Đóng pallet**, bấm tạo phiếu. Chọn định mức đang `hoat_dong`, kho, **số pallet sản xuất**. Hệ thống tính nhu cầu từng vật tư = `định mức 1 pallet × số pallet sản xuất` và điền sẵn số thực tế bằng đúng nhu cầu (`PhieuDongPalletServiceImpl.duKien` dòng 110–148; `dungDong` dòng 317–366).
  4. Người dùng có thể **ghi đè số thực tế** từng vật tư; nếu thực tế khác định mức thì **bắt buộc nhập lý do chênh lệch**. Có thể **thêm vật tư ngoài định mức**: khi đó `soLuongDinhMuc = 0`, thực tế > 0 cũng phải có lý do. Không được gửi trùng một vật tư hai dòng (`dungDong`, dòng 338–355; `QuyTacDongPallet.kiemTraDong`, dòng 27–36).
  5. Khi lưu, hệ thống kiểm tra từng vật tư không vượt tồn khả dụng = `ton − da_giu_cho − nhu cầu đang nằm trong các phiếu nháp khác`; nếu thiếu thì chặn (`kiemTraKhaDung`, dòng 373–389). Phiếu `DP-...` được lưu trạng thái `nhap`, kèm snapshot các dòng định mức/thực tế (`taoMoi`, dòng 169–190).
  6. Có thể sửa/hủy khi `nhap`; không đổi kho; người sửa là người lập hoặc GD/PGD (`capNhat` dòng 192–228; `huy` dòng 230–250). Nếu **định mức và số pallet sản xuất không đổi**, hệ thống giữ nguyên số định mức đã chốt trong phiếu nháp, không lấy lại BOM hiện tại; nếu đổi định mức hoặc số sản xuất thì dựng lại dòng theo BOM mới (`capNhat` dòng 204–224; `duKien` dòng 121–144).
  7. **GD/PGD** bấm duyệt → `POST /api/v1/dong-pallet/{id}/duyet`. Hệ thống:
    - kiểm tra tổng số lượng thực tế của tất cả vật tư > 0;
    - tạo phiếu xuất kho hệ thống `PX-...` `da_hoan_thanh`, `la_he_thong=true`;
    - trừ tồn từng vật tư theo **số thực tế** (bỏ qua dòng thực tế <= 0);
    - tạo phiếu nhập kho hệ thống `PN-...` `da_hoan_thanh`, `la_he_thong=true`;
    - cộng tồn thành phẩm đúng `soLuongSanXuat` (không phải tổng vật tư);
    - phiếu chuyển `da_duyet`, lưu người duyệt/thời gian (`duyet`, dòng 254–308).
  8. **Không có bước kiểm tra chất lượng thành phẩm, không có lô thành phẩm, không có tính giá thành, không có hao hụt/thu hồi phụ phẩm.**

  ### (b) Chứng từ/record tạo ra và trạng thái

  | Record | Bảng | Trạng thái | Ghi chú |
  |---|---|---|---|
  | Định mức | `dinh_muc` | `hoat_dong` / `ngung` | Mã `DM-` |
  | Dòng định mức | `chi_tiet_dinh_muc` | — | Số lượng cho 1 pallet; UNIQUE `(dinh_muc_id, mat_hang_id)` |
  | Phiếu đóng pallet | `phieu_dong_pallet` | `nhap` → `da_duyet`; nhánh `da_huy` | Mã `DP-`; lưu `matHangThanhPham` tại thời điểm lập |
  | Dòng phiếu | `chi_tiet_dong_pallet` | — | `so_luong_dinh_muc`, `so_luong_thuc_te`, `chenh_lech` sinh tự động, lý do |
  | Phiếu xuất hệ thống | `phieu_kho` | `da_hoan_thanh`, `la_he_thong=true` | Xuất vật tư |
  | Phiếu nhập hệ thống | `phieu_kho` | `da_hoan_thanh`, `la_he_thong=true` | Nhập pallet tái chế |
  | Tồn kho | `ton_kho` | — | Trừ vật tư, cộng thành phẩm |
  | Lô thành phẩm | — | — | **không có trong code**: không tạo `lo_hang` cho pallet tái chế |
  | Giá thành | — | — | **không có trong code** |

  ### (c) Dữ liệu đầu vào

  **Định mức — `TaoDinhMucRequest`:**

  | Trường | Kiểu | Bắt buộc | Mặc định | Ghi chú |
  |---|---|---|---|---|
  | `tenDinhMuc` | String (150) | Có (`@NotBlank`) | — | Cắt khoảng trắng |
  | `thanhPhamId` | Long | Có (`@NotNull`) | — | Phải loại `pallet` |
  | `ghiChu` | String (255) | Không | null | — |
  | `chiTiet` | List | Có (`@NotEmpty`) | — | Ít nhất 1 vật tư |
  | `chiTiet[].matHangId` | Long | Có | — | Phải loại `linh_kien`, không phải `LK-TX-11`/`LK-CX-12`, không trùng thành phẩm |
  | `chiTiet[].soLuong` | Integer | Có (`@Positive`) | — | > 0, cho 1 pallet |

  **Phiếu đóng pallet — `TaoPhieuDongPalletRequest`:**

  | Trường | Kiểu | Bắt buộc | Mặc định | Ghi chú |
  |---|---|---|---|---|
  | `dinhMucId` | Long | Có (`@NotNull`) | — | Phải `hoat_dong` |
  | `khoId` | Long | Có (`@NotNull`) | — | Không đổi khi sửa |
  | `soLuongSanXuat` | Integer | Có (`@NotNull`, `@Positive`) | — | > 0 |
  | `ghiChu` | String (255) | Không | null | — |
  | `chiTiet` | List | Không | null | Bỏ trống = dùng đúng định mức |
  | `chiTiet[].matHangId` | Long | Có nếu gửi dòng | — | Có thể là vật tư ngoài định mức |
  | `chiTiet[].soLuongThucTe` | Integer | Có nếu gửi dòng | — | `>= 0` |
  | `chiTiet[].lyDoChenhLech` | String (255) | Chỉ khi thực tế khác định mức | null | Bắt buộc nếu lệch |

  ### (d) Công thức số lượng/giá/tồn

  - Nhu cầu vật tư: `nhuCau = dinhMucMotPallet × soLuongSanXuat`, dùng `Math.multiplyExact`, tràn số thì báo "Số lượng sản xuất quá lớn" (`QuyTacDongPallet.tinhNhuCau`, dòng 11–21).
  - Chênh lệch dòng: `chenhLech = soLuongThucTe − soLuongDinhMuc` (`chenhLech`, dòng 23–25; cột sinh tự động `V7` dòng 50).
  - Tồn khả dụng: `khaDung = ton_kho − so_luong_da_giu_cho − Σ(soLuongThucTe của cùng vật tư trong các phiếu nháp khác)`; thiếu = `max(0, nhuCau − khaDung)` (`themDongDuKien` dòng 150–165; `kiemTraKhaDung` dòng 373–389).
  - Khi duyệt:
    - `ton_kho(vật tư) -= soLuongThucTe` cho từng dòng thực tế > 0;
    - `ton_kho(thành phẩm) += soLuongSanXuat`.
    — `duyet` dòng 278–299.
  - **Không có công thức quy đổi số linh kiện ra số pallet, không có tỷ lệ hao hụt, không có hệ số thu hồi.** Thành phẩm luôn đúng bằng số pallet sản xuất khai báo, bất kể tổng vật tư thực tế nhiều hay ít.
  - **Không có giá vốn/giá thành**: không cộng chi phí linh kiện, nhân công, điện nước vào thành phẩm.

  ### (e) Ràng buộc và kiểm tra

  - Định mức phải `hoat_dong`; thành phẩm loại `pallet`; vật tư loại `linh_kien`; không dùng `LK-TX-11`/`LK-CX-12`; không tự dùng thành phẩm; không trùng dòng; số lượng > 0.
  - Phiếu phải có ít nhất một vật tư với tổng thực tế > 0 (`QuyTacDongPallet.kiemTraCoVatTu`, dòng 38–43).
  - Mỗi vật tư thực tế `>= 0`; lệch định mức bắt buộc có lý do (`QuyTacDongPallet.kiemTraDong`; DB `ck_chenh_lech_co_ly_do`, `V7` dòng 54–55).
  - Không vượt tồn khả dụng khi lưu; khi duyệt `TonKhoNghiepVu.tru` kiểm tra lại tồn thực tế (không trừ phiếu nháp khác).
  - Chỉ sửa/hủy phiếu `nhap`; người sửa là người lập hoặc GD/PGD; đúng kho (`kiemTraNguoiSua` dòng 405–411).
  - Duyệt chỉ khi `nhap`; role `GD/PGD` ở controller (`PhieuDongPalletController` dòng 61–62). **Hàm duyệt không kiểm tra lại quyền kho.**
  - DB: trạng thái `nhap/da_duyet/da_huy`; `so_luong_san_xuat > 0`; `so_luong_thuc_te >= 0`; UNIQUE `(phieu_dong_pallet_id, mat_hang_id)`; phiếu `da_duyet` bắt buộc người duyệt/thời gian (`V7` dòng 23–56).

  ### (f) Ngoại lệ đã xử lý

  - Thiếu vật tư khi lập phiếu: chặn và hiển thị số thiếu; khi duyệt nếu tồn bị phiếu khác lấy trước thì `tru` báo lỗi, rollback toàn bộ.
  - Sửa định mức sau khi đã lập phiếu nháp: nếu định mức + số sản xuất không đổi, giữ snapshot; nếu đổi, dựng lại dòng và kiểm tra lại tồn.
  - Thêm vật tư ngoài định mức: được phép, định mức = 0, bắt buộc lý do.
  - Dùng ít vật tư hơn định mức: được phép nếu có lý do; thành phẩm vẫn đủ số khai báo.
  - Dùng nhiều vật tư hơn định mức: được phép nếu có lý do và đủ tồn.
  - Hủy phiếu nháp: không đụng tồn.
  - Phiếu đã duyệt: không sửa/hủy/đảo; **không có trong code** điều chỉnh thành phẩm hay vật tư sau duyệt.
  - **Không có** kiểm tra thành phẩm hỏng, không có nhập lại pallet hỏng, không có tách lô thành phẩm.

  ---

  ## 8. Bán pallet nguyên trạng và pallet mới

  > Pallet nguyên trạng là `PL-DC-07` (kết quả phân loại nhóm "bán lại"); pallet mới là mặt hàng loại `pallet` mua theo quy trình 2. Cả hai bán qua **cùng một màn hình hóa đơn**, cùng quy tắc giá/tồn/thanh toán; code không phân biệt "mới" và "nguyên trạng" ngoài mã mặt hàng.

  ### (a) Các bước theo thứ tự

  **Đường trực tiếp — lập hóa đơn:**

  1. **GD/PGD/NVBANHANG** mở màn hình bán hàng. Danh mục trả về: kho của nhân viên đang đăng nhập (không có bộ chọn kho trên danh mục), khách hàng `dang_hop_tac`, mặt hàng `dang_kinh_doanh` **trừ** `cho_tai_che`, `dich_vu`, `phe_lieu`; mỗi mặt hàng kèm `donGiaBan` và tồn khả dụng = `ton − da_giu_cho` (`HoaDonServiceImpl.layDanhMucBanHang`, dòng 176–211).
  2. Chọn khách hàng, thêm dòng hàng (mặt hàng + số lượng + chiết khấu % dòng). **Đơn giá không gửi từ client**: hệ thống lấy `mat_hang.don_gia_ban` tại thời điểm lập (`tinhDong`, dòng 398–421). Nhập chiết khấu tổng %, phí giao hàng, VAT (mặc định 8%), hạn thanh toán (mặc định hôm nay +15 ngày), ghi chú; chọn thanh toán ngay hoặc để sau.
  3. Bấm lưu → `POST /api/v1/hoa-don`. Hệ thống gộp dòng trùng mặt hàng, tính tiền, tạo hóa đơn `HD-...` trạng thái `cho_thanh_toan`, `loai_giao_dich = thu_ban_pallet` nếu có ít nhất một dòng loại `pallet`, ngược lại `thu_ban_linh_kien` (`taoMoi`, dòng 215–258).
  4. Nếu **không thanh toán ngay**: hệ thống chỉ **giữ chỗ** tồn (`so_luong_da_giu_cho += số lượng`), chưa xuất kho, chưa tạo phiếu xuất (`apDungTonKho` chế độ `GIU_CHO`, dòng 499–502).
  5. Nếu **thanh toán ngay**: hệ thống trừ tồn ngay (`XUAT`), ghi nhận thanh toán, tạo phiếu xuất kho `PX-...` `da_hoan_thanh`, ghi lịch sử (`taoMoi` dòng 242–250).
  6. Với hóa đơn chờ thanh toán, **GD/PGD/NVBANHANG** bấm thanh toán → `POST /api/v1/hoa-don/{id}/thanh-toan`. Hệ thống chuyển phần giữ chỗ thành xuất kho (`XUAT_TU_GIU_CHO`), ghi một bản ghi `thanh_toan`, tạo phiếu xuất kho, chuyển hóa đơn `da_thanh_toan` (`thanhToan`, dòng 262–281).
  7. Sửa hóa đơn khi `cho_thanh_toan`: được đổi khách hàng, danh sách mặt hàng, hạn thanh toán, ghi chú; khi đổi danh sách, hệ thống **nhả giữ chỗ cũ → xóa dòng cũ → tính lại → giữ chỗ danh sách mới**. Hóa đơn `da_thanh_toan` chỉ được sửa ghi chú (`capNhat`, dòng 288–342).
  8. Hủy hóa đơn: **GD/PGD** bấm xóa → `DELETE /api/v1/hoa-don/{id}?lyDo=...`; chỉ hóa đơn `cho_thanh_toan`, bắt buộc lý do; hệ thống nhả giữ chỗ và chuyển `da_huy`, không xóa cứng (`huy`, dòng 344–363).

  **Đường qua báo giá (nếu dùng):**

  9. Nhân viên bán hàng lập báo giá `BG-...` trạng thái `nhap`, gửi khách (`da_gui`), khách chấp nhận (`da_chap_nhan`) hoặc từ chối (`tu_choi`) (`BaoGiaBanHangService` dòng 81–170; `QuyTacBaoGia.kiemTraChuyenTrangThai` dòng 51–56).
  10. Với báo giá `da_chap_nhan`, bấm chuyển thành hóa đơn → hệ thống **giữ chỗ tồn**, tạo hóa đơn `cho_thanh_toan` với **đơn giá và chiết khấu đã chốt trong báo giá** (khác đường trực tiếp: không lấy lại `don_gia_ban`), VAT và giảm giá tổng theo báo giá, phí giao hàng = 0, hạn mặc định +15 ngày (`chuyenThanhHoaDon`, dòng 174–256). Báo giá chỉ chuyển một lần trừ khi hóa đơn cũ đã hủy.

  ### (b) Chứng từ/record tạo ra và trạng thái

  | Record | Bảng | Trạng thái | Ghi chú |
  |---|---|---|---|
  | Hóa đơn bán | `hoa_don` | `cho_thanh_toan` → `da_thanh_toan`; nhánh `da_huy` | Mã `HD-`; `loai_giao_dich` `thu_ban_pallet`/`thu_ban_linh_kien` |
  | Dòng hóa đơn | `chi_tiet_hoa_don` | — | Đơn giá, chiết khấu %; thành tiền sinh tự động ở DB |
  | Thanh toán | `thanh_toan` | `thanh_cong` | Một bản ghi cho toàn bộ hóa đơn |
  | Lịch sử hóa đơn | `lich_su_hoa_don` | — | Ghi tạo, sửa trường, hủy, thanh toán |
  | Phiếu xuất kho | `phieu_kho` (`xuat_kho`) | `da_hoan_thanh` | Chỉ tạo khi xuất hàng/thanh toán; không có `lo_id` trên dòng |
  | Tồn kho | `ton_kho` | — | Giữ chỗ → trừ thật khi xuất |
  | Báo giá | `bao_gia` | `nhap` → `da_gui` → `da_chap_nhan`/`tu_choi` | Chỉ khi đi đường báo giá; có `hoa_don_id` sau chuyển đổi |
  | Hóa đơn mua/thanh toán NCC | — | — | **không có trong code** |

  ### (c) Dữ liệu đầu vào

  **Lập hóa đơn trực tiếp — `TaoHoaDonRequest`:**

  | Trường | Kiểu | Bắt buộc | Mặc định | Ghi chú |
  |---|---|---|---|---|
  | `khachHangId` | Long | Có (`@NotNull`) | — | Phải `dang_hop_tac` |
  | `khoId` | Long | Không | Kho của nhân viên | Service không kiểm tra quyền kho |
  | `chiTiet` | List | Có (`@NotEmpty`) | — | Ít nhất 1 dòng; dòng trùng mặt hàng được gộp |
  | `chiTiet[].matHangId` | Long | Có | — | Phải `dang_kinh_doanh`, không `cho_tai_che`/`dich_vu`/`phe_lieu` |
  | `chiTiet[].soLuong` | Integer | Có (`@Min 1`) | — | > 0 |
  | `chiTiet[].tyLeChietKhau` | BigDecimal | Không | 0 | 0–100, % theo dòng |
  | `tyLeVat` | BigDecimal | Không | 8 | 0–100 |
  | `phiGiaoHang` | BigDecimal | Không | 0 | `>= 0` |
  | `tyLeGiamGiaTong` | BigDecimal | Không | 0 | 0–100, % trên tổng sau chiết khấu dòng |
  | `hanThanhToan` | LocalDate | Không | Hôm nay +15 ngày | — |
  | `ghiChu` | String (2000) | Không | null | — |
  | `thanhToanNgay` | boolean | Không | false | true = xuất kho và thanh toán ngay |
  | `phuongThucThanhToan` | String | Chỉ khi thanh toán ngay | — | `tien_mat`/`chuyen_khoan`/`qr_code` |
  | `tienKhachDua` | BigDecimal | Chỉ khi tiền mặt | — | Phải >= tổng thanh toán |

  **Sửa hóa đơn — `CapNhatHoaDonRequest`:** tất cả trường tùy chọn, null = giữ nguyên; gồm `khachHangId`, `chiTiet` (cùng cấu trúc dòng), `tyLeVat`, `phiGiaoHang`, `tyLeGiamGiaTong`, `hanThanhToan`, `ghiChu`.

  **Thanh toán — `ThanhToanHoaDonRequest`:**

  | Trường | Kiểu | Bắt buộc | Ghi chú |
  |---|---|---|---|
  | `phuongThuc` | String | Có (`@NotBlank`) | Chỉ 3 giá trị hợp lệ |
  | `tienKhachDua` | BigDecimal | Chỉ khi `tien_mat` | Phải >= tổng; tiền thối = khách đưa − tổng |
  | `maGiaoDichNganHang` | String | Không | Lưu vào `thanh_toan` |

  ### (d) Công thức số lượng/giá/tồn

  - Giá dòng: `gross = don_gia_ban × so_luong`; `chiet_khau_dong = gross × ty_le_chiet_khau / 100` (làm tròn 2, HALF_UP); `thanh_tien = gross − chiet_khau_dong` (`tinhDong`, dòng 412–420; DB sinh `tien_chiet_khau`, `thanh_tien` tại `V1` dòng 341–344).
  - `tong_tien_hang = Σ thanh_tien` các dòng sau khi gộp trùng (`tinhDong`, dòng 415).
  - `tien_giam_gia_tong = tong_tien_hang × ty_le_giam_gia_tong / 100` (làm tròn 2).
  - `tien_truoc_thue = tong_tien_hang − tien_giam_gia_tong + phi_giao_hang`.
  - `tien_vat = tien_truoc_thue × ty_le_vat / 100` (làm tròn 2).
  - `tong_thanh_toan = tien_truoc_thue + tien_vat` (`ganTong`, dòng 425–440).
  - Tồn kho theo chế độ (`apDungTonKho`, dòng 487–516):
    - giữ chỗ: `da_giu_cho += q`, điều kiện `ton − da_giu_cho >= q`;
    - xuất thẳng: `ton -= q`, điều kiện `ton − da_giu_cho >= q`;
    - xuất từ giữ chỗ: `ton -= q`, `da_giu_cho = max(0, da_giu_cho − q)`, điều kiện `ton >= q`;
    - nhả giữ chỗ: `da_giu_cho = max(0, da_giu_cho − q)`.
  - Thanh toán: tiền mặt `thoi_lai = tien_khach_dua − tong_thanh_toan`; chuyển khoản/QR ghi nhận đúng `tong_thanh_toan`, tiền thối = 0; bản ghi `thanh_toan.so_tien = tong_thanh_toan` (`ghiNhanThanhToan`, dòng 451–485).
  - **Không có** chiết khấu theo số tiền tuyệt đối, không có nhiều mức thuế theo dòng, không có tỷ giá, không có giá vốn/COGS.

  ### (e) Ràng buộc và kiểm tra

  - Khách hàng phải `dang_hop_tac` (`timKhachHangHopTac`, dòng 442–449).
  - Mặt hàng phải `dang_kinh_doanh`; chặn `cho_tai_che`, `dich_vu`, `phe_lieu`; số lượng >= 1; chiết khấu 0–100; VAT 0–100; phí giao hàng >= 0; hóa đơn ít nhất 1 dòng (`tinhDong` dòng 398–410; DTO).
  - Tồn khả dụng phải đủ khi giữ chỗ/xuất; nếu chưa có dòng `ton_kho` cho mặt hàng tại kho thì báo "chưa có tồn kho" (`apDungTonKho`, dòng 493–495).
  - Không sửa/hủy hóa đơn `da_thanh_toan` (trừ ghi chú); không sửa/hủy hóa đơn `da_huy`; hóa đơn `da_chot_so = true` bị chặn mọi thay đổi (`kiemTraChoPhepSua`, dòng 367–374).
  - Hủy chỉ khi `cho_thanh_toan`, bắt buộc lý do, chỉ GD/PGD (`huy`; controller dòng 63–64).
  - Role bán hàng: `GD`, `PGD`, `NVBANHANG` (`HoaDonController` dòng 23). **Service không gọi `PhanQuyenKho`** khi lập/sửa/hủy/thanh toán, nên có thể truyền `khoId` bất kỳ.
  - DB: `hoa_don.trang_thai` gồm `nhap/cho_thanh_toan/cho_duyet/da_thanh_toan/qua_han/da_huy` (`V3`); `chi_tiet_hoa_don.so_luong > 0`, đơn giá >= 0, chiết khấu 0–100; `thanh_toan` có trạng thái `dang_cho/da_thanh_toan/het_han` (`V1` dòng 460–470).
  - `qua_han` **không được lưu**: chỉ là cờ tính khi đọc, khi hóa đơn `cho_thanh_toan` và `han_thanh_toan < hôm nay` (`laQuaHan`, dòng 594–598).

  ### (f) Ngoại lệ đã xử lý

  - Thiếu tồn khi giữ chỗ/xuất: chặn, rollback toàn bộ hóa đơn.
  - Hai người bán cùng mặt hàng: khóa bi quan từng dòng `ton_kho` theo thứ tự id mặt hàng để tránh deadlock (`apDungTonKho`, dòng 487–490).
  - Thanh toán tiền mặt thiếu: chặn; thừa: cho phép và tính tiền thối. Chuyển khoản/QR: bỏ qua `tienKhachDua`, ghi đúng tổng.
  - Hóa đơn tổng = 0: chặn thanh toán (`ghiNhanThanhToan`, dòng 456–459).
  - Sửa danh sách hàng khi chờ thanh toán: nhả giữ chỗ cũ trước, nếu danh sách mới thiếu tồn thì rollback, hóa đơn cũ được khôi phục.
  - Hủy hóa đơn chờ thanh toán: nhả giữ chỗ; hóa đơn đã thanh toán không hủy được.
  - Quá hạn: chỉ hiển thị cờ `quaHan`/`soNgayQuaHan`, **không tự chuyển trạng thái**, không có nhắc nợ tự động.
  - **Không có** thanh toán một phần, thanh toán nhiều lần, hoàn tiền, trả hàng bán, công nợ phải thu, sao kê khách hàng, đối trừ công nợ, chiết khấu sau hóa đơn.
  - **Không có** chọn lô khi bán; phiếu xuất bán không gán `lo_id`; không có giá vốn theo lô.

  ---

  ## 9. Bán pallet tái chế

  ### (a) Các bước theo thứ tự

  Quy trình **giống hoàn toàn quy trình 8**; khác biệt chỉ ở nguồn hàng và mã mặt hàng:

  1. Pallet tái chế `PL-TC-08` được tạo bởi phiếu đóng pallet đã duyệt (quy trình 7), tồn kho công ty tăng, **không có lô**.
  2. **GD/PGD/NVBANHANG** mở màn hình bán hàng, chọn khách hàng `dang_hop_tac`, chọn `PL-TC-08`, số lượng, chiết khấu; đơn giá lấy `mat_hang.don_gia_ban` (hoặc giá đã chốt nếu đi qua báo giá).
  3. Lưu hóa đơn `cho_thanh_toan`; giữ chỗ tồn. Thanh toán ngay hoặc sau; khi thanh toán/xuất kho, `ton_kho` của `PL-TC-08` giảm, phiếu xuất `PX-...` được tạo.
  4. Hủy hóa đơn chờ thanh toán được phép; hóa đơn đã thanh toán chỉ sửa ghi chú.
  5. **Không có màn hình/báo giá riêng cho pallet tái chế; không có phân biệt giá mới/cũ/tái chế; không có truy xuất lô thành phẩm.**

  ### (b) Chứng từ/record tạo ra và trạng thái

  Giống quy trình 8: `hoa_don` (`cho_thanh_toan` → `da_thanh_toan`/`da_huy`), `chi_tiet_hoa_don`, `thanh_toan`, `lich_su_hoa_don`, `phieu_kho` xuất, `ton_kho`. Khác biệt: dòng hàng là `PL-TC-08` loại `pallet`, nên `loai_giao_dich = thu_ban_pallet`; **không có `lo_hang` tương ứng**.

  ### (c) Dữ liệu đầu vào

  Giống quy trình 8 (`TaoHoaDonRequest`, `CapNhatHoaDonRequest`, `ThanhToanHoaDonRequest`). Không có trường riêng cho pallet tái chế.

  ### (d) Công thức số lượng/giá/tồn

  Giống quy trình 8. Cụ thể với pallet tái chế:

  - Giá bán = `mat_hang.don_gia_ban` của `PL-TC-08` tại thời điểm lập hóa đơn trực tiếp (hoặc giá đã chốt trong báo giá).
  - `ton_kho(PL-TC-08) -= so_luong` khi xuất; `da_giu_cho` tăng/giảm theo chế độ.
  - Không có giá thành pallet tái chế để tính lãi; báo cáo tái chế chỉ tính hiệu suất số lượng/khối lượng, không tính giá vốn thành phẩm (xem `BaoCaoTaiCheService`).

  ### (e) Ràng buộc và kiểm tra

  Giống quy trình 8. Thêm các điểm cần lưu ý:

  - `PL-TC-08` phải `dang_kinh_doanh`, loại `pallet`; nếu bị đổi trạng thái ngừng kinh doanh thì không bán được.
  - Tồn kho phải có dòng `ton_kho` tại kho bán; pallet tái chế **không có lô** nên không thể chọn/đối chiếu lô.
  - Không có kiểm tra thành phẩm tái chế đã được duyệt hay chưa ngoài việc tồn kho chỉ tăng sau khi phiếu đóng pallet được duyệt.

  ### (f) Ngoại lệ đã xử lý

  Giống quy trình 8. Không có xử lý riêng cho pallet tái chế bị lỗi/hoàn trả; **không có trong code** nghiệp vụ bán pallet tái chế theo lô, theo hợp đồng, hoặc truy xuất nguồn gốc linh kiện.

  ---
  ## 10. Bán phế liệu gỗ

  ### (a) Các bước theo thứ tự

  1. **GD/PGD/NVBANHANG** mở màn hình **Bán phế liệu**. Danh mục trả về kho của nhân viên đang đăng nhập, tồn khả dụng của `PE-GO-15` theo kg (`ton − da_giu_cho`), đơn giá gợi ý = `mat_hang.don_gia_ban`, danh sách khách hàng `dang_hop_tac` (`BanPheLieuService.layDanhMuc`, dòng 60–74).
  2. Chọn khách hàng, nhập **số kg** (> 0), đơn giá/kg (bỏ trống = giá bán hiện hành; nếu nhập phải > 0), VAT (bỏ trống = 8%), hạn thanh toán (bỏ trống = hôm nay +15 ngày), ghi chú; chọn **thanh toán ngay** hoặc **lập hóa đơn chờ thanh toán** (`BanPheLieu.jsx` dòng 169–236).
  3. Bấm bán → `POST /api/v1/ban-phe-lieu`. Hệ thống tạo hóa đơn `HD-...` loại `thu_ban_phe_lieu`, trạng thái `cho_thanh_toan`, một dòng `PE-GO-15` với `soLuong = soKg`, `donGia = giá/kg`, không chiết khấu, không phí giao hàng (`ban`, dòng 76–141).
  4. Nếu thanh toán ngay: trừ tồn kho ngay, ghi nhận thanh toán, tạo phiếu xuất kho `PX-...` `da_hoan_thanh`; hóa đơn chuyển `da_thanh_toan` (`ban`, dòng 120–131; `ghiNhanThanhToan`, dòng 143–167).
  5. Nếu chờ thanh toán: chỉ **giữ chỗ** số kg (`so_luong_da_giu_cho += kg`), chưa xuất kho (dòng 132–136). Thanh toán sau thực hiện qua màn hình hóa đơn dùng chung → `HoaDonServiceImpl.thanhToan`, hệ thống xuất kho từ phần giữ chỗ và tạo phiếu xuất.
  6. Hủy hóa đơn chờ thanh toán thực hiện qua màn hình hóa đơn dùng chung (`HoaDonServiceImpl.huy`): nhả giữ chỗ, chuyển `da_huy`, bắt buộc lý do, chỉ GD/PGD.
  7. **Không có** cân điện tử, nhập nhiều lần cân, trừ độ ẩm/tạp chất, bán theo lô, xuất kho theo lô, hoặc hóa đơn mua bán phế liệu với nhà cung cấp.

  ### (b) Chứng từ/record tạo ra và trạng thái

  | Record | Bảng | Trạng thái | Ghi chú |
  |---|---|---|---|
  | Hóa đơn bán phế liệu | `hoa_don` | `cho_thanh_toan` → `da_thanh_toan`; `da_huy` | Mã `HD-`; `loai_giao_dich=thu_ban_phe_lieu` |
  | Dòng hóa đơn | `chi_tiet_hoa_don` | — | `so_luong` là kg; đơn giá theo kg |
  | Thanh toán | `thanh_toan` | `thanh_cong` | Chỉ một bản ghi toàn bộ hóa đơn |
  | Phiếu xuất kho | `phieu_kho` (`xuat_kho`) | `da_hoan_thanh` | Tạo khi bán thu tiền ngay hoặc khi thanh toán sau |
  | Tồn kho | `ton_kho` | — | `PE-GO-15` giữ chỗ/trừ kg |
  | Lô hàng | `lo_hang` | không đổi | **Không có** lô cho phế liệu; phiếu xuất không gán `lo_id` |
  | Hóa đơn mua/thanh toán NCC | — | — | **không có trong code** |

  ### (c) Dữ liệu đầu vào

  **`BanPheLieuRequest`:**

  | Trường | Kiểu | Bắt buộc | Mặc định | Ghi chú |
  |---|---|---|---|---|
  | `khachHangId` | Long | Có (`@NotNull`) | — | Phải `dang_hop_tac` |
  | `khoId` | Long | Không | Kho của nhân viên | Service không kiểm tra quyền kho |
  | `soKg` | Integer | Có (`@NotNull`, `@Positive`) | — | > 0; lưu nguyên kg |
  | `donGiaKg` | BigDecimal | Không | `mat_hang.don_gia_ban` của `PE-GO-15` | Nếu nhập phải > 0 |
  | `tyLeVat` | BigDecimal | Không | 8 | 0–100 |
  | `hanThanhToan` | LocalDate | Không | Hôm nay +15 ngày | — |
  | `ghiChu` | String (500) | Không | null | — |
  | `thanhToanNgay` | boolean | Không | false | true = trừ tồn và thu tiền ngay |
  | `phuongThucThanhToan` | String | Chỉ khi thanh toán ngay | — | `tien_mat`/`chuyen_khoan`/`qr_code` |
  | `tienKhachDua` | BigDecimal | Chỉ khi tiền mặt | — | Phải >= tổng |

  ### (d) Công thức số lượng/giá/tồn

  - `tong_tien_hang = don_gia_kg × so_kg`, làm tròn 2 (`ban`, dòng 87–93).
  - `tien_vat = tong_tien_hang × ty_le_vat / 100`, làm tròn 2; `tong_thanh_toan = tong_tien_hang + tien_vat` (`QuyTacDichVuThaoDo.tinhVat`, dòng 49–52; `ban`, dòng 94–95).
  - Không có chiết khấu, phí giao hàng, giảm giá tổng; các cột này ghi 0 (`ban`, dòng 110–113).
  - Tồn:
    - thanh toán ngay: `ton_kho(PE-GO-15) -= so_kg` (dòng 121–122);
    - chờ thanh toán: `da_giu_cho += so_kg` (dòng 133–134); khi thanh toán sau: `ton -= kg`, `da_giu_cho = max(0, da_giu_cho − kg)`.
  - Thanh toán tiền mặt: `thoi_lai = tien_khach_dua − tong`; chuyển khoản/QR: ghi nhận đúng tổng, tiền thối 0 (`ghiNhanThanhToan`, dòng 143–167).
  - Không có giá vốn của phế liệu, không có lãi gộp trên hóa đơn.

  ### (e) Ràng buộc và kiểm tra

  - Khách hàng phải `dang_hop_tac` (`ban`, dòng 79–83).
  - `soKg > 0`; đơn giá > 0 (sau khi lấy giá mặc định); VAT 0–100 (`BanPheLieuRequest`; `ban`, dòng 87–90).
  - Tồn khả dụng `ton − da_giu_cho >= soKg`, nếu không chặn; kho phải có dòng tồn phế liệu (`ban`, dòng 97–103).
  - Phương thức thanh toán chỉ 3 giá trị; tiền mặt phải đủ (`ghiNhanThanhToan`, dòng 143–156).
  - Role `GD/PGD/NVBANHANG` (`BanPheLieuController` dòng 18). **Service không kiểm tra `PhanQuyenKho`**, nên `khoId` tùy ý.
  - Hủy hóa đơn chờ thanh toán dùng quy tắc chung: chỉ GD/PGD, bắt buộc lý do, nhả giữ chỗ (`HoaDonServiceImpl.huy`).
  - Hóa đơn đã thanh toán chỉ sửa ghi chú; nếu gửi danh sách dòng mới sẽ bị chặn vì `phe_lieu` không được phép ở `tinhDong` (`HoaDonServiceImpl.capNhat` dòng 288–342; `tinhDong` dòng 404–409).
  - DB: `chi_tiet_hoa_don.so_luong > 0`; `thanh_toan.so_tien > 0`; `hoa_don.tong_thanh_toan >= 0` (`V1`).

  ### (f) Ngoại lệ đã xử lý

  - Không có tồn phế liệu tại kho: báo "Kho ... chưa có phế liệu" (dòng 97–98).
  - Bán vượt tồn khả dụng: chặn, rollback.
  - Chưa có đơn giá (cả giá mặt hàng và giá nhập đều null/<=0): báo "Chưa có đơn giá phế liệu..." (dòng 88–90).
  - Thanh toán tiền mặt thiếu: chặn; thừa: tính tiền thối.
  - Hủy hóa đơn chờ thanh toán: nhả giữ chỗ; hóa đơn đã thanh toán không hủy được.
  - **Không có** bán một phần rồi hủy phần còn lại, không có trả hàng, không có điều chỉnh kg sau khi lập, không có chuyển kho phế liệu.

  ---

  ## 11. Dịch vụ tháo dỡ cho khách

  ### (a) Các bước theo thứ tự

  **Tiếp nhận pallet của khách:**

  1. **GD/PGD/NVKHO** mở màn hình **Dịch vụ khách**, bấm tiếp nhận. Chọn khách hàng, kho, nhập **số pallet nhận** (> 0), **đơn giá dịch vụ/pallet** (`>= 0`), ghi chú. Lưu → phiếu `DV-...` trạng thái `da_tiep_nhan` (`PhieuDichVuThaoDoServiceImpl.tiepNhan`, dòng 96–120).
  2. Hệ thống cộng số pallet nhận vào **sổ tồn của khách** `ton_kho_khach` tại mặt hàng `PL-CTD-06`, **không cộng vào `ton_kho` công ty** (dòng 115–116; `TonKhoKhachNghiepVu.cong` dòng 19–25).
  3. **Không có kiểm tra khách hàng đang hợp tác** ở bước tiếp nhận (khác với bán hàng); chỉ kiểm tra khách hàng tồn tại (`tiepNhan`, dòng 103–104).

  **Ghi nhận và duyệt kết quả tháo dỡ:**

  4. **GD/PGD/NVKHO** mở phiếu, nhập kết quả: số pallet đã tháo dỡ (<= số nhận), số thanh nguyên, số cục vuông, số thanh cần xử lý, số cục cần xử lý, số kg phế liệu — tất cả **nhập tay**. Lưu → phiếu `cho_duyet`; có thể sửa lại kết quả khi còn `da_tiep_nhan` hoặc `cho_duyet` (`ghiNhanKetQua`, dòng 124–149).
  5. **GD/PGD** bấm duyệt → `POST /api/v1/dich-vu-thao-do/{id}/duyet`. Hệ thống kiểm tra lại quy tắc, rồi cập nhật **sổ khách**:
    - `PL-CTD-06 -= soPalletThaoDo`;
    - `LK-TN-09 += soThanhNguyen`;
    - `LK-CV-10 += soCucVuong`;
    - `LK-TX-11 += soThanhCanXuLy`;
    - `LK-CX-12 += soCucCanXuLy`;
    - `PE-GO-15 += kgPheLieu`;
    - tính `phiDichVu = soPalletThaoDo × donGiaDichVu`;
    - phiếu chuyển `da_thao_do`, lưu người duyệt/thời gian (`duyet`, dòng 151–180).
  6. **Không có** cộng tồn công ty, không tạo phiếu kho, không tạo lô, không tạo hóa đơn ở bước duyệt.

  **Trả hàng cho khách:**

  7. Sau khi duyệt, **GD/PGD/NVKHO** bấm trả hàng → `POST /api/v1/dich-vu-thao-do/{id}/tra-hang`. Hệ thống trừ khỏi sổ khách:
    - số pallet chưa tháo: `soPalletNhan − soPalletThaoDo` của `PL-CTD-06`;
    - toàn bộ số thanh/cục đã ghi nhận;
    - số kg phế liệu **còn lại** `kgPheLieu − kgPheLieuDaMua`;
    - phiếu chuyển `da_tra_hang` (`traHang`, dòng 184–209).
  8. Sau trả hàng, phiếu không quay lại `da_thao_do`; **không mua lại phế liệu được nữa** (xem bước 10).

  **Lập hóa đơn dịch vụ:**

  9. **GD/PGD** bấm lập hóa đơn → `POST /api/v1/dich-vu-thao-do/{id}/lap-hoa-don`, chỉ khi phiếu `da_thao_do` hoặc `da_tra_hang`, chưa có hóa đơn còn hiệu lực, và `phiDichVu > 0`. Hệ thống tạo hóa đơn `HD-...` loại `thu_dich_vu_thao_do`, trạng thái `cho_thanh_toan`, một dòng `DV-TD-99` với số lượng = `soPalletThaoDo`, đơn giá = `donGiaDichVu`, VAT mặc định 8%, hạn mặc định +15 ngày; gắn hóa đơn vào phiếu (`DichVuThaoDoTaiChinhService.lapHoaDon`, dòng 64–116).
  10. Thanh toán hóa đơn dịch vụ thực hiện qua màn hình hóa đơn dùng chung → `HoaDonServiceImpl.thanhToan`; dòng `dich_vu` bị bỏ qua khi xuất tồn nên không tạo phiếu xuất kho (`HoaDonServiceImpl.apDungTonKho` dòng 488–490; `taoPhieuXuatKho` dòng 522–525).

  **Mua lại phế liệu của khách (tùy chọn, trước khi trả hàng):**

  11. **GD/PGD** bấm mua lại phế liệu → `POST /api/v1/dich-vu-thao-do/{id}/mua-lai-phe-lieu`, chỉ khi phiếu `da_thao_do` (chưa trả hàng). Nhập số kg (<= `kgPheLieu − kgPheLieuDaMua`), đơn giá/kg (> 0), phương thức thanh toán, ghi chú. Hệ thống:
      - trừ kg khỏi sổ khách, cộng kg vào `ton_kho` công ty;
      - tạo phiếu nhập kho hệ thống `PN-...` `da_hoan_thanh`;
      - tạo hóa đơn **chi** `chi_mua_phe_lieu_khach` trạng thái `da_thanh_toan`, VAT = 0, tổng = kg × đơn giá, thanh toán ngay bằng phương thức đã chọn;
      - tạo bản ghi `thanh_toan` và `mua_lai_phe_lieu_khach`;
      - cộng dồn `kgPheLieuDaMua` trên phiếu (`muaLaiPheLieu`, dòng 120–190).
  12. **Không có** mua lại thanh/cục hoặc pallet nguyên trạng của khách; chỉ mua phế liệu gỗ.

  **Hủy:**

  13. Hủy chỉ khi phiếu `da_tiep_nhan` hoặc `cho_duyet`, bắt buộc lý do; hệ thống trừ lại toàn bộ `soPalletNhan` khỏi sổ khách `PL-CTD-06` (trả khách nguyên trạng), chuyển `da_huy` (`huy`, dòng 211–234).

  ### (b) Chứng từ/record tạo ra và trạng thái

  | Record | Bảng | Trạng thái | Ghi chú |
  |---|---|---|---|
  | Phiếu dịch vụ | `phieu_dich_vu_thao_do` | `da_tiep_nhan` → `cho_duyet` → `da_thao_do` → `da_tra_hang`; nhánh `da_huy` | Mã `DV-` |
  | Sổ tồn khách | `ton_kho_khach` | không có trạng thái | UNIQUE `(khach_hang_id, mat_hang_id, kho_id)`; `so_luong >= 0` |
  | Mua lại phế liệu | `mua_lai_phe_lieu_khach` | — | Mỗi lần mua một dòng; `so_kg > 0`, `don_gia_kg > 0` |
  | Hóa đơn dịch vụ | `hoa_don` | `cho_thanh_toan` → `da_thanh_toan`; `da_huy` | `thu_dich_vu_thao_do`; gắn `hoa_don_id` trên phiếu |
  | Hóa đơn mua phế liệu | `hoa_don` | tạo thẳng `da_thanh_toan` | `chi_mua_phe_lieu_khach`; VAT 0 |
  | Thanh toán | `thanh_toan` | `thanh_cong` | Của hóa đơn dịch vụ (sau) hoặc hóa đơn mua phế liệu (ngay) |
  | Phiếu nhập kho | `phieu_kho` (`nhap_kho`) | `da_hoan_thanh` | Chỉ khi mua lại phế liệu; `la_he_thong=true` |
  | Tồn kho công ty | `ton_kho` | — | Chỉ tăng khi mua lại phế liệu; pallet/linh kiện của khách không vào đây |
  | Lô hàng | `lo_hang` | — | **không có trong code** cho hàng của khách và phế liệu mua lại |

  ### (c) Dữ liệu đầu vào

  **Tiếp nhận — `TiepNhanDichVuRequest`:**

  | Trường | Kiểu | Bắt buộc | Mặc định | Ghi chú |
  |---|---|---|---|---|
  | `khachHangId` | Long | Có (`@NotNull`) | — | Không kiểm tra trạng thái hợp tác |
  | `khoId` | Long | Có (`@NotNull`) | — | Phải được phép thao tác |
  | `soPalletNhan` | Integer | Có (`@NotNull`, `@Positive`) | — | > 0 |
  | `donGiaDichVu` | BigDecimal | Có (`@NotNull`, `@PositiveOrZero`) | — | `>= 0`/pallet |
  | `ghiChu` | String (255) | Không | null | — |

  **Kết quả — `KetQuaDichVuRequest`:**

  | Trường | Kiểu | Bắt buộc | Mặc định | Ghi chú |
  |---|---|---|---|---|
  | `soPalletThaoDo` | Integer | Có (`@NotNull`, `@Positive`) | — | > 0 và <= `soPalletNhan` |
  | `soThanhNguyen` | Integer | Không | 0 | `>= 0` |
  | `soCucVuong` | Integer | Không | 0 | `>= 0` |
  | `soThanhCanXuLy` | Integer | Không | 0 | `>= 0` |
  | `soCucCanXuLy` | Integer | Không | 0 | `>= 0` |
  | `kgPheLieu` | Integer | Không | 0 | `>= 0` |

  **Lập hóa đơn dịch vụ — `LapHoaDonDichVuRequest`:**

  | Trường | Kiểu | Bắt buộc | Mặc định | Ghi chú |
  |---|---|---|---|---|
  | `tyLeVat` | BigDecimal | Không | 8 | 0–100 |
  | `hanThanhToan` | LocalDate | Không | Hôm nay +15 ngày | — |
  | `ghiChu` | String (500) | Không | null | Ghép vào ghi chú hóa đơn |

  **Mua lại phế liệu — `MuaLaiPheLieuRequest`:**

  | Trường | Kiểu | Bắt buộc | Mặc định | Ghi chú |
  |---|---|---|---|---|
  | `soKg` | Integer | Có (`@NotNull`, `@Positive`) | — | <= phần phế liệu còn lại |
  | `donGiaKg` | BigDecimal | Có (`@NotNull`, `@Positive`) | — | > 0 |
  | `phuongThucThanhToan` | String | Có (`@NotBlank`) | — | 3 giá trị hợp lệ |
  | `ghiChu` | String (500) | Không | null | — |

  ### (d) Công thức số lượng/giá/tồn

  - Sổ khách sau tiếp nhận: `ton_kho_khach(PL-CTD-06) += soPalletNhan`.
  - Sổ khách sau duyệt: `PL-CTD-06 -= soPalletThaoDo`; các linh kiện/phế liệu cộng đúng số nhập tay (`duyet`, dòng 164–170).
  - Số pallet trả nguyên: `soPalletNhan − soPalletThaoDo`; hiển thị 0 nếu chưa có kết quả (`toResponse`, dòng 266–281).
  - Phí dịch vụ: `phiDichVu = soPalletThaoDo × donGiaDichVu`, làm tròn 2 (`QuyTacDichVuThaoDo.tinhPhi`, dòng 32–35).
  - Hóa đơn dịch vụ: `tienTruocThue = phiDichVu`; `tienVat = phiDichVu × tyLeVat / 100`; `tongThanhToan = tienTruocThue + tienVat` (`lapHoaDon`, dòng 82–99).
  - Mua phế liệu: `tong = soKg × donGiaKg`, làm tròn 2; VAT = 0; tổng thanh toán = tổng (`muaLaiPheLieu`, dòng 137–168).
  - Sổ khách sau mua phế liệu: `PE-GO-15` khách `-= kg`; `ton_kho` công ty `PE-GO-15 += kg`; `kgPheLieuDaMua += kg` (`muaLaiPheLieu`, dòng 140–142, 185).
  - **Không có tỷ lệ quy đổi pallet → linh kiện/kg**, không có định mức dịch vụ, không có chi phí nhân công.

  ### (e) Ràng buộc và kiểm tra

  - Tiếp nhận: `soPalletNhan > 0`; `donGiaDichVu >= 0`; khách hàng tồn tại; kho được phép thao tác (`QuyTacDichVuThaoDo.kiemTraTiepNhan`, dòng 13–20).
  - Kết quả: `soPalletThaoDo > 0`, `<= soPalletNhan`; các kết quả `>= 0`; ít nhất một kết quả > 0; dùng chung quy tắc tháo dỡ (`QuyTacDichVuThaoDo.kiemTraKetQua`, dòng 22–30; `QuyTacTaiChe.kiemTraThaoDo`).
  - Duyệt chỉ khi `cho_duyet`; role GD/PGD; có kiểm tra kho (`duyet` không gọi `PhanQuyenKho` — **không kiểm tra lại kho của người duyệt**).
  - Trả hàng chỉ khi `da_thao_do`; có kiểm tra kho; sổ khách phải đủ hàng để trừ, nếu không `TonKhoKhachNghiepVu.tru` báo lỗi.
  - Lập hóa đơn: chỉ `da_thao_do`/`da_tra_hang`; chưa có hóa đơn còn hiệu lực; `phiDichVu > 0`; role GD/PGD; có kiểm tra kho (`lapHoaDon`, dòng 65–78).
  - Mua phế liệu: chỉ `da_thao_do`; phương thức hợp lệ; `soKg > 0` và `<= kgPheLieu − kgPheLieuDaMua`; đơn giá > 0; role GD/PGD; có kiểm tra kho (`muaLaiPheLieu`, dòng 121–133; `QuyTacDichVuThaoDo.kiemTraMuaLai`).
  - Hủy: chỉ trước duyệt; lý do bắt buộc; có kiểm tra kho (`huy`, dòng 211–225).
  - DB: `ton_kho_khach.so_luong >= 0`; UNIQUE `(khach_hang, mat_hang, kho)`; `phieu_dich_vu_thao_do.so_pallet_thao_do <= so_pallet_nhan`; trạng thái chỉ 5 giá trị; phiếu `cho_duyet/da_thao_do/da_tra_hang` phải có ít nhất một kết quả; phiếu `da_thao_do/da_tra_hang` phải có người duyệt, thời gian duyệt, `phi_dich_vu`; `kg_phe_lieu_da_mua <= kg_phe_lieu` (`V8` dòng 3–47; `V9` dòng 36–41).

  ### (f) Ngoại lệ đã xử lý

  - Khách không còn hợp tác sau khi tiếp nhận: **không bị chặn** ở các bước sau (chỉ bước bán hàng kiểm tra trạng thái khách).
  - Số pallet tháo dỡ > số nhận: chặn; tổng kết quả = 0: chặn; kg > 0 mà không có hàng bỏ: chặn (theo quy tắc chung).
  - Trả hàng khi sổ khách không đủ (do dữ liệu lệch): chặn, rollback.
  - Mua phế liệu vượt phần còn lại: chặn; mua sau khi đã trả hàng: chặn.
  - Hủy sau khi đã duyệt: chặn; hủy trước duyệt: trả lại toàn bộ pallet đã nhận vào sổ khách.
  - Lập hóa đơn hai lần: chặn nếu hóa đơn cũ chưa hủy; nếu hóa đơn cũ đã hủy thì được lập lại.
  - Phí dịch vụ = 0: không lập được hóa đơn.
  - **Không có** xử lý khách không đến lấy hàng, không có phí lưu kho, không có chuyển quyền sở hữu linh kiện/phế liệu, không có bán linh kiện của khách.

  ---

  ## 12. Hóa đơn và thanh toán nhà cung cấp

  ### (a) Các bước theo thứ tự

  **Không có trong code.** Không tồn tại màn hình, controller, service, endpoint hay job nào thực hiện quy trình:

  - lập hóa đơn mua từ đơn mua/phiếu nhận;
  - ghi nhận công nợ phải trả nhà cung cấp;
  - thanh toán nhà cung cấp (toàn bộ hoặc một phần);
  - phân bổ thanh toán cho nhiều hóa đơn;
  - đối chiếu công nợ, tuổi nợ, sao kê nhà cung cấp;
  - hạch toán giá vốn/chi phí mua.

  Các mảnh hiện có và điểm dừng:

  1. **Đơn mua** (`don_mua`) có nhà cung cấp, tổng tiền, trạng thái `nhap → da_xac_nhan → da_nhan_mot_phan → da_nhan_du` (hoặc `da_huy`). Nhận hàng tạo phiếu nhập và lô, ghi `lo_hang.don_gia_nhap`; **không sinh hóa đơn mua, không sinh công nợ, không sinh thanh toán** (`DonMuaServiceImpl.nhanHang`, dòng 248–329).
  2. **Nhà cung cấp** có CRUD (`NhaCungCapController`), nhưng không có bảng công nợ theo nhà cung cấp.
  3. **Schema `hoa_don`** cho phép `ncc_id` và các loại `chi_mua_pallet_cu`, `chi_nguyen_lieu_phu_tro`, `chi_sua_chua`, `chi_van_chuyen`, `chi_khac` (`V1` dòng 284–331; `V9` dòng 15–33). Tuy nhiên **không có service/controller/UI nào tạo các loại này**. `HoaDonController` chỉ phục vụ bán hàng (`GD/PGD/NVBANHANG`) và service chỉ tạo hóa đơn bán.
  4. **Chi phí vận hành** (`chi_phi_van_hanh`) có CRUD cho GD/PGD: loại chi phí, mô tả, số tiền, kỳ, ngày chi, ghi chú. Đây là khoản chi **nhập tay**, **không gắn nhà cung cấp, không có số hóa đơn, không có công nợ, không có thanh toán** (`TaiChinhController` dòng 52–81; `ChiPhiVanHanhRequest`).
  5. **Bảng `thanh_toan`** chỉ gắn với `hoa_don_id`; code chỉ dùng cho hóa đơn bán/dịch vụ và hóa đơn mua phế liệu của khách. **Không có thanh toán nhà cung cấp.**
  6. **Hóa đơn chi duy nhất được tạo trong code** là `chi_mua_phe_lieu_khach` (mua phế liệu của khách, quy trình 11), đối tượng là khách hàng, không phải nhà cung cấp, và trả tiền ngay.
  7. **Báo cáo tài chính** có tổng hợp `chi%` đã thanh toán (`TaiChinhRepository.tongChiHoaDon`, dòng 29–31) nhưng thực tế chỉ có hóa đơn mua phế liệu khách; không có báo cáo công nợ phải trả.

  ### (b) Chứng từ/record tạo ra và trạng thái

  | Record | Bảng | Trạng thái | Ghi chú |
  |---|---|---|---|
  | Hóa đơn mua | — | — | **không có trong code** |
  | Công nợ phải trả | — | — | **không có trong code** |
  | Thanh toán NCC | — | — | **không có trong code** |
  | Đơn mua | `don_mua` | 5 trạng thái như quy trình 1 | Chỉ dùng để đặt/nhận hàng |
  | Lô hàng có giá nhập | `lo_hang` | `con_hang`/`da_het` | Giá lấy từ đơn mua, không từ hóa đơn |
  | Chi phí vận hành | `chi_phi_van_hanh` | không có trạng thái | Khoản chi thủ công, không gắn NCC |
  | Hóa đơn chi mua phế liệu khách | `hoa_don` | `da_thanh_toan` | Chỉ là ngoại lệ của quy trình 11 |

  ### (c) Dữ liệu đầu vào

  **Không có DTO/request cho hóa đơn mua hoặc thanh toán nhà cung cấp.** Dữ liệu gần nhất:

  **`ChiPhiVanHanhRequest` (chi phí vận hành, không phải hóa đơn mua):**

  | Trường | Kiểu | Bắt buộc | Mặc định | Ghi chú |
  |---|---|---|---|---|
  | `khoId` | Long | Không | Kho của người dùng | — |
  | `loaiChiPhi` | String | Có (`@NotBlank`, pattern) | — | `van_tai_logistics`, `nhan_su`, `bao_tri_thiet_bi`, `dien_nang_luong`, `khac` |
  | `moTa` | String (255) | Không | null | — |
  | `soTien` | BigDecimal | Có (`@NotNull`, `>= 0.01`) | — | > 0 |
  | `ky` | String | Không | `thang` | `thang`/`quy`/`nam` |
  | `ngayChi` | LocalDate | Có (`@NotNull`) | — | — |
  | `ghiChu` | String (255) | Không | null | — |

  ### (d) Công thức số lượng/giá/tồn

  - Đơn mua: `tong_tien = Σ(so_luong_dat × don_gia)` (`DonMuaServiceImpl.tinhTong`, dòng 363–367).
  - Lô nhập: `don_gia_nhap = don_gia` của dòng đơn mua (`nhanHang`, dòng 299–309).
  - **Không có** công thức thuế mua, chiết khấu mua, phí vận chuyển mua, tỷ giá, giá vốn bình quân, phân bổ chi phí mua vào lô.
  - Chi phí vận hành: chỉ là `soTien` nhập tay, không tính từ hóa đơn/chứng từ.

  ### (e) Ràng buộc và kiểm tra

  - Không có ràng buộc nghiệp vụ mua/thanh toán NCC ở tầng service.
  - Ràng buộc schema (chỉ là cấu trúc, không có xử lý):
    - `hoa_don.loai_giao_dich` chỉ nhận các mã đã seed; `chi_mua_pallet_cu` bắt buộc `ncc_id` và không có `khach_hang_id`; `chi_nguyen_lieu_phu_tro`/`chi_sua_chua`/`chi_van_chuyen` không được có khách hàng (`V1` dòng 320–331; `V9` dòng 22–33).
    - `thanh_toan` chỉ gắn `hoa_don_id`, không có `ncc_id`.
    - `chi_phi_van_hanh.so_tien > 0` (kiểm tra qua DTO; DB xem `V1` dòng 551 trở đi).
  - Quyền: chi phí vận hành chỉ GD/PGD được thêm/sửa/xóa; xem được bởi GD/PGD/NVBANHANG (`TaiChinhController` dòng 26, 61–81).

  ### (f) Ngoại lệ đã xử lý

  - **Không có** ngoại lệ nào cho hóa đơn/thanh toán nhà cung cấp vì không có nghiệp vụ.
  - Với chi phí vận hành: DTO chặn số tiền <= 0, loại chi phí sai, ngày chi thiếu; không có kiểm tra trùng, không có duyệt, không có đính kèm chứng từ.

  ---

  ## 13. Hóa đơn và thanh toán khách hàng

  > Phần này mô tả **cơ chế hóa đơn/thanh toán dùng chung** cho các quy trình 8, 9, 10, 11. Chi tiết lập từng loại hóa đơn đã nêu ở các quy trình tương ứng; dưới đây là vòng đời, công thức và các điểm dùng chung.

  ### (a) Các bước theo thứ tự

  1. **Lập hóa đơn:** hóa đơn bán pallet/linh kiện, bán phế liệu, dịch vụ tháo dỡ đều được tạo ở trạng thái `cho_thanh_toan` (trừ hóa đơn mua phế liệu của khách tạo thẳng `da_thanh_toan`). Mã `HD-...` sinh từ `getMaChungTu("HD","HD-")`.
  2. **Xem danh sách/chi tiết:** `GET /api/v1/hoa-don` lọc tùy chọn theo trạng thái; `GET /api/v1/hoa-don/{id}` trả dòng hàng, lịch sử, thông tin đối tác, cờ quá hạn. **Không lọc theo kho và không kiểm tra quyền trên từng hóa đơn** (`HoaDonServiceImpl.layDanhSach` dòng 85–92; `layChiTiet` dòng 94–174).
  3. **Sửa hóa đơn chờ thanh toán:** đổi khách hàng, danh sách mặt hàng, hạn thanh toán, ghi chú. Khi đổi danh sách, hệ thống nhả giữ chỗ cũ, xóa dòng cũ, tính lại tiền, giữ chỗ danh sách mới; ghi lịch sử từng trường (`capNhat`, dòng 288–342).
  4. **Thanh toán:** `POST /api/v1/hoa-don/{id}/thanh-toan` với phương thức `tien_mat`/`chuyen_khoan`/`qr_code`.
    - Kiểm tra hóa đơn `cho_thanh_toan`, chưa chốt sổ, chưa hủy.
    - Chuyển phần giữ chỗ thành xuất kho (`XUAT_TU_GIU_CHO`), trừ tồn thật.
    - Ghi **một** bản ghi `thanh_toan` với `so_tien = tong_thanh_toan`, trạng thái `thanh_cong`.
    - Tạo phiếu xuất kho `PX-...` nếu hóa đơn có dòng hàng không phải `dich_vu`.
    - Chuyển hóa đơn `da_thanh_toan`, lưu `ngay_thanh_toan`, phương thức, tiền khách đưa, tiền thối; ghi lịch sử (`thanhToan`, dòng 262–281; `ghiNhanThanhToan`, dòng 451–485).
  5. **Hủy hóa đơn chờ thanh toán:** GD/PGD bấm xóa, nhập lý do; hệ thống nhả giữ chỗ, chuyển `da_huy`, ghi lịch sử. Hóa đơn đã thanh toán không hủy được (`huy`, dòng 344–363).
  6. **Quá hạn:** hệ thống chỉ tính cờ `quaHan = (trangThai = cho_thanh_toan && hanThanhToan < hôm nay)` khi đọc; **không tự đổi trạng thái**, không gửi nhắc nợ, không khóa bán hàng (`laQuaHan`, dòng 594–598).
  7. **Chốt sổ:** cột `da_chot_so` tồn tại; nếu `true` thì mọi sửa/hủy/thanh toán bị chặn (`kiemTraChoPhepSua`, dòng 367–374). **Không có service/controller nào đặt `da_chot_so = true`** trong code; chỉ có thể do thao tác DB bên ngoài.
  8. **Báo cáo doanh thu:** chỉ cộng hóa đơn `da_thanh_toan` có `loai_giao_dich LIKE 'thu%'`, theo `ngay_thanh_toan` (`TaiChinhRepository.tongDoanhThu`, dòng 21–23). Hóa đơn chi `chi%` được cộng riêng vào chi phí (`tongChiHoaDon`, dòng 29–31; `TaiChinhService.baoCaoLoiNhuan` dòng 221–225).

  ### (b) Chứng từ/record tạo ra và trạng thái

  | Record | Bảng | Trạng thái | Ghi chú |
  |---|---|---|---|
  | Hóa đơn | `hoa_don` | `cho_thanh_toan` → `da_thanh_toan`; `da_huy` | DB còn cho phép `nhap`, `cho_duyet`, `qua_han` nhưng code không dùng |
  | Dòng hóa đơn | `chi_tiet_hoa_don` | — | `so_luong > 0`; thành tiền/chiết khấu sinh tự động |
  | Thanh toán | `thanh_toan` | `thanh_cong` | Một bản ghi cho toàn bộ hóa đơn; không UNIQUE `hoa_don_id` |
  | Lịch sử | `lich_su_hoa_don` | — | `tao_moi`, `cap_nhat`, `huy`; không có `duyet` từ service hóa đơn |
  | Phiếu xuất kho | `phieu_kho` | `da_hoan_thanh` | Không tạo nếu hóa đơn chỉ có dịch vụ |
  | Tồn kho | `ton_kho` | — | Giữ chỗ/trừ thật theo dòng hàng |
  | Công nợ phải thu | — | — | **không có trong code** (chỉ có hóa đơn + thanh toán) |
  | Phiếu thu/UNC | — | — | **không có trong code** |

  ### (c) Dữ liệu đầu vào

  **Lập hóa đơn:** `TaoHoaDonRequest` như quy trình 8.
  **Sửa:** `CapNhatHoaDonRequest` như quy trình 8.
  **Thanh toán:** `ThanhToanHoaDonRequest` gồm `phuongThuc` (bắt buộc), `tienKhachDua`, `maGiaoDichNganHang`.
  **Hủy:** query param `lyDo` (bắt buộc trên thực tế; service chặn nếu rỗng).

  ### (d) Công thức số lượng/giá/tồn

  - Công thức tiền dùng chung như quy trình 8:
    - `thanh_tien_dong = don_gia × so_luong − chiết_khấu_dòng`;
    - `tong_tien_hang = Σ thanh_tien_dong`;
    - `tien_giam_gia_tong = tong_tien_hang × ty_le_giam_gia_tong / 100`;
    - `tien_truoc_thue = tong_tien_hang − tien_giam_gia_tong + phi_giao_hang`;
    - `tien_vat = tien_truoc_thue × ty_le_vat / 100`;
    - `tong_thanh_toan = tien_truoc_thue + tien_vat`.
  - Thanh toán:
    - tiền mặt: bắt buộc `tien_khach_dua >= tong_thanh_toan`; `tien_thoi_lai = tien_khach_dua − tong_thanh_toan`;
    - chuyển khoản/QR: `tien_khach_dua = tong_thanh_toan`; `tien_thoi_lai = 0`;
    - `thanh_toan.so_tien = tong_thanh_toan` (không phải số khách đưa).
  - Tồn: xem 4 chế độ ở quy trình 8 (`GIU_CHO`, `XUAT`, `XUAT_TU_GIU_CHO`, `NHA_GIU_CHO`).
  - Doanh thu báo cáo = tổng `tong_thanh_toan` của hóa đơn `da_thanh_toan` + `loai_giao_dich LIKE 'thu%'`; không trừ chiết khấu thêm lần nữa (đã nằm trong tổng).

  ### (e) Ràng buộc và kiểm tra

  - Hóa đơn phải có ít nhất 1 dòng; khách hàng `dang_hop_tac`; mặt hàng `dang_kinh_doanh` và đúng loại được bán (không `cho_tai_che`/`dich_vu`/`phe_lieu` ở hóa đơn thường).
  - Không sửa/hủy hóa đơn `da_huy` hoặc `da_chot_so`; hóa đơn `da_thanh_toan` chỉ sửa ghi chú; hủy chỉ khi `cho_thanh_toan`.
  - Thanh toán chỉ khi `cho_thanh_toan`; tổng > 0; phương thức hợp lệ; tiền mặt đủ.
  - Không có thanh toán một phần, không có nhiều lần thanh toán, không có hoàn tiền.
  - Quyền: `GD/PGD/NVBANHANG` cho toàn bộ API hóa đơn; hủy chỉ GD/PGD (`HoaDonController` dòng 23, 63–64). Service không kiểm tra kho/đối tượng.
  - DB: trạng thái hóa đơn theo `V3`; `chi_tiet_hoa_don.so_luong > 0`; `thanh_toan.so_tien > 0`, phương thức/trạng thái theo `V1` dòng 445–458.
  - **Không có khóa trên bản ghi hóa đơn khi thanh toán** (`HoaDonRepository` không có `findForUpdate`; entity không có `@Version`), và `thanh_toan` không có UNIQUE `hoa_don_id`. Về mặt code không có cơ chế chặn hai yêu cầu thanh toán đồng thời.

  ### (f) Ngoại lệ đã xử lý

  - Thiếu tồn khi giữ chỗ/xuất: chặn, rollback.
  - Hóa đơn tổng = 0: chặn thanh toán.
  - Thanh toán tiền mặt thiếu: chặn; thừa: tính tiền thối.
  - Sửa hóa đơn chờ thanh toán làm thiếu tồn: rollback, dữ liệu cũ được giữ.
  - Hủy hóa đơn chờ thanh toán: nhả giữ chỗ; hóa đơn đã thanh toán/đã hủy không hủy được.
  - Quá hạn: chỉ là cờ hiển thị; không có xử lý tự động.
  - **Không có** đối chiếu công nợ, nhắc nợ, lãi chậm trả, chiết khấu thanh toán, thanh toán trước, đặt cọc, công nợ đầu kỳ, hoặc xuất hóa đơn điện tử (cột `file_pdf` tồn tại nhưng không có service sinh file).

  ---
  # PHẦN 4 — BẢY CÂU HỎI XUYÊN QUY TRÌNH VÀ TỔNG HỢP

  > Phần này trả lời trực tiếp 7 câu hỏi về cách mã nguồn lưu dữ liệu và xử lý nghiệp vụ. Các kết luận dưới đây chỉ dựa trên code hiện tại (Flyway `V1..V12`), không dựa trên quy trình mong muốn.

  ## 4.1. Câu hỏi 1 — Số lượng pallet được lưu như thế nào (chiếc hay theo lô; kg/m³)?

  **Kết luận ngắn: tồn kho lưu theo chiếc (hoặc kg với phế liệu), gộp theo cặp `(mặt hàng, kho)`. Lô hàng là chứng từ theo dõi số lượng nhập và số còn lại, không phải chiều lưu trữ tồn kho. Không có m³, không có quy đổi chiếc ↔ kg, không có quy đổi kích thước ↔ khối lượng.**

  ### Nơi lưu số lượng

  | Dữ liệu | Bảng/cột | Kiểu | Ý nghĩa |
  |---|---|---|---|
  | Tồn kho thực tế | `ton_kho.so_luong_ton_kho` | `INTEGER` | Số chiếc (hoặc kg) hiện có của một mặt hàng tại một kho |
  | Đang giữ chỗ | `ton_kho.so_luong_da_giu_cho` | `INTEGER` | Phần đã cam kết cho hóa đơn chờ thanh toán |
  | Tồn tối thiểu | `ton_kho.so_luong_toi_thieu` | `INTEGER` | Có cột nhưng **không có logic nào dùng để cảnh báo/đặt hàng** |
  | Số lượng nhập lô | `lo_hang.so_luong_nhap` | `INTEGER` | Số pallet nhập của lô mua |
  | Số lượng còn lại của lô | `lo_hang.so_luong_con_lai` | `INTEGER` | Chỉ giảm ở một số bước (xem dưới) |
  | Số pallet xử lý theo đợt | `phieu_tai_che.so_luong_da_xu_ly` | `INTEGER` | Số pallet của lô đưa vào phân loại trong đợt |
  | Kết quả phân loại | `chi_tiet_tai_che.so_luong` | `INTEGER` | Bán lại / tháo dỡ / kg phế liệu |
  | Kết quả tháo dỡ | `phieu_thao_do.so_thanh_nguyen`, `so_cuc_vuong`, `so_thanh_can_xu_ly`, `so_cuc_can_xu_ly`, `kg_phe_lieu` | `INTEGER` | Số chiếc hoặc kg |
  | Kết quả xử lý thêm | `phieu_xu_ly_them.so_dat_lai`, `so_phe_lieu`, `kg_phe_lieu` | `INTEGER` | Số chiếc hoặc kg |
  | Sản lượng lắp ráp | `phieu_dong_pallet.so_luong_san_xuat` | `INTEGER` | Số pallet thành phẩm khai báo |
  | Dòng hóa đơn | `chi_tiet_hoa_don.so_luong` | `INTEGER` | Số chiếc hoặc kg |
  | Dòng phiếu kho | `chi_tiet_phieu_kho.so_luong` | `INTEGER` | Số chiếc hoặc kg |
  | Sổ khách | `ton_kho_khach.so_luong` | `INTEGER` | Số chiếc/kg hàng của khách, tách khỏi `ton_kho` |

  Nguồn: `V1__initial_schema.sql` dòng 220–249, 260–280; `V4` dòng 81–82; `V5` dòng 10–15; `V6` dòng 10–12; `V7` dòng 23–56; `V8` dòng 3–11; `V9` dòng 14–19.

  ### Lô hàng có phải là chiều tồn kho?

  **Không.** Tồn kho có UNIQUE `(mat_hang_id, kho_id)` (`V1` dòng 229). Không có bảng tồn theo lô, không có cột `lo_id` trong `ton_kho`. `chi_tiet_phieu_kho.lo_id` là tùy chọn và chỉ để ghi nguồn lô trên chứng từ.

  `lo_hang.so_luong_con_lai` **không được cập nhật nhất quán** trên mọi đường xuất:

  - Giảm khi duyệt phiếu phân loại: `lo.setSoLuongConLai(lo.getSoLuongConLai() - n)` (`PhieuTaiCheServiceImpl.duyetPhanLoai`, dòng 280–282).
  - Giảm khi lập phiếu xuất kho thủ công có chọn lô: `PhieuXuatServiceImpl.ghiChiTietVaTruTon`, dòng 246–255.
  - **Không giảm** khi bán hàng qua hóa đơn: dòng phiếu xuất do hóa đơn tạo không gán `lo_id` (`HoaDonServiceImpl.taoPhieuXuatKho`, dòng 522–536).
  - **Không giảm** khi tháo dỡ nội bộ, xử lý thêm, đóng pallet, bán phế liệu, dịch vụ khách.
  - Bị đặt về 0 khi hủy phiếu nhập thủ công (`PhieuNhapServiceImpl.capNhatPhieuNhap`, dòng 192–200) và bị xóa cùng dòng phiếu khi sửa phiếu nhập (`dòng 210–213`).

  Vì vậy `so_luong_con_lai` chỉ đáng tin cho luồng mua → phân loại; sau khi bán qua hóa đơn, số còn lại của lô không phản ánh tồn thực.

  ### Đơn vị tính và kích thước

  - `mat_hang.don_vi_tinh` chỉ nhận `cai` hoặc `kg` (`V4` dòng 10–11). Ràng buộc `chk_mathang_phelieu_kg`: mặt hàng loại `phe_lieu` bắt buộc `kg`, mọi loại khác bắt buộc `cai` (`V4` dòng 13–15).
  - Không có đơn vị `m3`, `m2`, `kg` cho pallet/linh kiện, không có cột quy đổi.
  - `mat_hang.kich_thuoc_dai/rong/cao` là `INTEGER`, có CHECK `> 0` (`V1` dòng 203–205); `tai_trong_tinh`, `tai_trong_dong` là `NUMERIC`. Các trường này chỉ được lưu, hiển thị và in ra chuỗi kích thước (`MatHangServiceImpl` dòng 141–145; `TonKhoServiceImpl` dòng 179–180; `HoaDonServiceImpl` dòng 601–602). **Không có công thức nào dùng kích thước hoặc tải trọng để tính số lượng, khối lượng, giá hoặc tồn.**
  - Không có trường `trong_luong`/`the_tich` trên `mat_hang` hay `lo_hang`.

  ### Các điểm "không có trong code"

  - Không có tồn kho theo lô, theo serial, theo hạn sử dụng, theo vị trí làm chiều tồn (vị trí chỉ là thuộc tính của dòng `ton_kho`).
  - Không có quy đổi pallet ↔ thanh/cục/kg, pallet ↔ m³, kg ↔ chiếc.
  - Không có số lượng thập phân; mọi số lượng là `INTEGER`.
  - Không có đơn vị tính thứ hai/đơn vị quy đổi cho cùng một mặt hàng.

  ---

  ## 4.2. Câu hỏi 2 — Giá mua lô lưu ở đâu, có theo dõi giá vốn theo lô không?

  **Kết luận ngắn: giá mua lô nằm ở `lo_hang.don_gia_nhap`. Giá này chỉ được ghi khi nhận hàng theo đơn mua; nhập kho thủ công để trống. Code chỉ dùng giá này để tính một cột trong báo cáo tái chế (`so_luong_nhap × don_gia_nhap`), không dùng để tính giá vốn khi bán, khi tháo dỡ hay khi lắp ráp.**

  ### Nơi lưu giá

  | Dữ liệu | Bảng/cột | Ghi chú |
  |---|---|---|
  | Đơn giá dòng đơn mua | `chi_tiet_don_mua.don_gia` | Giá do người lập đơn nhập |
  | Đơn giá nhập của lô | `lo_hang.don_gia_nhap` | `NUMERIC(18,2)`, cho phép NULL, CHECK `>= 0` (`V4` dòng 81–82) |
  | Tổng tiền đơn mua | `don_mua.tong_tien` | Tính từ số lượng đặt × đơn giá (`DonMuaServiceImpl.tinhTong`, dòng 363–367) |
  | Đơn giá bán | `mat_hang.don_gia_ban` | Dùng cho hóa đơn bán, không liên quan giá mua |
  | Giá trên hóa đơn | `chi_tiet_hoa_don.don_gia` | Giá bán tại thời điểm lập |

  ### Đường ghi giá lô

  - **Chỉ một đường ghi:** `DonMuaServiceImpl.nhanHang` tạo `LoHang` với `.donGiaNhap(dong.getDonGia())` (dòng 299–309), lấy từ `chi_tiet_don_mua.don_gia`.
  - **Nhập kho thủ công không ghi giá:** `PhieuNhapServiceImpl.ghiChiTietVaCapNhatTon` tạo `LoHang` nhưng không set `donGiaNhap` (dòng 234–244) → `don_gia_nhap = NULL`.
  - Không có màn hình sửa giá lô sau khi nhập. Không có service `setDonGiaNhap` nào khác (grep toàn backend chỉ có 2 chỗ đọc/ghi: `DonMuaServiceImpl` và `BaoCaoTaiCheService`).

  ### Cách giá lô được dùng

  `BaoCaoTaiCheService.theoLo` (dòng 96–119) trả về:

  - `tong_von = lo_hang.so_luong_nhap * COALESCE(lo_hang.don_gia_nhap, 0)` (dòng 101–102).
  - `so_pallet_da_xu_ly = so_luong_nhap − so_luong_con_lai` (dòng 113).
  - Các tỷ lệ `ban_lai`, `thao_do`, `loai_bo` trên `so_luong_nhap` (dòng 114–116).

  Đây là **báo cáo**, không phải bút toán. `tong_von` tính trên toàn bộ số nhập của lô, không phải phần đã tiêu thụ; không có giá vốn của phần đã bán/tháo dỡ.

  ### Các điểm "không có trong code"

  - Không có giá vốn hàng bán (COGS) khi xuất bán pallet, linh kiện, phế liệu.
  - Không có giá thành pallet tái chế; phiếu đóng pallet không cộng giá trị vật tư, nhân công, điện nước.
  - Không có phân bổ chi phí vận chuyển, bốc xếp, thuế nhập khẩu vào giá lô.
  - Không có giá vốn bình quân, FIFO, LIFO, giá đích danh.
  - Không có bút toán kho/giá vốn, không có sổ cái, không có kỳ kế toán.
  - Không có hóa đơn mua để đối chiếu giá lô với giá nhà cung cấp.
  - Không có đánh giá lại giá trị tồn kho, không có dự phòng giảm giá hàng tồn.

  ---

  ## 4.3. Câu hỏi 3 — Khi tháo dỡ, số lượng tốt/hỏng/phế liệu nhập tay hay theo tỷ lệ? Công thức?

  **Kết luận ngắn: tất cả số lượng đầu ra đều nhập tay, không có tỷ lệ, không có công thức quy đổi từ số pallet. Các tỷ lệ chỉ xuất hiện trong báo cáo để thống kê, không quay lại điều khiển nghiệp vụ.**

  ### Nơi nhập tay

  | Bước | Trường nhập tay | Ràng buộc | Nguồn |
  |---|---|---|---|
  | Phân loại lô | `so_luong_da_xu_ly`, `so_ban_lai`, `so_thao_do`, `so_loai_bo`, `kg_phe_lieu_loai_bo` | `so_ban_lai + so_thao_do + so_loai_bo = so_luong_da_xu_ly`; nếu `so_loai_bo > 0` thì `kg > 0`; nếu `so_loai_bo = 0` thì `kg = 0` | `QuyTacTaiChe.kiemTraPhanLoai`, dòng 21–39 |
  | Tháo dỡ nội bộ | `so_pallet_thao_do`, `so_thanh_nguyen`, `so_cuc_vuong`, `so_thanh_can_xu_ly`, `so_cuc_can_xu_ly`, `kg_phe_lieu` | `so_pallet > 0`; các số `>= 0`; tổng 5 kết quả `> 0` | `QuyTacTaiChe.kiemTraThaoDo`, dòng 46–58 |
  | Xử lý thêm | `so_dat_lai`, `so_phe_lieu`, `kg_phe_lieu`, `ly_do` | `so_dat_lai + so_phe_lieu > 0`; `so_phe_lieu > 0` ⇒ `kg > 0`; `kg > 0` ⇒ `so_phe_lieu > 0`; lý do bắt buộc | `QuyTacTaiChe.kiemTraXuLyThem`, dòng 79–96 |
  | Dịch vụ khách | `so_pallet_thao_do`, 4 loại linh kiện, `kg_phe_lieu` | `so_pallet_thao_do <= so_pallet_nhan`; tổng kết quả `> 0` | `QuyTacDichVuThaoDo.kiemTraKetQua`, dòng 22–30 |

  Không có ràng buộc nào về **giới hạn trên** của số thanh/cục/kg so với số pallet tháo dỡ. Ví dụ: tháo 1 pallet vẫn có thể nhập 1.000 thanh nguyên nếu người dùng nhập như vậy; hệ thống chỉ kiểm tra `>= 0` và tổng `> 0`.

  ### Công thức duy nhất có tính toán

  Chỉ trong báo cáo `BaoCaoTaiCheService.hieuSuat` (dòng 69–79):

  - `thanhNguyenTrenPallet = thanhNguyen / palletDaThaoDo` (làm tròn 2, HALF_UP).
  - `cucVuongTrenPallet = cucVuong / palletDaThaoDo`.
  - `kgPheLieuTrenPallet = kgPheLieu / palletDaThaoDo`.
  - `tyLeCanXuLy = (thanhCanXuLy + cucCanXuLy) / (thanhNguyen + cucVuong + thanhCanXuLy + cucCanXuLy) × 100`.
  - Tỷ lệ phân loại: `banLai / tong`, `thaoDo / tong`, `loaiBo / tong` × 100 (dòng 65–67).
  - Nếu mẫu số = 0 thì trả 0 (`tyLe`, `chia`, dòng 174–181).

  Các tỷ lệ này **chỉ để đọc**, không được lưu, không dùng để tính kết quả của phiếu sau, không dùng để kiểm tra định mức tháo dỡ.

  ### Hệ quả nghiệp vụ

  - Không có "hệ số thu hồi" chuẩn theo loại pallet, theo kích thước, theo chất liệu.
  - Không có tự động tính số thanh/cục từ số pallet.
  - Không có kiểm tra chéo giữa tổng khối lượng đầu ra và khối lượng pallet đầu vào.
  - Không có tách "tốt/hỏng" theo mức độ; chỉ có 4 loại linh kiện + phế liệu, tất cả do người dùng chọn số lượng.
  - Pallet hỏng hoàn toàn phải được đưa vào nhóm `loai_bo` ngay từ bước phân loại, vì bước tháo dỡ bắt buộc có ít nhất một kết quả (`QuyTacTaiChe.kiemTraThaoDo`).

  ---

  ## 4.4. Câu hỏi 4 — Khi lắp ráp, linh kiện/vật tư mới được trừ như thế nào? Xử lý thiếu ra sao?

  ### Nhu cầu và số thực tế

  - Định mức (BOM) lưu số lượng vật tư cho **một** pallet: `chi_tiet_dinh_muc.so_luong` (`V7` dòng 15–21).
  - Khi lập phiếu đóng pallet, nhu cầu từng vật tư:
    `nhuCau = dinhMucMotPallet × soLuongSanXuat`
    (`QuyTacDongPallet.tinhNhuCau`, dòng 11–21; dùng `Math.multiplyExact`, tràn số thì báo "Số lượng sản xuất quá lớn").
  - Số thực tế mặc định bằng đúng nhu cầu (`PhieuDongPalletServiceImpl.dungDong`, dòng 330–335).
  - Người dùng được ghi đè số thực tế. Nếu `soLuongThucTe ≠ soLuongDinhMuc` thì **bắt buộc nhập `lyDoChenhLech`** (`QuyTacDongPallet.kiemTraDong`, dòng 27–36; DB `ck_chenh_lech_co_ly_do`, `V7` dòng 54–55).
  - Được phép **thêm vật tư ngoài định mức**: dòng mới có `soLuongDinhMuc = 0`, `soLuongThucTe > 0`, cũng phải có lý do (`dungDong`, dòng 344–354). Vật tư ngoài định mức vẫn phải là `linh_kien`, không phải `LK-TX-11`/`LK-CX-12`, không trùng thành phẩm (`DinhMucServiceImpl.kiemTraVatTu`, dòng 135–147).
  - Không được gửi trùng một vật tư hai dòng (`dungDong`, dòng 339–343).

  ### Kiểm tra tồn khi lưu phiếu

  `kiemTraKhaDung` (dòng 373–389) tính cho từng vật tư:

  `khaDung = ton_kho.so_luong_ton_kho − ton_kho.so_luong_da_giu_cho − Σ(soLuongThucTe của cùng vật tư trong các phiếu đóng pallet nháp khác)`

  Nếu `soLuongThucTe > khaDung` thì chặn, báo số tồn/đã giữ/phiếu nháp khác đang giữ. Đây là kiểm tra tại thời điểm lưu; **không đặt `so_luong_da_giu_cho`** cho phiếu đóng pallet, nên hai phiếu nháp vẫn có thể cùng nhìn thấy một lượng tồn nếu được tạo trước khi phiếu kia lưu.

  ### Khi duyệt

  `PhieuDongPalletServiceImpl.duyet` (dòng 256–308):

  1. Kiểm tra phiếu `nhap`.
  2. Kiểm tra tổng `soLuongThucTe` của tất cả dòng `> 0` (`QuyTacDongPallet.kiemTraCoVatTu`).
  3. Tạo phiếu xuất kho hệ thống `PX-...`, `da_hoan_thanh`, `la_he_thong = true`.
  4. Với từng dòng có `soLuongThucTe > 0`, gọi `TonKhoNghiepVu.tru` để trừ tồn. Hàm `tru` kiểm tra lại `ton − da_giu_cho >= soLuong`; nếu thiếu, ném lỗi và **rollback toàn bộ giao dịch** (`TonKhoNghiepVu.tru`, dòng 39–53).
  5. Tạo phiếu nhập kho hệ thống `PN-...`, `da_hoan_thanh`, `la_he_thong = true`.
  6. Cộng tồn thành phẩm đúng `soLuongSanXuat`, **không** cộng theo tổng vật tư.
  7. Chuyển phiếu `da_duyet`, lưu người/thời gian duyệt.

  ### Không có trong code

  - Không trừ theo lô, không chọn lô khi lắp ráp, không ghi `lo_id` cho dòng xuất vật tư.
  - Không có ưu tiên "dùng hàng thu hồi trước, hàng mới sau" hoặc ngược lại. Mọi `linh_kien` đều bị trừ như nhau.
  - Không có thay thế vật tư (substitution), không có tỷ lệ hao hụt, không có backflush tự động.
  - Không có sản phẩm phụ/phế liệu sinh ra từ lắp ráp.
  - Không có giá thành thành phẩm, không có giá trị vật tư xuất.
  - Không có kiểm tra chất lượng thành phẩm, không có lô thành phẩm, không có truy xuất ngược linh kiện đã dùng.
  - Không có đặt trước tồn cho sản xuất; chỉ có kiểm tra "phiếu nháp khác đang giữ" khi lưu.
  - Không có xử lý thiếu một phần (partial build); thiếu bất kỳ vật tư nào thì không duyệt được.

  ---

  ## 4.5. Câu hỏi 5 — Pallet khách gửi có vào tồn công ty không? Ai sở hữu linh kiện/phế liệu?

  **Kết luận ngắn: pallet khách gửi không vào `ton_kho` công ty. Chúng nằm trong sổ `ton_kho_khach` theo `(khách hàng, mặt hàng, kho)`. Linh kiện và phế liệu sau tháo dỡ cũng nằm trong sổ khách; chỉ khi công ty mua lại phế liệu thì kg phế liệu mới chuyển sang `ton_kho` công ty. Không có mua lại linh kiện, không có chuyển quyền sở hữu linh kiện.**

  ### Sổ khách

  - Bảng `ton_kho_khach` có UNIQUE `(khach_hang_id, mat_hang_id, kho_id)`, `so_luong >= 0` (`V8` dòng 3–11).
  - `TonKhoKhachNghiepVu.cong`/`tru` chỉ thao tác trên sổ này, không gọi `TonKhoNghiepVu` (`service/TonKhoKhachNghiepVu.java`).
  - Tiếp nhận: cộng `soPalletNhan` vào `PL-CTD-06` của khách (`PhieuDichVuThaoDoServiceImpl.tiepNhan`, dòng 115–116).
  - Duyệt kết quả: trừ `PL-CTD-06` đúng `soPalletThaoDo`; cộng `LK-TN-09`, `LK-CV-10`, `LK-TX-11`, `LK-CX-12`, `PE-GO-15` vào sổ khách (`duyet`, dòng 164–170).
  - Trả hàng: trừ khỏi sổ khách phần pallet chưa tháo, toàn bộ linh kiện đã ghi nhận và phần phế liệu chưa mua (`traHang`, dòng 184–209). Hàng trả lại không đi vào tồn công ty.
  - Hủy trước duyệt: trừ lại toàn bộ `soPalletNhan` khỏi sổ khách (`huy`, dòng 211–234).

  ### Mua lại phế liệu

  `DichVuThaoDoTaiChinhService.muaLaiPheLieu` (dòng 120–190):

  1. Chỉ khi phiếu `da_thao_do` (chưa trả hàng).
  2. `soKg <= kgPheLieu − kgPheLieuDaMua`.
  3. Trừ kg khỏi sổ khách: `tonKhoKhachNghiepVu.tru(...)`.
  4. Cộng kg vào tồn công ty: `tonKhoNghiepVu.cong(...)`.
  5. Tạo phiếu nhập kho hệ thống `PN-...`, `da_hoan_thanh`.
  6. Tạo hóa đơn **chi** `chi_mua_phe_lieu_khach`, trạng thái `da_thanh_toan` ngay, VAT = 0, tổng = `soKg × donGiaKg`.
  7. Tạo bản ghi `thanh_toan` và `mua_lai_phe_lieu_khach`.
  8. Cộng dồn `kgPheLieuDaMua`.

  ### Quyền sở hữu theo code

  - Không có cột `chu_so_huu`/`so_huu` trên `ton_kho`, `ton_kho_khach`, `lo_hang`, `phieu_dich_vu_thao_do`.
  - Việc "của khách" được suy ra từ việc nằm trong `ton_kho_khach`; việc "của công ty" suy ra từ `ton_kho`.
  - Linh kiện trong sổ khách **không thể** chuyển sang công ty bằng bất kỳ chức năng nào; chỉ phế liệu có chức năng mua lại.
  - Không có bán linh kiện của khách, không có phí lưu kho, không có xử lý hàng không đến lấy, không có chuyển quyền sở hữu khi khách bỏ hàng.
  - Không có định giá sổ khách, không có hóa đơn cho phần linh kiện khách nhận lại.

  ### Các điểm "không có trong code"

  - Không có tồn kho công ty cho pallet khách gửi.
  - Không có mua lại pallet nguyên trạng/linh kiện của khách.
  - Không có hợp đồng gửi giữ, không có biên bản bàn giao, không có thời hạn lưu kho.
  - Không có tách chi phí dịch vụ theo pallet tháo dỡ thực tế so với pallet nhận; hóa đơn dịch vụ chỉ tính `soPalletThaoDo × donGiaDichVu`.
  - Không có xử lý phế liệu thuộc công ty hay thuộc khách ngoài quy ước sổ `ton_kho_khach`.

  ---

  ## 4.6. Câu hỏi 6 — Tồn kho có thể âm không? Có cơ chế điều chỉnh tồn được duyệt không?

  ### Tồn âm

  **Không.** Có ba lớp chặn:

  1. DB: `ton_kho.so_luong_ton_kho >= 0`, `so_luong_da_giu_cho >= 0`, và `so_luong_ton_kho >= so_luong_da_giu_cho` (`V1` dòng 225–230).
  2. Service: `TonKhoNghiepVu.tru` tính `khaDung = ton − da_giu_cho`, nếu `khaDung < soLuong` thì ném lỗi; không có nhánh nào cho phép trừ quá (`TonKhoNghiepVu.java`, dòng 39–53).
  3. Các service khác dùng `tru` hoặc kiểm tra `ton − da_giu_cho` trước khi trừ (`HoaDonServiceImpl.apDungTonKho` dòng 497–511; `BanPheLieuService.ban` dòng 97–103; `PhieuDongPalletServiceImpl` dòng 281, 374–389).

  Sổ khách cũng không âm: `ton_kho_khach.so_luong >= 0` (`V8` dòng 8) và `TonKhoKhachNghiepVu.tru` kiểm tra trước khi trừ.

  ### Điều chỉnh tồn

  **Cơ chế duy nhất tạo chênh lệch có chứng từ là kiểm kê.** Không có API "điều chỉnh tồn" độc lập, không có phiếu điều chỉnh do người dùng tự lập.

  Quy trình kiểm kê trong code:

  1. `POST /api/v1/kiem-ke/phien` với `TaoPhienKiemKeRequest { khoId, nguoiTaoId, ghiChu }` (`KiemKeController` dòng 26–33; DTO không có validation). `nguoiTaoId` do client gửi, **không lấy từ người đăng nhập** (`KiemKeServiceImpl.taoPhienKiemKe`, dòng 44–50).
  2. Mỗi kho chỉ được có một phiên `dang_dien_ra` (`dòng 52–55`).
  3. Snapshot tồn kho: lấy toàn bộ dòng `ton_kho` của kho (`tonKhoRepository.findByKho_Id`), **bỏ qua mặt hàng loại `cho_tai_che`** (`dòng 68–72`). Không lấy `ton_kho_khach`. Mỗi dòng tạo `ChiTietKiemKe` với `tonHeThong = soLuongTonKho`, `tonThucTe = null`, `trangThai = cho_kiem`.
  4. Nhập `tonThucTe` cho từng dòng: `PUT /api/v1/kiem-ke/chi-tiet/{id}`. Không cho sửa nếu phiên đã hoàn tất; `tonThucTe >= 0`; nếu bằng `tonHeThong` thì `khop`, khác thì `lech`; để trống thì `cho_kiem` (`capNhatChiTiet`, dòng 145–174).
  5. Chốt: `POST /api/v1/kiem-ke/phien/{id}/hoan-tat?nguoiChotId=...`. `nguoiChotId` do client gửi. Hệ thống:
    - khóa phiên, kiểm tra còn `cho_kiem` thì chặn;
    - với từng dòng `lech`, khóa dòng `ton_kho` tương ứng;
    - nếu `tonThucTe < soLuongDaGiuCho` thì chặn (không được giảm dưới phần đã giữ chỗ);
    - đặt `ton_kho.so_luong_ton_kho = tonThucTe`;
    - nếu chưa có dòng `ton_kho` và `tonThucTe > 0` thì tạo dòng mới (trường hợp dòng kiểm kê không còn tồn);
    - tạo **một** phiếu kho `dieu_chinh_kiem_ke`, `da_hoan_thanh`, `la_he_thong = true`, `nguoiDuyet = nguoiChot`;
    - mỗi dòng lệch tạo một `ChiTietPhieuKho` với `soLuong = |chenhLech|`, ghi chú "Kiểm kê tăng/giảm";
    - chuyển phiên `da_hoan_tat`, lưu `nguoiChot`, `ngayHoanTat`.
    — `hoanTatKiemKe`, dòng 176–264.

  **Không có bước phê duyệt riêng cho phiếu điều chỉnh.** Phiếu điều chỉnh được tạo thẳng `da_hoan_thanh`; người chốt kiểm kê được ghi vào `nguoiDuyet`. Không có trạng thái `cho_duyet`, không có kiểm tra người chốt phải là GD/PGD, không có kiểm tra `nguoiChotId` có trùng người đang đăng nhập hay không.

  ### Các đường thay đổi tồn khác (không phải điều chỉnh)

  - **Nhập kho thủ công:** `POST /api/v1/phieu-nhap` cho phép client chọn `trangThai` `cho_xu_ly` hoặc `da_hoan_thanh`; nếu chọn `da_hoan_thanh`, tồn được cộng ngay, không qua duyệt (`PhieuNhapServiceImpl.taoPhieuNhap`, dòng 141–168; `ghiChiTietVaCapNhatTon`, dòng 229–259).
  - **Xuất kho thủ công:** tương tự, client chọn `da_hoan_thanh` thì trừ tồn ngay; có thể chọn lô và trừ `lo_hang.so_luong_con_lai` (`PhieuXuatServiceImpl.taoPhieuXuat`, dòng 141–174; `ghiChiTietVaTruTon`, dòng 241–255).
  - **Chuyển kho bằng xếp vị trí:** `PhieuNhapServiceImpl.xepViTri` cho phép chọn kho đích khác kho phiếu; hệ thống trừ kho nguồn, cộng kho đích, chuyển lô sang kho đích, đánh dấu `da_xep_vi_tri` (`dòng 268–347`). **Không tạo phiếu `chuyen_kho`**, không có duyệt, không kiểm tra quyền kho đích.
  - **Bán hàng/giữ chỗ:** `HoaDonServiceImpl.apDungTonKho` tăng/giảm `da_giu_cho`, trừ `ton_kho` khi xuất (`dòng 487–516`).
  - **Phiếu hệ thống của tái chế:** phân loại, tháo dỡ, xử lý thêm, đóng pallet, mua phế liệu khách đều tạo phiếu `da_hoan_thanh` và gọi trực tiếp `TonKhoNghiepVu`.

  ### Các điểm "không có trong code"

  - Không có phiếu điều chỉnh tồn do người dùng lập ngoài kiểm kê.
  - Không có duyệt nhiều cấp cho điều chỉnh/kiểm kê.
  - Không có đếm vòng (cycle count), không có kiểm kê theo vị trí/khu vực, không có kiểm kê định kỳ tự động.
  - Không có khóa sổ kho, không có đóng kỳ, không có chặn giao dịch sau ngày khóa.
  - Không có cảnh báo tồn tối thiểu, không có điểm đặt hàng lại.
  - Không có chặn hai phiên kiểm kê khác kho cùng lúc (chỉ chặn trong cùng kho).
  - Không có thêm dòng kiểm kê cho mặt hàng chưa có dòng `ton_kho`; snapshot chỉ lấy những gì đang có.

  ---

  ## 4.7. Câu hỏi 7 — Vai trò và quyền

  ### Vai trò seed

  | Mã | Tên | `cap_duyet` | Nguồn |
  |---|---|---|---|
  | `GD` | Giám đốc | 2 | `V2__insert_master_data.sql` dòng 4–9 |
  | `PGD` | Phó giám đốc | 2 | `V2` dòng 4–9 |
  | `NVKHO` | Nhân viên kho | 0 | `V2` dòng 4–9 |
  | `NVBANHANG` | Nhân viên bán hàng | 0 | `V2` dòng 4–9 |

  Mỗi nhân viên có **một** `vai_tro_id` và **một** `kho_id` (`NhanVien` entity). Quyền thực tế khi chạy:

  1. `@PreAuthorize("hasAnyRole(...)")` trên controller, dùng mã vai trò đưa vào JWT (`JwtAuthenticationFilter` dòng 46: `"ROLE_" + vaiTro`).
  2. `PhanQuyenKho.kiemTra(nv, kho)` cho phép `GD/PGD` mọi kho; vai trò khác chỉ kho được gán (`PhanQuyenKho.java` dòng 11–24). Nếu nhân viên không có `kho_id`, họ không thao tác được kho nào.

  ### Ma trận quyền theo controller (thực tế)

  | Module/API | GD | PGD | NVKHO | NVBANHANG | Nguồn |
  |---|---|---|---|---|---|
  | Đơn mua (`/don-mua`) | Có | Có | Có | Không | `DonMuaController` dòng 21; duyệt/hủy chỉ GD/PGD dòng 53, 65 |
  | Nhà cung cấp | Có | Có | Có | Không | `NhaCungCapController` dòng 18 |
  | Phiếu nhập/xuất kho | Có | Có | Có | Không | `PhieuNhapController` dòng 25; `PhieuXuatController` dòng 24 |
  | Tái chế — tạo/sửa/xem | Có | Có | Có | Không | `PhieuTaiCheController` dòng 19; `PhieuThaoDoController` dòng 19; `PhieuXuLyThemController` dòng 19; `PhieuDongPalletController` dòng 20 |
  | Tái chế — duyệt | Có | Có | Không | Không | các controller trên, method `duyet` |
  | Định mức — xem | Có | Có | Có | Không | `DinhMucController` dòng 18 |
  | Định mức — tạo/sửa/ngừng/kích hoạt | Có | Có | Không | Không | `DinhMucController` dòng 34–53 |
  | Kiểm kê | Có | Có | Có | Không | `KiemKeController` dòng 21; không có phân quyền theo method |
  | Dịch vụ tháo dỡ khách — tiếp nhận/kết quả/trả hàng/hủy | Có | Có | Có | Không | `PhieuDichVuThaoDoController` dòng 23 |
  | Dịch vụ khách — duyệt/lập hóa đơn/mua phế liệu | Có | Có | Không | Không | `PhieuDichVuThaoDoController` dòng 58, 74, 82 |
  | Bán hàng/hóa đơn | Có | Có | Không | Có | `HoaDonController` dòng 23; hủy chỉ GD/PGD dòng 64 |
  | Bán phế liệu | Có | Có | Không | Có | `BanPheLieuController` dòng 18 |
  | Báo giá | Có | Có | Không | Có | `BaoGiaBanHangController` dòng 22 |
  | Khách hàng | Có | Có | Không | Có | `KhachHangController` dòng 18 |
  | Tài chính — xem | Có | Có | Không | Có | `TaiChinhController` dòng 26 |
  | Tài chính — chi phí/mục tiêu | Có | Có | Không | Không | `TaiChinhController` dòng 62–98 |
  | Báo cáo tái chế | Có | Có | Không | Không | `BaoCaoTaiCheController` dòng 20 |
  | Nhân viên | Có | Có | Không | Không | `NhanVienController` dòng 25–52 |
  | Mặt hàng/tồn kho | Có | Có | Có | Có | `MatHangController` dòng 26; `TonKhoController` dòng 19 |
  | Upload file | Có | Có | Có | Có | `FileUploadController` dòng 32 |

  ### `vai_tro_quyen`, `phe_duyet`, `cap_duyet`

  - `vai_tro_quyen` có seed đầy đủ (`V2` dòng 12–36) nhưng **không được đọc** ở bất kỳ service/controller nào. Quyền thực tế là mã vai trò hard-code trong `@PreAuthorize`.
  - `phe_duyet` có entity/repository nhưng **không có service/controller/UI** nào tạo, duyệt hoặc đọc yêu cầu phê duyệt.
  - `cap_duyet` chỉ được đọc trong `ThongBaoServiceImpl` để chọn người nhận thông báo (`dòng 53–54`); không dùng để quyết định luồng duyệt.
  - Không có cấu hình quyền theo module/action, không có quyền theo bản ghi, không có phân tách nhiệm vụ (người lập ≠ người duyệt) ngoài việc controller yêu cầu GD/PGD cho một số action.

  ### Phạm vi kho — nơi nào có, nơi nào không

  `PhanQuyenKho` chỉ được gọi ở:

  | Service | Các method có kiểm tra | Ghi chú |
  |---|---|---|
  | `DonMuaServiceImpl` | tạo/sửa/xác nhận/nhận hàng (`dòng 139, 170, 174, 255`) | Có |
  | `PhieuTaiCheServiceImpl` | xem chi tiết, tạo, sửa/hủy (`dòng 76, 126, 329`) | Duyệt không gọi |
  | `PhieuThaoDoServiceImpl` | xem chi tiết, tạo, sửa/hủy (`dòng 73, 106, 297`) | Duyệt không gọi |
  | `PhieuXuLyThemServiceImpl` | xem chi tiết, tạo, sửa/hủy (`dòng 74, 111, 254`) | Duyệt không gọi |
  | `PhieuDongPalletServiceImpl` | xem chi tiết, dự kiến, tạo, sửa/hủy (`dòng 92, 116, 174, 410`) | Duyệt không gọi |
  | `PhieuDichVuThaoDoServiceImpl` | xem chi tiết, tiếp nhận, ghi kết quả, trả hàng, hủy (`dòng 73, 102, 132, 192, 222`) | Duyệt không gọi |
  | `DichVuThaoDoTaiChinhService` | lập hóa đơn, mua phế liệu (`dòng 68, 124`) | — |

  **Không gọi `PhanQuyenKho`:**

  - `HoaDonServiceImpl` (bán hàng): `xacDinhKho` chỉ lấy `khoId` client gửi hoặc kho của nhân viên (`dòng 618–622`). Nhân viên bán hàng có thể truyền `khoId` bất kỳ.
  - `BanPheLieuService`: tương tự (`xacDinhKho`, dòng 180–184).
  - `KiemKeServiceImpl`: tạo phiên với `khoId` và `nguoiTaoId` client gửi; chốt với `nguoiChotId` client gửi; không kiểm tra kho được gán.
  - `PhieuNhapServiceImpl` / `PhieuXuatServiceImpl`: tạo/sửa/xuất/nhập theo `khoId` client gửi; `xepViTri` chuyển sang kho đích bất kỳ.
  - `BaoGiaBanHangService`, `MatHangServiceImpl`, `TonKhoServiceImpl`, `NhaCungCapService`, `KhachHangService`, `TaiChinhService`: không giới hạn theo kho.

  **Hệ quả:** `NVKHO` có thể xem/tạo phiếu nhập/xuất/kiểm kê cho kho khác nếu truyền `khoId`; `NVBANHANG` có thể lập hóa đơn cho kho khác. Các action duyệt chỉ GD/PGD, mà GD/PGD luôn được `PhanQuyenKho` coi là quản lý, nên việc không kiểm tra lại kho ở bước duyệt **không tạo khác biệt thực tế** với vai trò hiện tại; nhưng bản thân code không kiểm tra.

  ### Các điểm "không có trong code"

  - Không có quản lý người dùng/ vai trò động; vai trò là cột trên nhân viên, không có bảng gán nhiều vai trò.
  - Không có kiểm tra `vai_tro_quyen` khi gọi API.
  - Không có phê duyệt theo cấp/ngưỡng tiền, không có `phe_duyet`.
  - Không có giới hạn dữ liệu theo người tạo (row-level security): `NVBANHANG` xem được mọi hóa đơn, `NVKHO` xem được mọi phiếu kho/kiểm kê nếu biết id hoặc bỏ trống `khoId`.
  - Không có audit log chi tiết thay đổi trường; `lich_su_hoa_don` chỉ ghi một số sự kiện của hóa đơn, các module khác không có lịch sử sửa.
  - Không có chặn tự duyệt: GD/PGD có thể tạo và duyệt cùng một phiếu; không có kiểm tra người lập khác người duyệt.
  - Không có chặn nhân viên kho tự chốt kiểm kê; `nguoiChotId` do client chọn, không gắn với tài khoản đang đăng nhập.

  ---

  ## 4.8. Ma trận tổng hợp 13 quy trình

  | # | Quy trình | Chứng từ chính | Đầu vào chính | Tác động tồn kho | Hóa đơn | Duyệt |
  |---|---|---|---|---|---|---|
  | 1 | Mua pallet gỗ cũ theo lô | `don_mua`, `phieu_kho` nhập, `lo_hang` | NCC, mặt hàng `cho_tai_che`, số lượng, đơn giá | `ton_kho +=`; tạo lô `cho_tai_che` có `don_gia_nhap` | Không | Xác nhận đơn: GD/PGD; nhận hàng: GD/PGD/NVKHO |
  | 2 | Mua pallet thường và vật tư mới | `don_mua` hoặc `phieu_kho` nhập thủ công, `lo_hang` | NCC/mặt hàng `pallet`/`linh_kien`, số lượng, đơn giá | `ton_kho +=`; tạo lô `ban_thanh_pham`; nhập thủ công giá lô NULL | Không | Đơn mua: GD/PGD; phiếu nhập thủ công: không duyệt |
  | 3 | Nhận hàng, kiểm tra, phân loại | `phieu_tai_che`, phiếu xuất/nhập hệ thống, `chi_tiet_tai_che` | Lô, số pallet xử lý, 3 nhóm bán lại/tháo dỡ/loại bỏ, kg phế liệu | `PL-CPL-05 -=`; `PL-DC-07`/`PL-CTD-06`/`PE-GO-15 +=`; giảm `lo_hang.so_luong_con_lai` | Không | GD/PGD |
  | 4 | Chuyển kho và kiểm kê | `phieu_kho` nhập/xuất, `phien_kiem_ke`, `phieu_kho dieu_chinh_kiem_ke` | Kho, vị trí, số lượng thực tế | Chuyển kho: trừ nguồn/cộng đích; kiểm kê: đặt tồn = thực tế | Không | Không có duyệt riêng; người chốt do client chọn |
  | 5 | Tháo dỡ pallet cũ nội bộ | `phieu_thao_do`, phiếu xuất/nhập hệ thống | Phiếu tái chế, số pallet, 4 loại linh kiện, kg phế liệu | `PL-CTD-06 -=`; `LK-*`/`PE-GO-15 +=` | Không | GD/PGD |
  | 6 | Phân loại linh kiện thu hồi | `phieu_xu_ly_them`, phiếu xuất/nhập hệ thống | Kho, loại nguồn thanh/cục, số dùng lại, số bỏ, kg, lý do | `LK-TX-11`/`LK-CX-12 -=`; `LK-TN-09`/`LK-CV-10`/`PE-GO-15 +=` | Không | GD/PGD |
  | 7 | Lắp ráp pallet tái chế | `dinh_muc`, `phieu_dong_pallet`, phiếu xuất/nhập hệ thống | Định mức, kho, số pallet sản xuất, số thực tế từng vật tư, lý do lệch | Trừ vật tư thực tế; cộng `PL-TC-08` đúng số sản xuất | Không | GD/PGD |
  | 8 | Bán pallet nguyên trạng và pallet mới | `hoa_don`, `chi_tiet_hoa_don`, `thanh_toan`, phiếu xuất | Khách hàng, mặt hàng, số lượng, chiết khấu, VAT, phí giao hàng | Giữ chỗ khi lập; trừ thật khi thanh toán/xuất | Hóa đơn bán | Không |
  | 9 | Bán pallet tái chế | Như quy trình 8 | Như quy trình 8, mặt hàng `PL-TC-08` | Như quy trình 8; không có lô thành phẩm | Hóa đơn bán | Không |
  | 10 | Bán phế liệu gỗ | `hoa_don` (`thu_ban_phe_lieu`), `thanh_toan`, phiếu xuất | Khách hàng, số kg, đơn giá/kg, VAT | Giữ chỗ/trừ `PE-GO-15` theo kg | Hóa đơn bán | Không |
  | 11 | Dịch vụ tháo dỡ cho khách | `phieu_dich_vu_thao_do`, `ton_kho_khach`, `mua_lai_phe_lieu_khach`, hóa đơn dịch vụ | Khách hàng, kho, số pallet nhận, đơn giá dịch vụ, kết quả tháo dỡ | Sổ khách biến động; chỉ kg phế liệu mua lại vào `ton_kho` công ty | Hóa đơn dịch vụ; hóa đơn chi mua phế liệu | Duyệt kết quả/lập hóa đơn/mua phế liệu: GD/PGD |
  | 12 | Hóa đơn và thanh toán nhà cung cấp | — | — | — | **không có trong code** | — |
  | 13 | Hóa đơn và thanh toán khách hàng | `hoa_don`, `chi_tiet_hoa_don`, `thanh_toan`, `lich_su_hoa_don` | Khách hàng, dòng hàng, VAT, chiết khấu, hạn thanh toán | Giữ chỗ/trừ theo dòng hàng (trừ `dich_vu`) | Hóa đơn bán/dịch vụ | Không duyệt hóa đơn; hủy chỉ GD/PGD |

  ---

  ## 4.9. Danh mục tổng hợp "không có trong code"

  ### Mua hàng và nhà cung cấp

  - Không có hóa đơn mua, không có công nợ phải trả, không có thanh toán nhà cung cấp.
  - Không có thanh toán một phần/nhiều lần cho nhà cung cấp; `thanh_toan` chỉ gắn `hoa_don_id`.
  - Không có hóa đơn GTGT đầu vào, không có thuế mua, không có chiết khấu mua, không có phí vận chuyển mua.
  - Không có trả hàng mua, giảm giá sau mua, debit note.
  - Không có phân bổ chi phí mua vào giá lô, không có giá vốn bình quân.
  - Không có đối chiếu công nợ, tuổi nợ, sao kê nhà cung cấp.
  - Không có hợp đồng mua/giá theo hợp đồng.

  ### Tồn kho

  - Không có tồn theo lô/serial/hạn sử dụng.
  - Không có tồn âm; không có cơ chế cho phép âm tạm thời.
  - Không có điều chỉnh tồn ngoài kiểm kê.
  - Không có duyệt điều chỉnh/kiểm kê.
  - Không có chuyển kho có phiếu `chuyen_kho` và duyệt; chuyển kho chỉ qua `xepViTri`.
  - Không có quy đổi m³/kg/chiếc, không có trọng lượng.
  - Không có tồn tối thiểu/điểm đặt hàng/cảnh báo thiếu.
  - Không có đóng kỳ/khóa sổ kho.
  - Không có kiểm kê vòng, kiểm kê theo vị trí, kiểm kê tự động.
  - Không có barcode/RFID.

  ### Sản xuất/tái chế

  - Không có lệnh sản xuất, công đoạn, máy móc, nhân công.
  - Không có giá thành sản xuất, giá thành đơn vị, WIP.
  - Không có tỷ lệ thu hồi/hao hụt tự động; tất cả đầu ra nhập tay.
  - Không có sản phẩm phụ/phế liệu tự sinh từ lắp ráp.
  - Không có thay thế vật tư, backflush, định mức nhiều phiên bản.
  - Không có lô thành phẩm, truy xuất linh kiện đã dùng, kiểm tra chất lượng.
  - Không có định mức tháo dỡ/dịch vụ; định mức chỉ dùng cho lắp ráp.

  ### Bán hàng và khách hàng

  - Không có thanh toán một phần/nhiều lần, không có công nợ phải thu, không có tuổi nợ/sao kê khách hàng.
  - Không có hoàn tiền, trả hàng bán, credit note, điều chỉnh hóa đơn sau thanh toán.
  - Không có đặt cọc/thanh toán trước.
  - Không có bảng giá, giá theo hợp đồng, chiết khấu theo khách/số lượng.
  - Không có phiếu giao hàng, biên bản giao nhận, vận đơn.
  - Không có hóa đơn điện tử, không sinh file PDF (`file_pdf` chỉ là cột).
  - Không có giá vốn/COGS/lãi gộp trên hóa đơn.
  - Không có chọn lô khi bán, không trừ `lo_hang.so_luong_con_lai` khi bán.
  - Không có giữ chỗ có thời hạn/giải phóng tự động.

  ### Dịch vụ khách

  - Không có chuyển quyền sở hữu pallet/linh kiện/phế liệu.
  - Không có mua lại linh kiện/pallet nguyên trạng của khách; chỉ mua phế liệu.
  - Không có phí lưu kho, phí xử lý hàng không lấy, xử lý hàng bỏ.
  - Không có định giá sổ khách, không có hóa đơn cho phần hàng khách nhận lại.
  - Không có hợp đồng dịch vụ, biên bản bàn giao, thời hạn lưu kho.

  ### Phê duyệt và phân quyền

  - Không có engine phê duyệt; `phe_duyet` không được dùng.
  - Không có duyệt nhiều cấp/ngưỡng tiền; `cap_duyet` chỉ dùng cho thông báo.
  - Không có kiểm tra `vai_tro_quyen`; quyền hard-code theo mã vai trò.
  - Không có row-level security: hóa đơn/phiếu kho/kiểm kê không giới hạn theo người tạo hay kho ở nhiều API.
  - Không có tách nhiệm vụ người lập/người duyệt.
  - Không có audit log đầy đủ thay đổi trường.

  ### Kế toán/báo cáo

  - Không có sổ cái, bút toán, kỳ kế toán, tài khoản kế toán.
  - Không có báo cáo giá trị tồn kho, giá vốn, lãi gộp theo lô/sản phẩm.
  - Không có báo cáo công nợ phải thu/phải trả.
  - Không có báo cáo thuế đầu vào; báo cáo thuế hiện tại chỉ dựa trên hóa đơn bán.
  - Không có đối chiếu ngân hàng, không có webhook thanh toán được dùng (bảng `ma_qr_thanh_toan`/`webhook_ngan_hang_log` tồn tại nhưng không có luồng tạo/xử lý trong code).

  ---

  *Hết tài liệu. Toàn bộ kết luận trên bám theo mã nguồn tại commit ghi ở đầu tài liệu; các chỗ ghi "không có trong code" là không tìm thấy xử lý tương ứng, không phải đánh giá nghiệp vụ thực tế.*
