package com.pallet.backend.service.impl;

import com.pallet.backend.dto.request.ChiTietPhieuXuatItemRequest;
import com.pallet.backend.dto.request.TaoPhieuXuatRequest;
import com.pallet.backend.dto.response.ChiTietPhieuKhoResponse;
import com.pallet.backend.dto.response.PhieuXuatResponse;
import com.pallet.backend.dto.response.ThongKePhieuXuatResponse;
import com.pallet.backend.entity.*;
import com.pallet.backend.exception.BusinessException;
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

    private static final int SLA_GIO = 24;

    private final PhieuKhoRepository phieuKhoRepository;
    private final ChiTietPhieuKhoRepository chiTietPhieuKhoRepository;
    private final MatHangRepository matHangRepository;
    private final KhoRepository khoRepository;
    private final NhanVienRepository nhanVienRepository;
    private final LoHangRepository loHangRepository;
    private final TonKhoRepository tonKhoRepository;
    private final com.pallet.backend.service.ThongBaoTriggerService thongBaoTrigger;

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
        PhieuKho pk = phieuKhoRepository.findById(id).orElseThrow(() -> new BusinessException("Không tìm thấy phiếu"));
        return mapToResponse(pk);
    }

    private void kiemTraDauVao(TaoPhieuXuatRequest request) {
        if (request.getChiTiet() == null || request.getChiTiet().isEmpty()) {
            throw new BusinessException("Phiếu xuất cần ít nhất 1 mặt hàng");
        }
        for (ChiTietPhieuXuatItemRequest it : request.getChiTiet()) {
            if (it.getMatHangId() == null || it.getSoLuong() == null || it.getSoLuong() <= 0) {
                throw new BusinessException("Mỗi dòng hàng cần chọn mặt hàng và số lượng lớn hơn 0");
            }
        }
    }

    private String gopGhiChu(TaoPhieuXuatRequest request) {
        return "Loại xuất: " + request.getLoaiXuat() + " - Lý do: " + request.getLyDoXuat();
    }

    @Override
    @Transactional
    public PhieuXuatResponse taoPhieuXuat(TaoPhieuXuatRequest request) {
        if (request.getKhoId() == null) throw new BusinessException("Thiếu thông tin kho");
        kiemTraDauVao(request);

        String trangThai = request.getTrangThai() == null ? CHO_XU_LY : request.getTrangThai();
        if (!CHO_XU_LY.equals(trangThai) && !DA_HOAN_THANH.equals(trangThai)) {
            throw new BusinessException("Trạng thái phiếu xuất không hợp lệ: " + trangThai);
        }

        Kho kho = khoRepository.findById(request.getKhoId()).orElseThrow(() -> new BusinessException("Không tìm thấy kho"));
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
        if (DA_HOAN_THANH.equals(trangThai)) {
            thongBaoTrigger.phieuXuatHoanThanh(pk.getId(), pk.getMaPhieu(), kho.getTenKho(),
                    request.getChiTiet() != null ? request.getChiTiet().size() : 0, nv);
        }
        return mapToResponse(pk);
    }

    @Override
    @Transactional
    public PhieuXuatResponse capNhatPhieuXuat(Long id, TaoPhieuXuatRequest request) {
        PhieuKho pk = phieuKhoRepository.findById(id)
                .filter(p -> LOAI_PHIEU.equals(p.getLoaiPhieu()))
                .orElseThrow(() -> new BusinessException("Không tìm thấy phiếu xuất"));
        if (!CHO_XU_LY.equals(pk.getTrangThai())) {
            throw new BusinessException("Chỉ được sửa phiếu đang ở trạng thái Chờ xử lý");
        }

        String trangThaiMoi = request.getTrangThai() == null ? CHO_XU_LY : request.getTrangThai();
        if (!CHO_XU_LY.equals(trangThaiMoi) && !DA_HOAN_THANH.equals(trangThaiMoi) && !DA_HUY.equals(trangThaiMoi)) {
            throw new BusinessException("Trạng thái phiếu xuất không hợp lệ: " + trangThaiMoi);
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
        if (DA_HOAN_THANH.equals(trangThaiMoi)) {
            thongBaoTrigger.phieuXuatHoanThanh(pk.getId(), pk.getMaPhieu(),
                    pk.getKho() != null ? pk.getKho().getTenKho() : "",
                    request.getChiTiet() != null ? request.getChiTiet().size() : 0, pk.getNhanVienGiaoNhan());
        }
        return mapToResponse(pk);
    }

    private void ghiChiTietVaTruTon(PhieuKho pk, Kho kho, List<ChiTietPhieuXuatItemRequest> items) {
        for (ChiTietPhieuXuatItemRequest item : items) {
            MatHang mh = matHangRepository.findById(item.getMatHangId())
                    .orElseThrow(() -> new BusinessException("Không tìm thấy mặt hàng ID " + item.getMatHangId()));
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
                List<TonKho> tonKhos = tonKhoRepository.findByMatHang_Id(mh.getId()).stream()
                        .filter(t -> t.getKho().getId().equals(kho.getId()))
                        .toList();

                if (tonKhos.isEmpty()) {
                    throw new BusinessException("Sản phẩm " + mh.getTenMatHang() + " chưa có trong kho này!");
                }

                int tongTon = tonKhos.stream().mapToInt(TonKho::getSoLuongTonKho).sum();
                int tongGiuCho = tonKhos.stream().mapToInt(TonKho::getSoLuongDaGiuCho).sum();
                int tonKhaDung = tongTon - tongGiuCho;

                if (tonKhaDung < item.getSoLuong()) {
                    throw new BusinessException(String.format("Tồn kho khả dụng của %s chỉ còn %d cái, không đủ để xuất %d cái.",
                            mh.getTenMatHang(), tonKhaDung, item.getSoLuong()));
                }

                int soLuongCanTru = item.getSoLuong();
                for (TonKho tk : tonKhos) {
                    if (soLuongCanTru <= 0) break;
                    
                    int tkKhaDung = tk.getSoLuongTonKho() - tk.getSoLuongDaGiuCho();
                    if (tkKhaDung > 0) {
                        int truKhoan = Math.min(tkKhaDung, soLuongCanTru);
                        tk.setSoLuongTonKho(tk.getSoLuongTonKho() - truKhoan);
                        tonKhoRepository.save(tk);
                        soLuongCanTru -= truKhoan;
                    }
                }

                if (lo != null) {
                    if (lo.getSoLuongConLai() < item.getSoLuong()) {
                        throw new BusinessException("Số lượng trong lô " + lo.getMaLo() + " không đủ!");
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
            throw new BusinessException("Lỗi xuất Excel Lịch sử xuất kho");
        }
    }

    private PhieuXuatResponse mapToResponse(PhieuKho pk) {
        List<ChiTietPhieuKho> dsChiTiet = chiTietPhieuKhoRepository.findByPhieuKho_Id(pk.getId());
        int tongSoLuong = dsChiTiet.stream().mapToInt(ChiTietPhieuKho::getSoLuong).sum();

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