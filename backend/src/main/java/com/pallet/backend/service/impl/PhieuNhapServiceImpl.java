package com.pallet.backend.service.impl;

import org.springframework.transaction.annotation.Transactional;
import com.pallet.backend.dto.request.ChiTietPhieuNhapItemRequest;
import com.pallet.backend.dto.request.TaoPhieuNhapRequest;
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

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PhieuNhapServiceImpl implements PhieuNhapService {

    private final PhieuKhoRepository phieuKhoRepository;
    private final ChiTietPhieuKhoRepository chiTietPhieuKhoRepository;
    private final MatHangRepository matHangRepository;
    private final KhoRepository khoRepository;
    private final NhaCungCapRepository nccRepository;
    private final NhanVienRepository nhanVienRepository;
    private final LoHangRepository loHangRepository;
    private final TonKhoRepository tonKhoRepository;
    
    // Thêm Repository này để đếm tổng số vị trí lưu trữ của kho
    private final ViTriLuuTruRepository viTriLuuTruRepository;

    private Specification<PhieuKho> taoDieuKienLoc(String keyword, String trangThai, LocalDateTime tuNgay, LocalDateTime denNgay) {
        Specification<PhieuKho> spec = Specification.where(PhieuKhoSpecifications.loaiPhieu("nhap_kho"));

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
    public Page<PhieuNhapResponse> layDanhSach(String keyword, String trangThai, LocalDateTime tuNgay, LocalDateTime denNgay, Pageable pageable) {
        Specification<PhieuKho> spec = taoDieuKienLoc(keyword, trangThai, tuNgay, denNgay);
        return phieuKhoRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ThongKePhieuNhapResponse layThongKe() {
        // Tính khung thời gian hôm nay
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

    // Lấy số lượng nhập hoàn thành hôm nay
    Integer nhapTrongNgayQuery = chiTietPhieuKhoRepository.sumSoLuongByLoaiPhieuAndTrangThaiAndDateRange("nhap_kho", "da_hoan_thanh", startOfDay, endOfDay);
        Integer dangKiemDinhQuery = chiTietPhieuKhoRepository.sumSoLuongByLoaiPhieuAndTrangThai("nhap_kho", "cho_xu_ly");
        
        int nhapTrongNgay = nhapTrongNgayQuery != null ? nhapTrongNgayQuery : 0;
        int dangKiemDinh = dangKiemDinhQuery != null ? dangKiemDinhQuery : 0;

        // TÍNH TOÁN SỨC CHỨA KHẢ DỤNG (Xử lý cho thẻ thứ 4)
        int tongSlot = (int) viTriLuuTruRepository.count();
        if (tongSlot == 0) tongSlot = 24000; // Fallback nếu DB rỗng để không bị chia cho 0
        
        Integer slotDaDungQuery = tonKhoRepository.countViTriDaSuDung();
        int slotDaDung = slotDaDungQuery != null ? slotDaDungQuery : 0;
        int slotTrong = tongSlot - slotDaDung;
        
        // Tính % và làm tròn 1 chữ số
        double phanTram = ((double) slotTrong / tongSlot) * 100;
        phanTram = Math.round(phanTram * 10.0) / 10.0;

        return ThongKePhieuNhapResponse.builder()
                .nhapKhoTrongNgay(nhapTrongNgay)
                .dangKiemDinh(dangKiemDinh)
                .tongGiaTriNhapThang(0L) 
                .sucChuaKhaDungPhanTram(phanTram) // Dữ liệu trả về FE
                .tongSoSlot(tongSlot)             // Dữ liệu trả về FE
                .soSlotTrong(slotTrong)           // Dữ liệu trả về FE
                .build();
    }

    @Override
    @Transactional(readOnly = true) 
    public PhieuNhapResponse layChiTiet(Long id) {
        PhieuKho pk = phieuKhoRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu"));
        return mapToResponse(pk);
    }

    @Override
    @Transactional // Bỏ readOnly = true ở đây (Lỗi 1)
    public PhieuNhapResponse taoPhieuNhap(TaoPhieuNhapRequest request) {
        Kho kho = khoRepository.findById(request.getKhoId()).orElseThrow();
        NhanVien nv = nhanVienRepository.findById(request.getNhanVienTiepNhanId()).orElse(null);
        NhaCungCap ncc = nccRepository.findById(request.getNccId()).orElse(null);

        // 1. Lưu Phiếu kho (Đã sửa lỗi "Bộ đếm chung" - Lỗi 4)
        String maPhieu = phieuKhoRepository.getMaChungTu("PN", "PN-"); 
        
        PhieuKho pk = PhieuKho.builder()
                .maPhieu(maPhieu)
                .loaiPhieu("nhap_kho")
                .kho(kho)
                .nhanVienGiaoNhan(nv)
                .bienSoXeDoiTac(request.getBienSoXe())
                .ghiChu(request.getGhiChu())
                .trangThai(request.getTrangThai())
                .ngayGio(LocalDateTime.now())
                .build();
        pk = phieuKhoRepository.save(pk);

        // 2. Lưu Chi tiết, sinh Lô hàng và cộng Tồn kho
        for (ChiTietPhieuNhapItemRequest item : request.getChiTiet()) {
            MatHang mh = matHangRepository.findById(item.getMatHangId()).orElseThrow();
            
            // Tạo lô hàng tự động (Đang sinh lô "ảo" dù phiếu chưa hoàn thành, nên cân nhắc nghiệp vụ này)
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

            // Lưu chi tiết phiếu
            ChiTietPhieuKho ct = ChiTietPhieuKho.builder()
                    .phieuKho(pk)
                    .matHang(mh)
                    .lo(lo)
                    .soLuong(item.getSoLuong())
                    .ghiChu(item.getGhiChu())
                    .build();
            chiTietPhieuKhoRepository.save(ct);

            // Cập nhật Tồn kho nếu phiếu Đã hoàn thành (Chốt thực tế)
            if ("da_hoan_thanh".equals(pk.getTrangThai())) {
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

        return mapToResponse(pk);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] xuatExcel(String keyword, String trangThai, LocalDateTime tuNgay, LocalDateTime denNgay) {
        Specification<PhieuKho> spec = taoDieuKienLoc(keyword, trangThai, tuNgay, denNgay);
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
        
        List<ChiTietPhieuKho> dsChiTiet = chiTietPhieuKhoRepository.findByPhieuKho_Id(pk.getId());
        if(dsChiTiet != null && !dsChiTiet.isEmpty()) {
            tongSoLuong = dsChiTiet.stream().mapToInt(ChiTietPhieuKho::getSoLuong).sum();
            if (dsChiTiet.get(0).getLo() != null && dsChiTiet.get(0).getLo().getNcc() != null) {
                tenNcc = dsChiTiet.get(0).getLo().getNcc().getTenNcc();
            }
        }

        return PhieuNhapResponse.builder()
                .id(pk.getId())
                .maPhieu(pk.getMaPhieu())
                .tenNhaCungCap(tenNcc)
                .tenNhanVienTiepNhan(pk.getNhanVienGiaoNhan() != null ? pk.getNhanVienGiaoNhan().getHoTen() : "")
                .ngayNhapKho(pk.getNgayGio())
                .tongSoLuongPallet(tongSoLuong)
                .trangThai(pk.getTrangThai())
                .build();
    }
}