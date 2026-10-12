package com.pallet.backend.service.impl;

import com.pallet.backend.dto.request.CapNhatChiTietKiemKeRequest;
import com.pallet.backend.dto.request.TaoPhienKiemKeRequest;
import com.pallet.backend.dto.response.ChiTietKiemKeResponse;
import com.pallet.backend.dto.response.ThongKeKiemKeResponse;
import com.pallet.backend.entity.*;
import com.pallet.backend.repository.*;
import com.pallet.backend.service.KiemKeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KiemKeServiceImpl implements KiemKeService {

    private final PhienKiemKeRepository phienKiemKeRepository;
    private final ChiTietKiemKeRepository chiTietKiemKeRepository;
    private final TonKhoRepository tonKhoRepository;
    private final KhoRepository khoRepository;
    private final NhanVienRepository nhanVienRepository;
    private final PhieuKhoRepository phieuKhoRepository;

    @Override
    @Transactional
    public PhienKiemKe taoPhienKiemKe(TaoPhienKiemKeRequest request) {
        Kho kho = khoRepository.findById(request.getKhoId()).orElseThrow();
        NhanVien nguoiTao = nhanVienRepository.findById(request.getNguoiTaoId()).orElseThrow();

        // 1. Tạo Phiên kiểm kê
        String maPhien = phieuKhoRepository.getMaChungTu("PKK", "PKK-"); // Có thể dùng sequence PKK
        PhienKiemKe phien = PhienKiemKe.builder()
                .maPhien(maPhien)
                .kho(kho)
                .nguoiTao(nguoiTao)
                .ngayBatDau(LocalDate.now())
                .trangThai("dang_dien_ra")
                .ghiChu(request.getGhiChu())
                .build();
        phien = phienKiemKeRepository.save(phien);

        // 2. Chụp Snapshot Tồn Kho hiện tại đưa vào ChiTietKiemKe
        List<TonKho> dsTonKho = tonKhoRepository.findByKho_Id(kho.getId());
        for (TonKho tk : dsTonKho) {
            // Lọc bỏ hàng chờ tái chế
            if ("cho_tai_che".equals(tk.getMatHang().getLoaiMatHang())) continue;

            ChiTietKiemKe ct = ChiTietKiemKe.builder()
                    .phienKiemKe(phien)
                    .matHang(tk.getMatHang())
                    .viTri(tk.getViTri())
                    .tonHeThong(tk.getSoLuongTonKho())
                    .tonThucTe(null) // Để null theo DB Schema
                    .trangThaiKiemKe("cho_kiem")
                    .build();
            chiTietKiemKeRepository.save(ct);
        }

        return phien;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChiTietKiemKeResponse> layDanhSachChiTiet(Long phienKiemKeId) {
        List<ChiTietKiemKe> dsChiTiet = chiTietKiemKeRepository.findByPhienKiemKe_Id(phienKiemKeId);
        return dsChiTiet.stream().map(ct -> {
            String viTri = ct.getViTri() != null ? 
                    "Khu " + ct.getViTri().getKhuVuc() + " - Kệ " + ct.getViTri().getKe() : "Chưa xếp vị trí";
            
            // Tính toán tay cho an toàn nếu JPA chưa fetch Generated Column kịp
            Integer chenhLech = null;
            if (ct.getTonThucTe() != null) {
                chenhLech = ct.getTonThucTe() - ct.getTonHeThong();
            }

            return ChiTietKiemKeResponse.builder()
                    .id(ct.getId())
                    .maSku(ct.getMatHang().getMaMatHang())
                    .tenPallet(ct.getMatHang().getTenMatHang())
                    .viTriLuuTru(viTri)
                    .tonHeThong(ct.getTonHeThong())
                    .tonThucTe(ct.getTonThucTe())
                    .chenhLech(chenhLech)
                    .trangThaiKiemKe(ct.getTrangThaiKiemKe())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ThongKeKiemKeResponse layThongKe(Long phienKiemKeId) {
        List<ChiTietKiemKe> dsChiTiet = chiTietKiemKeRepository.findByPhienKiemKe_Id(phienKiemKeId);
        
        int tongHeThong = 0;
        int tongThucTe = 0;
        int slLech = 0;
        int soDongDaKiem = 0;

        for (ChiTietKiemKe ct : dsChiTiet) {
            tongHeThong += ct.getTonHeThong();
            if (ct.getTonThucTe() != null) {
                tongThucTe += ct.getTonThucTe();
                soDongDaKiem++;
            }
            if ("lech".equals(ct.getTrangThaiKiemKe())) {
                slLech++;
            }
        }

        double tienDo = dsChiTiet.isEmpty() ? 0 : ((double) soDongDaKiem / dsChiTiet.size()) * 100;

        return ThongKeKiemKeResponse.builder()
                .tongPalletHeThong(tongHeThong)
                .daKiemTraThucTe(tongThucTe)
                .tienDoPhanTram(Math.round(tienDo * 10.0) / 10.0) // Làm tròn 1 chữ số thập phân
                .phatHienChenhLech(slLech)
                .build();
    }

    @Override
    @Transactional
    public void capNhatChiTiet(Long chiTietId, CapNhatChiTietKiemKeRequest request) {
        ChiTietKiemKe ct = chiTietKiemKeRepository.findById(chiTietId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dòng kiểm kê"));

        ct.setTonThucTe(request.getTonThucTe());
        ct.setGhiChuGiaiTrinh(request.getGhiChuGiaiTrinh());
        
        if (request.getNguoiKiemId() != null) {
            NhanVien nv = nhanVienRepository.findById(request.getNguoiKiemId()).orElse(null);
            ct.setNguoiKiem(nv);
        }

        // Đảm bảo tuân thủ Ràng buộc Constraint của Database
        if (request.getTonThucTe() == null) {
            ct.setTrangThaiKiemKe("cho_kiem");
        } else if (request.getTonThucTe().equals(ct.getTonHeThong())) {
            ct.setTrangThaiKiemKe("khop");
        } else {
            ct.setTrangThaiKiemKe("lech");
        }

        chiTietKiemKeRepository.save(ct);
    }

    @Override
    @Transactional
    public void hoanTatKiemKe(Long phienKiemKeId, Long nguoiChotId) {
        PhienKiemKe phien = phienKiemKeRepository.findById(phienKiemKeId).orElseThrow();
        NhanVien nguoiChot = nhanVienRepository.findById(nguoiChotId).orElseThrow();

        List<ChiTietKiemKe> dsChiTiet = chiTietKiemKeRepository.findByPhienKiemKe_Id(phien.getId());
        
        // Kiểm tra xem đã kiểm hết chưa
        boolean conThieu = dsChiTiet.stream().anyMatch(ct -> "cho_kiem".equals(ct.getTrangThaiKiemKe()));
        if (conThieu) {
            throw new RuntimeException("Không thể chốt sổ! Vẫn còn Pallet đang ở trạng thái 'Chờ kiểm'.");
        }

        // 1. Cập nhật trạng thái Phiên
        phien.setTrangThai("da_hoan_tat");
        phien.setNguoiChot(nguoiChot);
        phien.setNgayHoanTat(LocalDate.now());
        phienKiemKeRepository.save(phien);

        // 2. Tạo Phiếu Kho Điều Chỉnh Kiểm Kê cho các mã bị lệch
        List<ChiTietKiemKe> dsLech = dsChiTiet.stream()
                .filter(ct -> "lech".equals(ct.getTrangThaiKiemKe()))
                .collect(Collectors.toList());

        if (!dsLech.isEmpty()) {
            String maPhieu = phieuKhoRepository.getMaChungTu("PDC", "PDC-");
            PhieuKho phieuDieuChinh = PhieuKho.builder()
                    .maPhieu(maPhieu)
                    .loaiPhieu("dieu_chinh_kiem_ke")
                    .kho(phien.getKho())
                    .nguoiDuyet(nguoiChot)
                    .ghiChu("Hệ thống tự động sinh phiếu điều chỉnh kho sau kiểm kê phiên: " + phien.getMaPhien())
                    .trangThai("da_hoan_thanh")
                    .ngayGio(LocalDateTime.now())
                    .build();
            phieuKhoRepository.save(phieuDieuChinh);

            // Cập nhật lại tồn kho thực tế và Lưu lịch sử phiếu
            for (ChiTietKiemKe ct : dsLech) {
                // Update tồn kho
                TonKho tk = tonKhoRepository.findByMatHang_IdAndKho_Id(ct.getMatHang().getId(), phien.getKho().getId())
                        .orElseThrow();
                tk.setSoLuongTonKho(ct.getTonThucTe());
                tonKhoRepository.save(tk);
            }
        }
    }
}