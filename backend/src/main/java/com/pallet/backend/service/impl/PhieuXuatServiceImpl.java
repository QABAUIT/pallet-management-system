package com.pallet.backend.service.impl;

import com.pallet.backend.dto.request.ChiTietPhieuXuatItemRequest;
import com.pallet.backend.dto.request.TaoPhieuXuatRequest;
import com.pallet.backend.dto.response.ChiTietPhieuKhoResponse;
import com.pallet.backend.dto.response.PhieuXuatResponse;
import com.pallet.backend.dto.response.ThongKePhieuXuatResponse;
import com.pallet.backend.entity.*;
import com.pallet.backend.repository.*;
import com.pallet.backend.repository.specification.PhieuKhoSpecifications;
import com.pallet.backend.service.PhieuXuatService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhieuXuatServiceImpl implements PhieuXuatService {

    private static final String LOAI_PHIEU = "xuat_kho";
    private static final String CHO_XU_LY = "cho_xu_ly";
    private static final String DA_HOAN_THANH = "da_hoan_thanh";
    private static final String DA_HUY = "da_huy";

    /** Phiếu xuất còn "Chờ xử lý" quá số giờ này thì bị tính là TRỄ tiến độ (đổi ở đây nếu nhóm thống nhất SLA khác). */
    private static final int SLA_GIO = 24;

    private final PhieuKhoRepository phieuKhoRepository;
    private final ChiTietPhieuKhoRepository chiTietPhieuKhoRepository;
    private final MatHangRepository matHangRepository;
    private final KhoRepository khoRepository;
    private final NhanVienRepository nhanVienRepository;
    private final LoHangRepository loHangRepository;
    private final TonKhoRepository tonKhoRepository;

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
    public Page<PhieuXuatResponse> layDanhSach(String keyword, String trangThai, LocalDateTime tuNgay, LocalDateTime denNgay, Long khoId, Pageable pageable) {
        Specification<PhieuKho> spec = taoDieuKienLoc(keyword, trangThai, tuNgay, denNgay, khoId);
        return phieuKhoRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    /**
     * Thống kê 4 thẻ của tab Lịch sử xuất kho (tính theo 1 kho):
     *  - Tổng xuất hôm nay         : tổng SL phiếu xuất ĐÃ HOÀN THÀNH có ngày giờ là hôm nay.
     *  - Đang bốc dỡ & xếp xe      : tổng SL phiếu xuất đang CHỜ XỬ LÝ.
     *  - Xuất bảo dưỡng định kỳ    : tổng SL phiếu xuất loại "Bảo trì" ĐÃ HOÀN THÀNH trong tháng này.
     *  - Tỷ lệ xuất đúng tiến độ   : trong các phiếu xuất của tháng này (trừ phiếu huỷ), % phiếu KHÔNG bị trễ.
     *                                Phiếu trễ = còn "Chờ xử lý" quá SLA_GIO giờ. Chưa có phiếu nào -> trả null (FE hiện "—").
     *                                (schema chưa có hạn xuất / ngày hoàn thành nên chưa tính được trễ theo hạn thực tế)
     */
    @Override
    @Transactional(readOnly = true)
    public ThongKePhieuXuatResponse layThongKe(Long khoId) {
        LocalDateTime bayGio = LocalDateTime.now();
        LocalDate homNay = bayGio.toLocalDate();
        LocalDateTime dauNgay = homNay.atStartOfDay();
        LocalDateTime dauNgayMai = dauNgay.plusDays(1);
        LocalDateTime dauThang = homNay.withDayOfMonth(1).atStartOfDay();
        LocalDateTime dauThangSau = dauThang.plusMonths(1);

        int xuatHomNay = orZero(chiTietPhieuKhoRepository.sumSoLuongTheoKhoVaThoiGian(
                khoId, LOAI_PHIEU, DA_HOAN_THANH, dauNgay, dauNgayMai));
        int dangBocDo = orZero(chiTietPhieuKhoRepository.sumSoLuongTheoKho(khoId, LOAI_PHIEU, CHO_XU_LY));
        int xuatBaoDuong = orZero(chiTietPhieuKhoRepository.sumSoLuongTheoKhoThoiGianVaGhiChu(
                khoId, LOAI_PHIEU, DA_HOAN_THANH, dauThang, dauThangSau, "Loại xuất: Bảo trì%"));

        Specification<PhieuKho> phieuTrongThang = Specification.where(PhieuKhoSpecifications.loaiPhieu(LOAI_PHIEU))
                .and(PhieuKhoSpecifications.khoId(khoId))
                .and(PhieuKhoSpecifications.ngayGioTu(dauThang))
                .and(PhieuKhoSpecifications.ngayGioDen(dauThangSau.minusSeconds(1)));

        long tongPhieu = phieuKhoRepository.count(phieuTrongThang);
        long phieuHuy = phieuKhoRepository.count(phieuTrongThang.and(PhieuKhoSpecifications.trangThai(DA_HUY)));
        long phieuHieuLuc = tongPhieu - phieuHuy;
        long phieuTre = phieuKhoRepository.count(phieuTrongThang
                .and(PhieuKhoSpecifications.trangThai(CHO_XU_LY))
                .and(PhieuKhoSpecifications.ngayGioDen(bayGio.minusHours(SLA_GIO))));

        Double tyLe = phieuHieuLuc == 0 ? null
                : Math.round((phieuHieuLuc - phieuTre) * 1000.0 / phieuHieuLuc) / 10.0;

        return ThongKePhieuXuatResponse.builder()
                .tongXuatHomNay(xuatHomNay)
                .dangBocDoXepXe(dangBocDo)
                .xuatBaoDuongDinhKy(xuatBaoDuong)
                .tyLeXuatDungTienDo(tyLe)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PhieuXuatResponse layChiTiet(Long id) {
        PhieuKho pk = phieuKhoRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu"));
        return mapToResponse(pk);
    }

    private void kiemTraDauVao(TaoPhieuXuatRequest request) {
        if (request.getChiTiet() == null || request.getChiTiet().isEmpty()) {
            throw new RuntimeException("Phiếu xuất cần ít nhất 1 mặt hàng");
        }
        for (ChiTietPhieuXuatItemRequest it : request.getChiTiet()) {
            if (it.getMatHangId() == null || it.getSoLuong() == null || it.getSoLuong() <= 0) {
                throw new RuntimeException("Mỗi dòng hàng cần chọn mặt hàng và số lượng lớn hơn 0");
            }
        }
    }

    private String gopGhiChu(TaoPhieuXuatRequest request) {
        return "Loại xuất: " + request.getLoaiXuat() + " - Lý do: " + request.getLyDoXuat();
    }

    @Override
    @Transactional
    public PhieuXuatResponse taoPhieuXuat(TaoPhieuXuatRequest request) {
        if (request.getKhoId() == null) throw new RuntimeException("Thiếu thông tin kho");
        kiemTraDauVao(request);

        String trangThai = request.getTrangThai() == null ? CHO_XU_LY : request.getTrangThai();
        if (!CHO_XU_LY.equals(trangThai) && !DA_HOAN_THANH.equals(trangThai)) {
            throw new RuntimeException("Trạng thái phiếu xuất không hợp lệ: " + trangThai);
        }

        Kho kho = khoRepository.findById(request.getKhoId()).orElseThrow(() -> new RuntimeException("Không tìm thấy kho"));
        NhanVien nv = request.getNhanVienXuatId() == null ? null
                : nhanVienRepository.findById(request.getNhanVienXuatId()).orElse(null);

        String maPhieu = phieuKhoRepository.getMaChungTu("PX", "PX-");

        PhieuKho pk = PhieuKho.builder()
                .maPhieu(maPhieu)
                .loaiPhieu(LOAI_PHIEU)
                .kho(kho)
                .nhanVienGiaoNhan(nv)
                .bienSoXeDoiTac(request.getBienSoXe())
                .ghiChu(gopGhiChu(request))
                .trangThai(trangThai)
                .ngayGio(LocalDateTime.now())
                .build();
        pk = phieuKhoRepository.save(pk);

        ghiChiTietVaTruTon(pk, kho, request.getChiTiet());
        return mapToResponse(pk);
    }

    /**
     * Sửa phiếu xuất. Chỉ phiếu đang CHỜ XỬ LÝ mới sửa được (chưa trừ tồn kho).
     *  - Giữ "Chờ xử lý" hoặc chuyển "Hoàn thành": thay toàn bộ thông tin + dòng hàng (hoàn thành thì trừ tồn kho).
     *  - Chuyển "Đã huỷ": chỉ đổi trạng thái.
     */
    @Override
    @Transactional
    public PhieuXuatResponse capNhatPhieuXuat(Long id, TaoPhieuXuatRequest request) {
        PhieuKho pk = phieuKhoRepository.findById(id)
                .filter(p -> LOAI_PHIEU.equals(p.getLoaiPhieu()))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu xuất"));
        if (!CHO_XU_LY.equals(pk.getTrangThai())) {
            throw new RuntimeException("Chỉ được sửa phiếu đang ở trạng thái Chờ xử lý");
        }

        String trangThaiMoi = request.getTrangThai() == null ? CHO_XU_LY : request.getTrangThai();
        if (!CHO_XU_LY.equals(trangThaiMoi) && !DA_HOAN_THANH.equals(trangThaiMoi) && !DA_HUY.equals(trangThaiMoi)) {
            throw new RuntimeException("Trạng thái phiếu xuất không hợp lệ: " + trangThaiMoi);
        }

        if (DA_HUY.equals(trangThaiMoi)) {
            pk.setTrangThai(DA_HUY);
            phieuKhoRepository.save(pk);
            return mapToResponse(pk);
        }

        kiemTraDauVao(request);
        chiTietPhieuKhoRepository.deleteAll(chiTietPhieuKhoRepository.findByPhieuKho_Id(pk.getId()));

        pk.setBienSoXeDoiTac(request.getBienSoXe());
        pk.setGhiChu(gopGhiChu(request));
        pk.setTrangThai(trangThaiMoi);
        pk = phieuKhoRepository.save(pk);

        ghiChiTietVaTruTon(pk, pk.getKho(), request.getChiTiet());
        return mapToResponse(pk);
    }

    /** Lưu từng dòng hàng và (nếu phiếu hoàn thành) trừ tồn kho + trừ lô. */
    private void ghiChiTietVaTruTon(PhieuKho pk, Kho kho, List<ChiTietPhieuXuatItemRequest> items) {
        for (ChiTietPhieuXuatItemRequest item : items) {
            MatHang mh = matHangRepository.findById(item.getMatHangId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy mặt hàng ID " + item.getMatHangId()));
            LoHang lo = item.getLoId() != null ? loHangRepository.findById(item.getLoId()).orElse(null) : null;

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
                        .orElseThrow(() -> new RuntimeException("Sản phẩm chưa có trong kho này!"));

                if (tk.getSoLuongTonKho() < item.getSoLuong()) {
                    throw new RuntimeException("Tồn kho không đủ cho mã: " + mh.getMaMatHang());
                }
                tk.setSoLuongTonKho(tk.getSoLuongTonKho() - item.getSoLuong());
                tonKhoRepository.save(tk);

                if (lo != null) {
                    if (lo.getSoLuongConLai() < item.getSoLuong()) {
                        throw new RuntimeException("Số lượng trong lô " + lo.getMaLo() + " không đủ!");
                    }
                    lo.setSoLuongConLai(lo.getSoLuongConLai() - item.getSoLuong());
                    if (lo.getSoLuongConLai() == 0) {
                        lo.setTrangThai("da_het");
                    }
                    loHangRepository.save(lo);
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] xuatExcel(String keyword, String trangThai, LocalDateTime tuNgay, LocalDateTime denNgay, Long khoId) {
        Specification<PhieuKho> spec = taoDieuKienLoc(keyword, trangThai, tuNgay, denNgay, khoId);
        List<PhieuKho> dsPhieu = phieuKhoRepository.findAll(spec);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Lịch sử xuất kho");
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Mã Phiếu Xuất", "Mã NV Xuất", "Tên Nhân Viên", "Ngày Xuất", "Lý do / Ghi chú", "Số Lượng (Cái)", "Trạng Thái"};

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
                PhieuXuatResponse dto = mapToResponse(pk);

                row.createCell(0).setCellValue(dto.getMaPhieuXuat());
                row.createCell(1).setCellValue(dto.getMaNhanVienXuat());
                row.createCell(2).setCellValue(dto.getTenNhanVienXuat());
                row.createCell(3).setCellValue(dto.getNgayXuatKho().toString());
                row.createCell(4).setCellValue(dto.getLyDoXuat() != null ? dto.getLyDoXuat() : "");
                row.createCell(5).setCellValue(dto.getTongSoLuong());
                row.createCell(6).setCellValue(dto.getTrangThai());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi xuất Excel Lịch sử xuất kho", e);
        }
    }

    private PhieuXuatResponse mapToResponse(PhieuKho pk) {
        List<ChiTietPhieuKho> dsChiTiet = chiTietPhieuKhoRepository.findByPhieuKho_Id(pk.getId());
        int tongSoLuong = dsChiTiet.stream().mapToInt(ChiTietPhieuKho::getSoLuong).sum();

        // Tách Loại xuất và Lý do từ Ghi chú
        String loaiXuat = "Xuất bán";
        String lyDo = pk.getGhiChu();
        if (pk.getGhiChu() != null && pk.getGhiChu().contains(" - Lý do: ")) {
            String[] parts = pk.getGhiChu().split(" - Lý do: ", 2);
            loaiXuat = parts[0].replace("Loại xuất: ", "");
            lyDo = parts.length > 1 ? parts[1] : "";
        }

        List<ChiTietPhieuKhoResponse> chiTiet = dsChiTiet.stream()
                .map(ct -> ChiTietPhieuKhoResponse.builder()
                        .matHangId(ct.getMatHang().getId())
                        .maMatHang(ct.getMatHang().getMaMatHang())
                        .tenMatHang(ct.getMatHang().getTenMatHang())
                        .loId(ct.getLo() != null ? ct.getLo().getId() : null)
                        .soLuong(ct.getSoLuong())
                        .ghiChu(ct.getGhiChu())
                        .build())
                .toList();

        return PhieuXuatResponse.builder()
                .id(pk.getId())
                .maPhieuXuat(pk.getMaPhieu())
                .maNhanVienXuat(pk.getNhanVienGiaoNhan() != null ? pk.getNhanVienGiaoNhan().getMaNv() : "")
                .tenNhanVienXuat(pk.getNhanVienGiaoNhan() != null ? pk.getNhanVienGiaoNhan().getHoTen() : "")
                .ngayXuatKho(pk.getNgayGio())
                .loaiXuatKho(loaiXuat)
                .lyDoXuat(lyDo)
                .tongSoLuong(tongSoLuong)
                .trangThai(pk.getTrangThai())
                .bienSoXe(pk.getBienSoXeDoiTac())
                .chiTiet(chiTiet)
                .build();
    }
}