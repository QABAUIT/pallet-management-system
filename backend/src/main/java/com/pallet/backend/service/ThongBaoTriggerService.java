package com.pallet.backend.service;

import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.entity.ThongBao;
import com.pallet.backend.entity.VaiTro;
import com.pallet.backend.repository.ThongBaoRepository;
import com.pallet.backend.repository.VaiTroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Service dùng nội bộ để tạo thông báo tự động sau các sự kiện nghiệp vụ.
 * Không throw exception để không làm rollback transaction cha.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ThongBaoTriggerService {

    private final ThongBaoRepository thongBaoRepository;
    private final VaiTroRepository vaiTroRepository;
    private final SseNotificationService sseNotificationService;

    // ------------------------------------------------------------------ HÓA ĐƠN

    /** Hóa đơn vừa được tạo, trạng thái "chờ thanh toán" -> thông báo cho NVBANHANG & PGD/GD */
    public void hoaDonTaoMoi(Long hoaDonId, String maHoaDon, String tenKhachHang,
                              BigDecimal tongThanhToan, NhanVien nguoiLap) {
        try {
            VaiTro vaiTroNhan = timVaiTro("NVBANHANG");
            String noiDung = String.format(
                    "Hóa đơn %s cho khách hàng %s đã được tạo bởi %s. " +
                    "Tổng giá trị: %s đ. Trạng thái: Chờ thanh toán.",
                    maHoaDon, tenKhachHang, nguoiLap != null ? nguoiLap.getHoTen() : "Hệ thống",
                    String.format("%,.0f", tongThanhToan));

            luuThongBao(ThongBao.builder()
                    .loaiThongBao("hoa_don")
                    .tieuDe("Hóa đơn mới: " + maHoaDon)
                    .noiDung(noiDung)
                    .vaiTroNhan(vaiTroNhan)
                    .doiTuongLoai("hoa_don")
                    .doiTuongId(hoaDonId)
                    .daDoc(false)
                    .thoiGianTao(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.warn("Không thể tạo thông báo hóa đơn tạo mới [{}]: {}", maHoaDon, e.getMessage());
        }
    }

    /** Hóa đơn vừa được thanh toán -> thông báo cho NVBANHANG & GD */
    public void hoaDonThanhToan(Long hoaDonId, String maHoaDon, String tenKhachHang,
                                 BigDecimal tongThanhToan, NhanVien nguoiThucHien) {
        try {
            VaiTro vaiTroNhan = timVaiTro("NVBANHANG");
            String noiDung = String.format(
                    "Hóa đơn %s của khách hàng %s đã được thanh toán bởi %s. " +
                    "Tổng: %s đ.",
                    maHoaDon, tenKhachHang, nguoiThucHien != null ? nguoiThucHien.getHoTen() : "Hệ thống",
                    String.format("%,.0f", tongThanhToan));

            luuThongBao(ThongBao.builder()
                    .loaiThongBao("hoa_don")
                    .tieuDe("Thanh toán xác nhận: " + maHoaDon)
                    .noiDung(noiDung)
                    .vaiTroNhan(vaiTroNhan)
                    .doiTuongLoai("hoa_don")
                    .doiTuongId(hoaDonId)
                    .daDoc(false)
                    .thoiGianTao(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.warn("Không thể tạo thông báo thanh toán hóa đơn [{}]: {}", maHoaDon, e.getMessage());
        }
    }

    /** Hóa đơn bị hủy -> thông báo cho GD */
    public void hoaDonHuy(Long hoaDonId, String maHoaDon, String lyDo, NhanVien nguoiHuy) {
        try {
            String noiDung = String.format(
                    "Hóa đơn %s đã bị hủy bởi %s. Lý do: %s",
                    maHoaDon, nguoiHuy != null ? nguoiHuy.getHoTen() : "Hệ thống", lyDo != null ? lyDo : "Không có lý do");

            VaiTro vaiTroGD = timVaiTro("GD");
            luuThongBao(ThongBao.builder()
                    .loaiThongBao("hoa_don")
                    .tieuDe("⚠ Hóa đơn bị hủy: " + maHoaDon)
                    .noiDung(noiDung)
                    .vaiTroNhan(vaiTroGD)
                    .doiTuongLoai("hoa_don")
                    .doiTuongId(hoaDonId)
                    .daDoc(false)
                    .thoiGianTao(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.warn("Không thể tạo thông báo hủy hóa đơn [{}]: {}", maHoaDon, e.getMessage());
        }
    }

    // ------------------------------------------------------------------ NHẬP KHO

    /** Phiếu nhập kho hoàn thành -> thông báo cho NVKHO */
    public void phieuNhapHoanThanh(Long phieuId, String maPhieu, String tenKho,
                                    int soLuongMatHang, NhanVien nguoiTiepNhan) {
        try {
            VaiTro vaiTroKho = timVaiTro("NVKHO");
            String tenNv = nguoiTiepNhan != null ? nguoiTiepNhan.getHoTen() : "Hệ thống";
            String noiDung = String.format(
                    "Phiếu nhập %s đã hoàn thành tại kho %s. " +
                    "%d mặt hàng được tiếp nhận. Người tiếp nhận: %s.",
                    maPhieu, tenKho, soLuongMatHang, tenNv);

            luuThongBao(ThongBao.builder()
                    .loaiThongBao("phieu_nhap")
                    .tieuDe("Nhập kho hoàn thành: " + maPhieu)
                    .noiDung(noiDung)
                    .vaiTroNhan(vaiTroKho)
                    .doiTuongLoai("phieu_nhap")
                    .doiTuongId(phieuId)
                    .daDoc(false)
                    .thoiGianTao(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.warn("Không thể tạo thông báo nhập kho [{}]: {}", maPhieu, e.getMessage());
        }
    }

    // ------------------------------------------------------------------ XUẤT KHO

    /** Phiếu xuất kho hoàn thành -> thông báo cho NVKHO */
    public void phieuXuatHoanThanh(Long phieuId, String maPhieu, String tenKho,
                                    int soLuongMatHang, NhanVien nguoiThucHien) {
        try {
            VaiTro vaiTroKho = timVaiTro("NVKHO");
            String tenNv = nguoiThucHien != null ? nguoiThucHien.getHoTen() : "Hệ thống";
            String noiDung = String.format(
                    "Phiếu xuất %s đã hoàn thành tại kho %s. " +
                    "%d mặt hàng đã xuất. Người thực hiện: %s.",
                    maPhieu, tenKho, soLuongMatHang, tenNv);

            luuThongBao(ThongBao.builder()
                    .loaiThongBao("phieu_xuat")
                    .tieuDe("Xuất kho hoàn thành: " + maPhieu)
                    .noiDung(noiDung)
                    .vaiTroNhan(vaiTroKho)
                    .doiTuongLoai("phieu_xuat")
                    .doiTuongId(phieuId)
                    .daDoc(false)
                    .thoiGianTao(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            log.warn("Không thể tạo thông báo xuất kho [{}]: {}", maPhieu, e.getMessage());
        }
    }

    // ------------------------------------------------------------------ HELPERS

    private VaiTro timVaiTro(String maVaiTro) {
        return vaiTroRepository.findByMaVaiTro(maVaiTro).orElse(null);
    }

    private void luuThongBao(ThongBao tb) {
        ThongBao saved = thongBaoRepository.save(tb);
        try {
            com.pallet.backend.dto.response.ThongBaoResponse res = com.pallet.backend.dto.response.ThongBaoResponse.builder()
                    .id(saved.getId())
                    .loaiThongBao(saved.getLoaiThongBao())
                    .tieuDe(saved.getTieuDe())
                    .noiDung(saved.getNoiDung())
                    .nguoiNhanId(saved.getNguoiNhan() != null ? saved.getNguoiNhan().getId() : null)
                    .tenNguoiNhan(saved.getNguoiNhan() != null ? saved.getNguoiNhan().getHoTen() : null)
                    .vaiTroNhanId(saved.getVaiTroNhan() != null ? saved.getVaiTroNhan().getId() : null)
                    .tenVaiTroNhan(saved.getVaiTroNhan() != null ? saved.getVaiTroNhan().getTenVaiTro() : null)
                    .doiTuongLoai(saved.getDoiTuongLoai())
                    .doiTuongId(saved.getDoiTuongId())
                    .daDoc(saved.getDaDoc())
                    .thoiGianTao(saved.getThoiGianTao())
                    .build();
            sseNotificationService.broadcast(res);
        } catch (Exception e) {
            log.warn("Không thể push thông báo SSE [{}]: {}", saved.getId(), e.getMessage());
        }
    }
}
