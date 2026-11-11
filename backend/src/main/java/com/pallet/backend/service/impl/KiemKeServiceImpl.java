package com.pallet.backend.service.impl;

import com.pallet.backend.dto.request.CapNhatChiTietKiemKeRequest;
import com.pallet.backend.dto.request.TaoPhienKiemKeRequest;
import com.pallet.backend.dto.response.ChiTietKiemKeResponse;
import com.pallet.backend.dto.response.ThongKeKiemKeResponse;
import com.pallet.backend.entity.*;
import com.pallet.backend.exception.BusinessException;
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

    private static final String DANG_DIEN_RA = "dang_dien_ra";
    private static final String DA_HOAN_TAT = "da_hoan_tat";
    private static final String CHO_KIEM = "cho_kiem";
    private static final String LECH = "lech";

    private final PhienKiemKeRepository phienKiemKeRepository;
    private final ChiTietKiemKeRepository chiTietKiemKeRepository;
    private final ChiTietPhieuKhoRepository chiTietPhieuKhoRepository;
    private final TonKhoRepository tonKhoRepository;
    private final KhoRepository khoRepository;
    private final NhanVienRepository nhanVienRepository;
    private final PhieuKhoRepository phieuKhoRepository;

    @Override
    @Transactional
    public PhienKiemKe taoPhienKiemKe(TaoPhienKiemKeRequest request) {
        if (request.getKhoId() == null) {
            throw new BusinessException("Thiếu thông tin kho kiểm kê");
        }
        if (request.getNguoiTaoId() == null) {
            throw new BusinessException("Thiếu thông tin người tạo phiên kiểm kê");
        }

        Kho kho = khoRepository.findById(request.getKhoId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy kho"));
        NhanVien nguoiTao = nhanVienRepository.findById(request.getNguoiTaoId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên"));

        // Mỗi kho chỉ được có 1 phiên đang diễn ra để tránh 2 phiên cùng chốt sổ.
        if (phienKiemKeRepository.existsByKho_IdAndTrangThai(kho.getId(), DANG_DIEN_RA)) {
            throw new BusinessException("Kho " + kho.getTenKho() + " đang có một phiên kiểm kê chưa hoàn tất");
        }

        String maPhien = phieuKhoRepository.getMaChungTu("PKK", "PKK-");
        PhienKiemKe phien = PhienKiemKe.builder()
                .maPhien(maPhien)
                .kho(kho)
                .nguoiTao(nguoiTao)
                .ngayBatDau(LocalDate.now())
                .trangThai(DANG_DIEN_RA)
                .ghiChu(request.getGhiChu())
                .build();
        phien = phienKiemKeRepository.save(phien);

        // Chụp snapshot tồn kho hiện tại đưa vào ChiTietKiemKe
        List<TonKho> dsTonKho = tonKhoRepository.findByKho_Id(kho.getId());
        for (TonKho tk : dsTonKho) {
            // Lọc bỏ hàng chờ tái chế
            if ("cho_tai_che".equals(tk.getMatHang().getLoaiMatHang())) continue;

            ChiTietKiemKe ct = ChiTietKiemKe.builder()
                    .phienKiemKe(phien)
                    .matHang(tk.getMatHang())
                    .viTri(tk.getViTri())
                    .tonHeThong(tk.getSoLuongTonKho())
                    .tonThucTe(null)
                    .trangThaiKiemKe(CHO_KIEM)
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
            if (LECH.equals(ct.getTrangThaiKiemKe())) {
                slLech++;
            }
        }

        double tienDo = dsChiTiet.isEmpty() ? 0 : ((double) soDongDaKiem / dsChiTiet.size()) * 100;

        return ThongKeKiemKeResponse.builder()
                .tongPalletHeThong(tongHeThong)
                .daKiemTraThucTe(tongThucTe)
                .tienDoPhanTram(Math.round(tienDo * 10.0) / 10.0)
                .phatHienChenhLech(slLech)
                .build();
    }

    @Override
    @Transactional
    public void capNhatChiTiet(Long chiTietId, CapNhatChiTietKiemKeRequest request) {
        ChiTietKiemKe ct = chiTietKiemKeRepository.findById(chiTietId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy dòng kiểm kê"));
        if (!DANG_DIEN_RA.equals(ct.getPhienKiemKe().getTrangThai())) {
            throw new BusinessException("Phiên kiểm kê đã hoàn tất, không thể sửa");
        }
        if (request.getTonThucTe() != null && request.getTonThucTe() < 0) {
            throw new BusinessException("Tồn thực tế không được âm");
        }

        ct.setTonThucTe(request.getTonThucTe());
        ct.setGhiChuGiaiTrinh(request.getGhiChuGiaiTrinh());

        if (request.getNguoiKiemId() != null) {
            NhanVien nv = nhanVienRepository.findById(request.getNguoiKiemId()).orElse(null);
            ct.setNguoiKiem(nv);
        }

        if (request.getTonThucTe() == null) {
            ct.setTrangThaiKiemKe(CHO_KIEM);
        } else if (request.getTonThucTe().equals(ct.getTonHeThong())) {
            ct.setTrangThaiKiemKe("khop");
        } else {
            ct.setTrangThaiKiemKe(LECH);
        }

        chiTietKiemKeRepository.save(ct);
    }

    @Override
    @Transactional
    public void hoanTatKiemKe(Long phienKiemKeId, Long nguoiChotId) {
        PhienKiemKe phien = phienKiemKeRepository.findForUpdate(phienKiemKeId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy phiên kiểm kê"));
        if (!DANG_DIEN_RA.equals(phien.getTrangThai())) {
            throw new BusinessException("Phiên kiểm kê này đã được chốt trước đó");
        }

        NhanVien nguoiChot = nhanVienRepository.findById(nguoiChotId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhân viên chốt kiểm kê"));

        List<ChiTietKiemKe> dsChiTiet = chiTietKiemKeRepository.findByPhienKiemKe_Id(phien.getId());

        boolean conThieu = dsChiTiet.stream().anyMatch(ct -> CHO_KIEM.equals(ct.getTrangThaiKiemKe()));
        if (conThieu) {
            throw new BusinessException("Không thể chốt sổ! Vẫn còn Pallet đang ở trạng thái 'Chờ kiểm'.");
        }

        List<ChiTietKiemKe> dsLech = dsChiTiet.stream()
                .filter(ct -> LECH.equals(ct.getTrangThaiKiemKe()))
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
                    .laHeThong(true)
                    .ngayGio(LocalDateTime.now())
                    .build();
            phieuDieuChinh = phieuKhoRepository.save(phieuDieuChinh);

            for (ChiTietKiemKe ct : dsLech) {
                int tonThucTe = ct.getTonThucTe() != null ? ct.getTonThucTe() : ct.getTonHeThong();
                int chenhLech = tonThucTe - ct.getTonHeThong();

                TonKho tk = tonKhoRepository
                        .findForUpdate(ct.getMatHang().getId(), phien.getKho().getId())
                        .orElse(null);

                if (tk == null) {
                    if (tonThucTe > 0) {
                        tk = TonKho.builder()
                                .matHang(ct.getMatHang())
                                .kho(phien.getKho())
                                .viTri(ct.getViTri())
                                .soLuongTonKho(tonThucTe)
                                .soLuongDaGiuCho(0)
                                .soLuongToiThieu(0)
                                .updatedAt(LocalDateTime.now())
                                .build();
                        tonKhoRepository.save(tk);
                    }
                } else {
                    if (tonThucTe < tk.getSoLuongDaGiuCho()) {
                        throw new BusinessException("Tồn thực tế của " + ct.getMatHang().getTenMatHang()
                                + " nhỏ hơn số lượng đang giữ chỗ (" + tk.getSoLuongDaGiuCho() + ")");
                    }
                    tk.setSoLuongTonKho(tonThucTe);
                    if (ct.getViTri() != null) {
                        tk.setViTri(ct.getViTri());
                    }
                    tk.setUpdatedAt(LocalDateTime.now());
                    tonKhoRepository.save(tk);
                }

                // Ghi dòng chi tiết để phiếu điều chỉnh có đủ chứng từ đối soát.
                ChiTietPhieuKho chiTiet = ChiTietPhieuKho.builder()
                        .phieuKho(phieuDieuChinh)
                        .matHang(ct.getMatHang())
                        .soLuong(Math.abs(chenhLech))
                        .ghiChu(chenhLech > 0
                                ? "Kiểm kê tăng " + chenhLech
                                : "Kiểm kê giảm " + Math.abs(chenhLech))
                        .build();
                chiTietPhieuKhoRepository.save(chiTiet);
            }
        }

        phien.setTrangThai(DA_HOAN_TAT);
        phien.setNguoiChot(nguoiChot);
        phien.setNgayHoanTat(LocalDate.now());
        phienKiemKeRepository.save(phien);
    }
}
