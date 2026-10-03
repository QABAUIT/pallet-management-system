package com.pallet.backend.service.impl;

import com.pallet.backend.dto.request.NhanVienRequest;
import com.pallet.backend.dto.response.NhanVienResponse;
import com.pallet.backend.entity.BoPhan;
import com.pallet.backend.entity.Kho;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.entity.VaiTro;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.exception.ResourceNotFoundException;
import com.pallet.backend.repository.BoPhanRepository;
import com.pallet.backend.repository.KhoRepository;
import com.pallet.backend.repository.NhanVienRepository;
import com.pallet.backend.repository.VaiTroRepository;
import com.pallet.backend.service.NhanVienService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NhanVienServiceImpl implements NhanVienService {

    private final NhanVienRepository nhanVienRepository;
    private final VaiTroRepository vaiTroRepository;
    private final KhoRepository khoRepository;
    private final BoPhanRepository boPhanRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<NhanVienResponse> layDanhSach(String trangThai) {
        List<NhanVien> list = (trangThai != null)
                ? nhanVienRepository.findByTrangThai(trangThai)
                : nhanVienRepository.findAll();
        return list.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public NhanVienResponse layChiTiet(Long id) {
        NhanVien nv = timHoacNem(id);
        return toResponse(nv);
    }

    @Override
    @Transactional
    public NhanVienResponse taoMoi(NhanVienRequest request) {
        // Kiểm tra trùng tên đăng nhập
        if (nhanVienRepository.existsByTenDangNhap(request.getTenDangNhap())) {
            throw new BusinessException("Tên đăng nhập '" + request.getTenDangNhap() + "' đã tồn tại");
        }

        // Bắt buộc nhập mật khẩu khi tạo mới tài khoản
        if (request.getMatKhau() == null || request.getMatKhau().isBlank()) {
            throw new BusinessException("Mật khẩu ban đầu không được để trống khi tạo mới nhân viên");
        }

        NhanVien nv = new NhanVien();
        gan(nv, request);

        // Sinh mã nhân viên tự động: NV-001, NV-002...
        nv.setMaNv(sinhMaNhanVien());
        nv.setTenDangNhap(request.getTenDangNhap());
        nv.setMatKhauHash(passwordEncoder.encode(request.getMatKhau()));

        // Trạng thái mặc định theo DB schema: 'dang_lam_viec'
        nv.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : "dang_lam_viec");
        nv.setCreatedAt(LocalDateTime.now());
        nv.setUpdatedAt(LocalDateTime.now());

        nv = nhanVienRepository.save(nv);
        return toResponse(nv);
    }

    @Override
    @Transactional
    public NhanVienResponse capNhat(Long id, NhanVienRequest request) {
        NhanVien nv = timHoacNem(id);

        // Cập nhật thông tin cơ bản và quan hệ tổ chức
        gan(nv, request);

        // Chỉ đổi mật khẩu nếu client chủ động truyền chuỗi mật khẩu mới
        if (request.getMatKhau() != null && !request.getMatKhau().isBlank()) {
            nv.setMatKhauHash(passwordEncoder.encode(request.getMatKhau()));
        }

        if (request.getTrangThai() != null) {
            nv.setTrangThai(request.getTrangThai());
        }

        nv.setUpdatedAt(LocalDateTime.now());
        return toResponse(nhanVienRepository.save(nv));
    }

    @Override
    @Transactional
    public void choNghiViec(Long id) {
        NhanVien nv = timHoacNem(id);
        // XÓA MỀM: Chuyển cờ trạng thái để giữ nguyên khóa ngoại và lịch sử thao tác
        // phiếu kho/chứng từ
        nv.setTrangThai("da_nghi_viec");
        nv.setUpdatedAt(LocalDateTime.now());
        nhanVienRepository.save(nv);
    }

    // ---------- helper ----------

    private NhanVien timHoacNem(Long id) {
        return nhanVienRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Nhân viên", id));
    }

    private void gan(NhanVien nv, NhanVienRequest r) {
        nv.setHoTen(r.getHoTen());
        nv.setEmail(r.getEmail());
        nv.setSdt(r.getSdt());
        nv.setNgaySinh(r.getNgaySinh());
        nv.setGioiTinh(r.getGioiTinh());
        nv.setDiaChi(r.getDiaChi());
        nv.setAnhDaiDien(r.getAnhDaiDien());
        nv.setChucVu(r.getChucVu());
        nv.setLoaiHopDong(r.getLoaiHopDong());
        nv.setSoHopDong(r.getSoHopDong());
        nv.setNgayVaoLam(r.getNgayVaoLam());
        nv.setNgayNghiViec(r.getNgayNghiViec());

        // Gán Vai trò (bắt buộc)
        if (r.getVaiTroId() != null) {
            VaiTro vaiTro = vaiTroRepository.findById(r.getVaiTroId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Vai trò", r.getVaiTroId()));
            nv.setVaiTro(vaiTro);
        }

        // Gán Kho trực thuộc (nullable)
        if (r.getKhoId() != null) {
            Kho kho = khoRepository.findById(r.getKhoId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Kho", r.getKhoId()));
            nv.setKho(kho);
        } else {
            nv.setKho(null);
        }

        // Gán Bộ phận (nullable)
        if (r.getBoPhanId() != null) {
            BoPhan boPhan = boPhanRepository.findById(r.getBoPhanId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Bộ phận", r.getBoPhanId()));
            nv.setBoPhan(boPhan);
        } else {
            nv.setBoPhan(null);
        }
    }

    private String sinhMaNhanVien() {
        long stt = nhanVienRepository.count() + 1;
        return String.format("NV-%03d", stt);
    }

    private NhanVienResponse toResponse(NhanVien nv) {
        return NhanVienResponse.builder()
                .id(nv.getId())
                .maNv(nv.getMaNv())
                .hoTen(nv.getHoTen())
                .email(nv.getEmail())
                .sdt(nv.getSdt())
                .ngaySinh(nv.getNgaySinh())
                .gioiTinh(nv.getGioiTinh())
                .diaChi(nv.getDiaChi())
                .anhDaiDien(nv.getAnhDaiDien())
                .vaiTroId(nv.getVaiTro() != null ? nv.getVaiTro().getId() : null)
                .tenVaiTro(nv.getVaiTro() != null ? nv.getVaiTro().getTenVaiTro() : null)
                .khoId(nv.getKho() != null ? nv.getKho().getId() : null)
                .tenKho(nv.getKho() != null ? nv.getKho().getTenKho() : null)
                .boPhanId(nv.getBoPhan() != null ? nv.getBoPhan().getId() : null)
                .tenBoPhan(nv.getBoPhan() != null ? nv.getBoPhan().getTenBoPhan() : null)
                .chucVu(nv.getChucVu())
                .loaiHopDong(nv.getLoaiHopDong())
                .soHopDong(nv.getSoHopDong())
                .tenDangNhap(nv.getTenDangNhap())
                .ngayVaoLam(nv.getNgayVaoLam())
                .ngayNghiViec(nv.getNgayNghiViec())
                .trangThai(nv.getTrangThai())
                .createdAt(nv.getCreatedAt())
                .updatedAt(nv.getUpdatedAt())
                .build();
    }
}