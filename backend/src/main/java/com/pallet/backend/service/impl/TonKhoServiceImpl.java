package com.pallet.backend.service.impl;

import com.pallet.backend.dto.response.ThongKeTonKhoResponse;
import com.pallet.backend.dto.response.TonKhoResponse;
import com.pallet.backend.entity.MatHang;
import com.pallet.backend.entity.TonKho;
import com.pallet.backend.repository.TonKhoRepository;
import com.pallet.backend.repository.ViTriLuuTruRepository;
import com.pallet.backend.repository.KhoRepository;
import com.pallet.backend.repository.specification.TonKhoSpecifications;
import com.pallet.backend.service.TonKhoService;
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
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TonKhoServiceImpl implements TonKhoService {

    private final TonKhoRepository tonKhoRepository;
    private final ViTriLuuTruRepository viTriLuuTruRepository;
    private final KhoRepository khoRepository;

    private Specification<TonKho> taoDieuKienLoc(String keyword, String chatLieu, Long khoId) {
        Specification<TonKho> spec = (root, query, cb) -> cb.conjunction();

        spec = spec.and(TonKhoSpecifications.khoId(khoId));
        spec = spec.and((root, query, cb) ->
                cb.notEqual(root.join("matHang").get("loaiMatHang"), "cho_tai_che"));

        if (keyword != null && !keyword.trim().isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                String kw = "%" + keyword.toLowerCase() + "%";
                return cb.or(
                        cb.like(cb.lower(root.join("matHang").get("maMatHang")), kw),
                        cb.like(cb.lower(root.join("matHang").get("tenMatHang")), kw)
                );
            });
        }

        if (chatLieu != null && !chatLieu.trim().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.join("matHang").get("chatLieu"), chatLieu));
        }

        return spec;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TonKhoResponse> layDanhSachTonKho(String keyword, String chatLieu, Long khoId, Pageable pageable) {
        Specification<TonKho> spec = taoDieuKienLoc(keyword, chatLieu, khoId);
        Page<TonKho> pageResult = tonKhoRepository.findAll(spec, pageable);
        return pageResult.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ThongKeTonKhoResponse layThongKeTonKho(Long khoId) {
        List<TonKho> tatCaTonKho = tonKhoRepository.findByKho_Id(khoId);

        int tongSoLuong = 0;
        BigDecimal tongGiaTri = BigDecimal.ZERO;
        int soLuongCanhBao = 0;

        for (TonKho tk : tatCaTonKho) {
            MatHang mh = tk.getMatHang();
            if (!"cho_tai_che".equals(mh.getLoaiMatHang())) {
                tongSoLuong += tk.getSoLuongTonKho();

                BigDecimal donGia = mh.getDonGiaBan() != null ? mh.getDonGiaBan() : BigDecimal.ZERO;
                tongGiaTri = tongGiaTri.add(donGia.multiply(BigDecimal.valueOf(tk.getSoLuongTonKho())));

                if (tk.getSoLuongTonKho() <= tk.getSoLuongToiThieu()) {
                    soLuongCanhBao++;
                }
            }
        }

        return ThongKeTonKhoResponse.builder()
                .tongSoLuongTon(tongSoLuong)
                .tongGiaTriUocTinh(tongGiaTri)
                .soChungLoaiCanhBao(soLuongCanhBao)
                .build();
    }

    // MỚI: Trả về danh sách Kho
    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> layDanhSachKho() {
        return khoRepository.findAll().stream().map(k -> Map.<String, Object>of(
            "id", k.getId(),
            "tenKho", k.getTenKho()
        )).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> layDanhSachViTriKho(Long khoId) {
        return viTriLuuTruRepository.findByKho_Id(khoId).stream().map(v -> Map.<String, Object>of(
            "id", v.getId(),
            "maViTri", v.getMaViTri(),
            "tenViTri", String.format("Khu %s - Kệ %s - Tầng %s", 
                            v.getKhuVuc(), 
                            v.getKe() != null ? v.getKe() : "-", 
                            v.getTang() != null ? v.getTang() : "-")
        )).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] xuatExcelBaoCaoTonKho(String keyword, String chatLieu, Long khoId) {
        Specification<TonKho> spec = taoDieuKienLoc(keyword, chatLieu, khoId);
        List<TonKho> danhSachTonKho = tonKhoRepository.findAll(spec);

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Báo cáo tồn kho");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Mã SKU", "Tên Pallet", "Phân loại", "Kích thước (DxRxC mm)", "Số lượng tồn", "Đơn giá định mức", "Tình trạng"};

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (TonKho tk : danhSachTonKho) {
                Row row = sheet.createRow(rowIdx++);
                TonKhoResponse dto = mapToResponse(tk);

                row.createCell(0).setCellValue(dto.getMaSku());
                row.createCell(1).setCellValue(dto.getTenPallet());
                row.createCell(2).setCellValue(dto.getPhanLoai() != null ? dto.getPhanLoai() : "");
                row.createCell(3).setCellValue(dto.getKichThuoc() != null ? dto.getKichThuoc() : "");
                row.createCell(4).setCellValue(dto.getSoLuongTon());
                row.createCell(5).setCellValue(dto.getDonGiaDinhMuc() != null ? dto.getDonGiaDinhMuc().doubleValue() : 0);
                row.createCell(6).setCellValue(dto.getTinhTrang());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi tạo file Excel báo cáo tồn kho: " + e.getMessage());
        }
    }

    private TonKhoResponse mapToResponse(TonKho tonKho) {
        MatHang mh = tonKho.getMatHang();

        String tinhTrang = "Đủ hàng";
        if (tonKho.getSoLuongTonKho() == 0) {
            tinhTrang = "Cần nhập";
        } else if (tonKho.getSoLuongTonKho() <= tonKho.getSoLuongToiThieu()) {
            tinhTrang = "Sắp hết";
        }

        String kichThuoc = "";
        if (mh.getKichThuocDai() != null && mh.getKichThuocRong() != null && mh.getKichThuocCao() != null) {
            kichThuoc = mh.getKichThuocDai() + " x " + mh.getKichThuocRong() + " x " + mh.getKichThuocCao();
        }

        return TonKhoResponse.builder()
                .matHangId(mh.getId())
                .maSku(mh.getMaMatHang())
                .tenPallet(mh.getTenMatHang())
                .phanLoai(mh.getTieuChuan() != null ? mh.getTieuChuan() : mh.getLoaiMatHang())
                .kichThuoc(kichThuoc)
                .soLuongTon(tonKho.getSoLuongTonKho())
                .donGiaDinhMuc(mh.getDonGiaBan())
                .tinhTrang(tinhTrang)
                .build();
    }
}