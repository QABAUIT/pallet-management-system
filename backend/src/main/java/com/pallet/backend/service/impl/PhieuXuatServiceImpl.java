package com.pallet.backend.service.impl;

import com.pallet.backend.dto.request.ChiTietPhieuXuatItemRequest;
import com.pallet.backend.dto.request.TaoPhieuXuatRequest;
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

    private final PhieuKhoRepository phieuKhoRepository;
    private final ChiTietPhieuKhoRepository chiTietPhieuKhoRepository;
    private final MatHangRepository matHangRepository;
    private final KhoRepository khoRepository;
    private final NhanVienRepository nhanVienRepository;
    private final LoHangRepository loHangRepository;
    private final TonKhoRepository tonKhoRepository;

    private Specification<PhieuKho> taoDieuKienLoc(String keyword, String trangThai, LocalDateTime tuNgay, LocalDateTime denNgay) {
        // Lọc đúng loại phiếu là xuat_kho
        Specification<PhieuKho> spec = Specification.where(PhieuKhoSpecifications.loaiPhieu("xuat_kho"));

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
    public Page<PhieuXuatResponse> layDanhSach(String keyword, String trangThai, LocalDateTime tuNgay, LocalDateTime denNgay, Pageable pageable) {
        Specification<PhieuKho> spec = taoDieuKienLoc(keyword, trangThai, tuNgay, denNgay);
        return phieuKhoRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ThongKePhieuXuatResponse layThongKe() {
        List<PhieuKho> dsPhieu = phieuKhoRepository.findAll(PhieuKhoSpecifications.loaiPhieu("xuat_kho"));
        
// Tính khung thời gian hôm nay
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

    // Lấy số lượng xuất hoàn thành hôm nay
    int xuatHomNay = chiTietPhieuKhoRepository.sumSoLuongByLoaiPhieuAndTrangThaiAndDateRange("xuat_kho", "da_hoan_thanh", startOfDay, endOfDay);
        int dangBocDo = 0;
        int xuatBaoDuong = 0;

        for (PhieuKho pk : dsPhieu) {
            int tongSoLuong = chiTietPhieuKhoRepository.findByPhieuKho_Id(pk.getId())
                    .stream().mapToInt(ChiTietPhieuKho::getSoLuong).sum();

            if (pk.getNgayGio().toLocalDate().isEqual(LocalDate.now()) && "da_hoan_thanh".equals(pk.getTrangThai())) {
                xuatHomNay += tongSoLuong;
            }
            if ("cho_xu_ly".equals(pk.getTrangThai())) {
                dangBocDo += tongSoLuong;
            }
            if (pk.getGhiChu() != null && pk.getGhiChu().contains("Bảo trì")) {
                xuatBaoDuong += tongSoLuong;
            }
        }

        return ThongKePhieuXuatResponse.builder()
                .tongXuatHomNay(xuatHomNay)
                .dangBocDoXepXe(dangBocDo)
                .xuatBaoDuongDinhKy(xuatBaoDuong)
                .tyLeXuatDungTienDo(99.4) // Có thể phát triển logic tính SLA sau
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PhieuXuatResponse layChiTiet(Long id) {
        PhieuKho pk = phieuKhoRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu"));
        return mapToResponse(pk);
    }

    @Override
    @Transactional
    public PhieuXuatResponse taoPhieuXuat(TaoPhieuXuatRequest request) {
        Kho kho = khoRepository.findById(request.getKhoId()).orElseThrow();
        NhanVien nv = nhanVienRepository.findById(request.getNhanVienXuatId()).orElse(null);

        // Lưu thông tin loại xuất và lý do vào Ghi chú để tuân thủ Schema DB
        String ghiChuGop = "Loại xuất: " + request.getLoaiXuat() + " - Lý do: " + request.getLyDoXuat();

        // Lấy mã chứng từ (Dùng PX- cho Xuất kho)
        // Sửa "PK" thành "PX"
String maPhieu = phieuKhoRepository.getMaChungTu("PX", "PX-");
        
        PhieuKho pk = PhieuKho.builder()
                .maPhieu(maPhieu)
                .loaiPhieu("xuat_kho")
                .kho(kho)
                .nhanVienGiaoNhan(nv)
                .bienSoXeDoiTac(request.getBienSoXe())
                .ghiChu(ghiChuGop)
                .trangThai(request.getTrangThai())
                .ngayGio(LocalDateTime.now())
                .build();
        pk = phieuKhoRepository.save(pk);

        for (ChiTietPhieuXuatItemRequest item : request.getChiTiet()) {
            MatHang mh = matHangRepository.findById(item.getMatHangId()).orElseThrow();
            LoHang lo = null;

            if (item.getLoId() != null) {
                lo = loHangRepository.findById(item.getLoId()).orElse(null);
            }

            // Lưu chi tiết phiếu
            ChiTietPhieuKho ct = ChiTietPhieuKho.builder()
                    .phieuKho(pk)
                    .matHang(mh)
                    .lo(lo)
                    .soLuong(item.getSoLuong())
                    .ghiChu(item.getGhiChu())
                    .build();
            chiTietPhieuKhoRepository.save(ct);

            // XỬ LÝ TRỪ TỒN KHO NẾU HOÀN TẤT
            if ("da_hoan_thanh".equals(pk.getTrangThai())) {
                // 1. Trừ bảng TonKho
                TonKho tk = tonKhoRepository.findByMatHang_IdAndKho_Id(mh.getId(), kho.getId())
                        .orElseThrow(() -> new RuntimeException("Sản phẩm chưa có trong kho này!"));
                
                if (tk.getSoLuongTonKho() < item.getSoLuong()) {
                    throw new RuntimeException("Tồn kho không đủ cho mã: " + mh.getMaMatHang());
                }
                tk.setSoLuongTonKho(tk.getSoLuongTonKho() - item.getSoLuong());
                tonKhoRepository.save(tk);

                // 2. Trừ số lượng còn lại của Lô Hàng (nếu có chọn Lô)
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

        return mapToResponse(pk);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] xuatExcel(String keyword, String trangThai, LocalDateTime tuNgay, LocalDateTime denNgay) {
        Specification<PhieuKho> spec = taoDieuKienLoc(keyword, trangThai, tuNgay, denNgay);
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
                row.createCell(4).setCellValue(dto.getLyDoXuat());
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
        int tongSoLuong = chiTietPhieuKhoRepository.findByPhieuKho_Id(pk.getId())
                .stream().mapToInt(ChiTietPhieuKho::getSoLuong).sum();

        // Tách Loại xuất và Lý do từ Ghi chú
        String loaiXuat = "Xuất bán";
        String lyDo = pk.getGhiChu();
        if (pk.getGhiChu() != null && pk.getGhiChu().contains(" - Lý do: ")) {
            String[] parts = pk.getGhiChu().split(" - Lý do: ");
            loaiXuat = parts[0].replace("Loại xuất: ", "");
            if (parts.length > 1) {
                lyDo = parts[1];
            }
        }

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
                .build();
    }
}