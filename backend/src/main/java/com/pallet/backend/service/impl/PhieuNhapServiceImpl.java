package com.pallet.backend.service.impl;

import com.pallet.backend.dto.request.ChiTietPhieuNhapItemRequest;
import com.pallet.backend.dto.request.TaoPhieuNhapRequest;
import com.pallet.backend.dto.response.ChiTietPhieuKhoResponse;
import com.pallet.backend.dto.response.PhieuNhapResponse;
import com.pallet.backend.dto.response.ThongKePhieuNhapResponse;
import com.pallet.backend.entity.*;
import com.pallet.backend.repository.*;
import com.pallet.backend.repository.specification.PhieuKhoSpecifications;
import com.pallet.backend.service.PhieuNhapService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhieuNhapServiceImpl implements PhieuNhapService {

    private static final String LOAI_PHIEU = "nhap_kho";
    private static final String CHO_XU_LY = "cho_xu_ly";
    private static final String DA_HOAN_THANH = "da_hoan_thanh";
    private static final String DA_HUY = "da_huy";

    private final PhieuKhoRepository phieuKhoRepository;
    private final ChiTietPhieuKhoRepository chiTietPhieuKhoRepository;
    private final MatHangRepository matHangRepository;
    private final KhoRepository khoRepository;
    private final NhaCungCapRepository nccRepository;
    private final NhanVienRepository nhanVienRepository;
    private final LoHangRepository loHangRepository;
    private final TonKhoRepository tonKhoRepository;
    private final ViTriLuuTruRepository viTriLuuTruRepository;

    private static int orZero(Integer v) {
        return v != null ? v : 0;
    }

    private Specification<PhieuKho> taoDieuKienLoc(String keyword, String trangThai, LocalDateTime tuNgay, LocalDateTime denNgay, Long khoId) {
        Specification<PhieuKho> spec = Specification.where(PhieuKhoSpecifications.loaiPhieu(LOAI_PHIEU))
                .and(PhieuKhoSpecifications.khoId(khoId));

        if (keyword != null && !keyword.trim().isEmpty()) {
            spec = spec.and(PhieuKhoSpecifications.maPhieuChua(keyword));
        }
        if (trangThai != null && !trangThai.trim().isEmpty()) {
            spec = spec.and(PhieuKhoSpecifications.trangThai(trangThai));
        }
        if (tuNgay != null) {
            spec = spec.and(PhieuKhoSpecifications.ngayGioTu(tuNgay));
        }
        if (denNgay != null) {
            spec = spec.and(PhieuKhoSpecifications.ngayGioDen(denNgay));
        }
        return spec;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PhieuNhapResponse> layDanhSach(String keyword, String trangThai, LocalDateTime tuNgay, LocalDateTime denNgay, Long khoId, Pageable pageable) {
        Specification<PhieuKho> spec = taoDieuKienLoc(keyword, trangThai, tuNgay, denNgay, khoId);
        return phieuKhoRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    /**
     * Thống kê 4 thẻ của tab Lịch sử nhập kho (tính theo 1 kho):
     *  - Nhập kho trong ngày   : tổng SL phiếu nhập ĐÃ HOÀN THÀNH có ngày giờ là hôm nay.
     *  - Đang kiểm định        : tổng SL phiếu nhập đang CHỜ XỬ LÝ.
     *  - Tổng giá trị nhập tháng: tổng (SL x đơn giá bán) của phiếu nhập ĐÃ HOÀN THÀNH trong tháng hiện tại.
     *                            (schema chưa có đơn giá nhập nên dùng mat_hang.don_gia_ban, giống báo cáo tồn kho)
     *  - Sức chứa khả dụng     : % vị trí lưu trữ của kho chưa có hàng.
     */
    @Override
    @Transactional(readOnly = true)
    public ThongKePhieuNhapResponse layThongKe(Long khoId) {
        LocalDate homNay = LocalDate.now();
        LocalDateTime dauNgay = homNay.atStartOfDay();
        LocalDateTime dauNgayMai = dauNgay.plusDays(1);
        LocalDateTime dauThang = homNay.withDayOfMonth(1).atStartOfDay();
        LocalDateTime dauThangSau = dauThang.plusMonths(1);

        int nhapTrongNgay = orZero(chiTietPhieuKhoRepository.sumSoLuongTheoKhoVaThoiGian(
                khoId, LOAI_PHIEU, DA_HOAN_THANH, dauNgay, dauNgayMai));
        int dangKiemDinh = orZero(chiTietPhieuKhoRepository.sumSoLuongTheoKho(khoId, LOAI_PHIEU, CHO_XU_LY));

        BigDecimal giaTriThang = chiTietPhieuKhoRepository.sumGiaTriTheoKhoVaThoiGian(
                khoId, LOAI_PHIEU, DA_HOAN_THANH, dauThang, dauThangSau);
        long tongGiaTriThang = giaTriThang != null ? giaTriThang.setScale(0, RoundingMode.HALF_UP).longValue() : 0L;

        int tongSlot = (int) viTriLuuTruRepository.countByKho_Id(khoId);
        int slotDaDung = orZero(tonKhoRepository.countViTriDaSuDungTheoKho(khoId));
        int slotTrong = Math.max(0, tongSlot - slotDaDung);
        double phanTram = tongSlot == 0 ? 0.0 : Math.round((double) slotTrong / tongSlot * 1000.0) / 10.0;

        return ThongKePhieuNhapResponse.builder()
                .nhapKhoTrongNgay(nhapTrongNgay)
                .dangKiemDinh(dangKiemDinh)
                .tongGiaTriNhapThang(tongGiaTriThang)
                .sucChuaKhaDungPhanTram(phanTram)
                .tongSoSlot(tongSlot)
                .soSlotTrong(slotTrong)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PhieuNhapResponse layChiTiet(Long id) {
        PhieuKho pk = phieuKhoRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu"));
        return mapToResponse(pk);
    }

    private void kiemTraChiTiet(List<ChiTietPhieuNhapItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("Phiếu nhập cần ít nhất 1 mặt hàng");
        }
        for (ChiTietPhieuNhapItemRequest it : items) {
            if (it.getMatHangId() == null || it.getSoLuong() == null || it.getSoLuong() <= 0) {
                throw new RuntimeException("Mỗi dòng hàng cần chọn mặt hàng và số lượng lớn hơn 0");
            }
        }
    }

    @Override
    @Transactional
    public PhieuNhapResponse taoPhieuNhap(TaoPhieuNhapRequest request) {
        if (request.getKhoId() == null) throw new RuntimeException("Thiếu thông tin kho");
        kiemTraChiTiet(request.getChiTiet());

        String trangThai = request.getTrangThai() == null ? CHO_XU_LY : request.getTrangThai();
        if (!CHO_XU_LY.equals(trangThai) && !DA_HOAN_THANH.equals(trangThai)) {
            throw new RuntimeException("Trạng thái phiếu nhập không hợp lệ: " + trangThai);
        }

        Kho kho = khoRepository.findById(request.getKhoId()).orElseThrow(() -> new RuntimeException("Không tìm thấy kho"));
        NhanVien nv = request.getNhanVienTiepNhanId() == null ? null
                : nhanVienRepository.findById(request.getNhanVienTiepNhanId()).orElse(null);
        NhaCungCap ncc = request.getNccId() == null ? null
                : nccRepository.findById(request.getNccId()).orElse(null);

        String maPhieu = phieuKhoRepository.getMaChungTu("PN", "PN-");

        PhieuKho pk = PhieuKho.builder()
                .maPhieu(maPhieu)
                .loaiPhieu(LOAI_PHIEU)
                .kho(kho)
                .nhanVienGiaoNhan(nv)
                .bienSoXeDoiTac(request.getBienSoXe())
                .ghiChu(request.getGhiChu())
                .trangThai(trangThai)
                .ngayGio(LocalDateTime.now())
                .build();
        pk = phieuKhoRepository.save(pk);

        ghiChiTietVaCapNhatTon(pk, kho, ncc, request.getChiTiet());
        return mapToResponse(pk);
    }

    /**
     * Sửa phiếu nhập. Chỉ phiếu đang CHỜ XỬ LÝ mới sửa được (chưa đụng tới tồn kho).
     *  - Giữ "Chờ xử lý" hoặc chuyển "Hoàn tất": thay toàn bộ thông tin + dòng hàng (hoàn tất thì cộng tồn kho).
     *  - Chuyển "Đã huỷ": chỉ đổi trạng thái, đóng các lô đã sinh khi tạo phiếu.
     */
    @Override
    @Transactional
    public PhieuNhapResponse capNhatPhieuNhap(Long id, TaoPhieuNhapRequest request) {
        PhieuKho pk = phieuKhoRepository.findById(id)
                .filter(p -> LOAI_PHIEU.equals(p.getLoaiPhieu()))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập"));
        if (!CHO_XU_LY.equals(pk.getTrangThai())) {
            throw new RuntimeException("Chỉ được sửa phiếu đang ở trạng thái Chờ xử lý");
        }

        String trangThaiMoi = request.getTrangThai() == null ? CHO_XU_LY : request.getTrangThai();
        if (!CHO_XU_LY.equals(trangThaiMoi) && !DA_HOAN_THANH.equals(trangThaiMoi) && !DA_HUY.equals(trangThaiMoi)) {
            throw new RuntimeException("Trạng thái phiếu nhập không hợp lệ: " + trangThaiMoi);
        }

        List<ChiTietPhieuKho> chiTietCu = chiTietPhieuKhoRepository.findByPhieuKho_Id(pk.getId());

        if (DA_HUY.equals(trangThaiMoi)) {
            for (ChiTietPhieuKho ct : chiTietCu) {
                LoHang lo = ct.getLo();
                if (lo != null) {
                    lo.setSoLuongConLai(0);
                    lo.setTrangThai("da_het");
                    loHangRepository.save(lo);
                }
            }
            pk.setTrangThai(DA_HUY);
            phieuKhoRepository.save(pk);
            return mapToResponse(pk);
        }

        kiemTraChiTiet(request.getChiTiet());
        NhaCungCap ncc = request.getNccId() == null ? null
                : nccRepository.findById(request.getNccId()).orElse(null);

        // Xoá dòng hàng + lô cũ (phiếu chưa hoàn thành nên chưa có tồn kho nào phải hoàn lại)
        List<LoHang> loCu = chiTietCu.stream().map(ChiTietPhieuKho::getLo)
                .filter(l -> l != null).distinct().toList();
        chiTietPhieuKhoRepository.deleteAll(chiTietCu);
        loHangRepository.deleteAll(loCu);

        pk.setBienSoXeDoiTac(request.getBienSoXe());
        pk.setGhiChu(request.getGhiChu());
        pk.setTrangThai(trangThaiMoi);
        pk = phieuKhoRepository.save(pk);

        ghiChiTietVaCapNhatTon(pk, pk.getKho(), ncc, request.getChiTiet());
        return mapToResponse(pk);
    }

    /** Lưu từng dòng hàng, sinh lô và (nếu phiếu hoàn tất) cộng tồn kho. */
    private void ghiChiTietVaCapNhatTon(PhieuKho pk, Kho kho, NhaCungCap ncc, List<ChiTietPhieuNhapItemRequest> items) {
        for (ChiTietPhieuNhapItemRequest item : items) {
            MatHang mh = matHangRepository.findById(item.getMatHangId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy mặt hàng ID " + item.getMatHangId()));

            String maLo = "LO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            LoHang lo = LoHang.builder()
                    .maLo(maLo)
                    .matHang(mh)
                    .kho(kho)
                    .ncc(ncc)
                    .soLuongNhap(item.getSoLuong())
                    .soLuongConLai(item.getSoLuong())
                    .ngayNhap(LocalDate.now())
                    .trangThai("con_hang")
                    .mucDichNhap("ban_thanh_pham")
                    .build();
            lo = loHangRepository.save(lo);

            ChiTietPhieuKho ct = ChiTietPhieuKho.builder()
                    .phieuKho(pk)
                    .matHang(mh)
                    .lo(lo)
                    .soLuong(item.getSoLuong())
                    .ghiChu(item.getGhiChu())
                    .build();
            chiTietPhieuKhoRepository.save(ct);

            if (DA_HOAN_THANH.equals(pk.getTrangThai())) {
                TonKho tk = tonKhoRepository.findByMatHang_IdAndKho_Id(mh.getId(), kho.getId())
                        .orElse(TonKho.builder()
                                .matHang(mh)
                                .kho(kho)
                                .soLuongTonKho(0)
                                .soLuongDaGiuCho(0)
                                .soLuongToiThieu(0)
                                .updatedAt(LocalDateTime.now())
                                .build());
                tk.setSoLuongTonKho(tk.getSoLuongTonKho() + item.getSoLuong());
                tonKhoRepository.save(tk);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] xuatExcel(String keyword, String trangThai, LocalDateTime tuNgay, LocalDateTime denNgay, Long khoId) {
        Specification<PhieuKho> spec = taoDieuKienLoc(keyword, trangThai, tuNgay, denNgay, khoId);
        List<PhieuKho> dsPhieu = phieuKhoRepository.findAll(spec);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Lịch sử nhập kho");
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Mã Phiếu", "Người Tiếp Nhận", "Ngày Nhập", "Số Lượng (Cái)", "Trạng Thái"};

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (PhieuKho pk : dsPhieu) {
                Row row = sheet.createRow(rowIdx++);
                PhieuNhapResponse dto = mapToResponse(pk);

                row.createCell(0).setCellValue(dto.getMaPhieu());
                row.createCell(1).setCellValue(dto.getTenNhanVienTiepNhan() != null ? dto.getTenNhanVienTiepNhan() : "");
                row.createCell(2).setCellValue(dto.getNgayNhapKho().toString());
                row.createCell(3).setCellValue(dto.getTongSoLuongPallet());
                row.createCell(4).setCellValue(dto.getTrangThai());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xuất Excel Lịch sử nhập kho", e);
        }
    }

    private PhieuNhapResponse mapToResponse(PhieuKho pk) {
        int tongSoLuong = 0;
        String tenNcc = "";
        Long nccId = null;

        List<ChiTietPhieuKho> dsChiTiet = chiTietPhieuKhoRepository.findByPhieuKho_Id(pk.getId());
        if (dsChiTiet != null && !dsChiTiet.isEmpty()) {
            tongSoLuong = dsChiTiet.stream().mapToInt(ChiTietPhieuKho::getSoLuong).sum();
            LoHang loDau = dsChiTiet.get(0).getLo();
            if (loDau != null && loDau.getNcc() != null) {
                tenNcc = loDau.getNcc().getTenNcc();
                nccId = loDau.getNcc().getId();
            }
        }

        List<ChiTietPhieuKhoResponse> chiTiet = dsChiTiet == null ? List.of() : dsChiTiet.stream()
                .map(ct -> ChiTietPhieuKhoResponse.builder()
                        .matHangId(ct.getMatHang().getId())
                        .maMatHang(ct.getMatHang().getMaMatHang())
                        .tenMatHang(ct.getMatHang().getTenMatHang())
                        .loId(ct.getLo() != null ? ct.getLo().getId() : null)
                        .soLuong(ct.getSoLuong())
                        .ghiChu(ct.getGhiChu())
                        .build())
                .toList();

        return PhieuNhapResponse.builder()
                .id(pk.getId())
                .maPhieu(pk.getMaPhieu())
                .nccId(nccId)
                .tenNhaCungCap(tenNcc)
                .tenNhanVienTiepNhan(pk.getNhanVienGiaoNhan() != null ? pk.getNhanVienGiaoNhan().getHoTen() : "")
                .ngayNhapKho(pk.getNgayGio())
                .tongSoLuongPallet(tongSoLuong)
                .trangThai(pk.getTrangThai())
                .ghiChu(pk.getGhiChu())
                .bienSoXe(pk.getBienSoXeDoiTac())
                .chiTiet(chiTiet)
                .build();
    }
}