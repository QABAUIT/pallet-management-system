package com.pallet.backend.service.impl;

import com.pallet.backend.dto.request.NhanVienRequest;
import com.pallet.backend.dto.request.TaoTaiKhoanRequest;
import com.pallet.backend.dto.response.NhanVienResponse;
import com.pallet.backend.dto.response.TaoTaiKhoanResponse;
import com.pallet.backend.entity.BoPhan;
import com.pallet.backend.entity.Kho;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.entity.VaiTro;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.exception.ResourceNotFoundException;
import com.pallet.backend.repository.BoPhanRepository;
import com.pallet.backend.repository.KhoRepository;
import com.pallet.backend.repository.NhanVienRepository;
import com.pallet.backend.repository.PhienDangNhapRepository;
import com.pallet.backend.repository.VaiTroRepository;
import com.pallet.backend.service.NhanVienService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NhanVienServiceImpl implements NhanVienService {

    private final NhanVienRepository nhanVienRepository;
    private final VaiTroRepository vaiTroRepository;
    private final KhoRepository khoRepository;
    private final BoPhanRepository boPhanRepository;
    private final PhienDangNhapRepository phienDangNhapRepository;
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
        return toResponse(timHoacNem(id));
    }

    @Override
    @Transactional
    public TaoTaiKhoanResponse taoTaiKhoan(TaoTaiKhoanRequest req) {
        if (nhanVienRepository.existsByTenDangNhap(req.getTenDangNhap())) {
            throw new BusinessException("Tên đăng nhập đã tồn tại");
        }
        String email = chuanHoaEmail(req.getEmail());
        if (email != null && nhanVienRepository.existsByEmail(email)) {
            throw new BusinessException("Email đã được sử dụng");
        }
        VaiTro vaiTro = vaiTroRepository.findByMaVaiTro(req.getMaVaiTro())
                .orElseThrow(() -> new BusinessException("Vai trò không tồn tại"));

        String maNv = String.format("NV%04d", nhanVienRepository.nextMaNvSeq());

        NhanVien nv = NhanVien.builder()
                .maNv(maNv)
                .tenDangNhap(req.getTenDangNhap())
                .matKhauHash(passwordEncoder.encode(req.getMatKhau()))
                .hoTen(req.getHoTen())
                .email(email)
                .sdt(req.getSdt())
                .ngayVaoLam(req.getNgayVaoLam())
                .vaiTro(vaiTro)
                .trangThai("dang_lam_viec")
                .build();

        nv = nhanVienRepository.save(nv);
        return new TaoTaiKhoanResponse(nv.getId(), nv.getMaNv());
    }

    @Override
    @Transactional
    public NhanVienResponse capNhat(Long id, NhanVienRequest request) {
        NhanVien nv = timHoacNem(id);

        // Kiểm tra email trùng với người khác
        String email = chuanHoaEmail(request.getEmail());
        if (email != null) {
            nhanVienRepository.findByEmail(email).ifPresent(khac -> {
                if (!khac.getId().equals(id)) {
                    throw new BusinessException("Email đã được sử dụng");
                }
            });
        }

        gan(nv, request);
        nv.setEmail(email);

        // Chỉ đổi mật khẩu nếu client chủ động truyền mật khẩu mới
        if (request.getMatKhau() != null && !request.getMatKhau().isBlank()) {
            nv.setMatKhauHash(passwordEncoder.encode(request.getMatKhau()));
        }

        if (request.getTrangThai() != null) {
            nv.setTrangThai(request.getTrangThai());
            if ("da_nghi_viec".equals(request.getTrangThai())) {
                phienDangNhapRepository.deleteByNhanVien_Id(id);
            }
        }

        return toResponse(nhanVienRepository.save(nv));
    }

    @Override
    @Transactional
    public void choNghiViec(Long id) {
        NhanVien nv = timHoacNem(id);
        // XÓA MỀM: đổi trạng thái để giữ khóa ngoại và lịch sử chứng từ
        nv.setTrangThai("da_nghi_viec");
        if (nv.getNgayNghiViec() == null) {
            nv.setNgayNghiViec(LocalDate.now());
        }
        nhanVienRepository.save(nv);
        // Thu hồi mọi phiên đăng nhập để không refresh token được nữa
        phienDangNhapRepository.deleteByNhanVien_Id(id);
    }

    // ---------- helper ----------

    private NhanVien timHoacNem(Long id) {
        return nhanVienRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Nhân viên", id));
    }

    private String chuanHoaEmail(String email) {
        return (email == null || email.isBlank()) ? null : email.trim();
    }

    private void gan(NhanVien nv, NhanVienRequest r) {
        nv.setHoTen(r.getHoTen());
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

        if (r.getVaiTroId() != null) {
            VaiTro vaiTro = vaiTroRepository.findById(r.getVaiTroId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Vai trò", r.getVaiTroId()));
            nv.setVaiTro(vaiTro);
        }

        if (r.getKhoId() != null) {
            Kho kho = khoRepository.findById(r.getKhoId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Kho", r.getKhoId()));
            nv.setKho(kho);
        } else {
            nv.setKho(null);
        }

        if (r.getBoPhanId() != null) {
            BoPhan boPhan = boPhanRepository.findById(r.getBoPhanId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Bộ phận", r.getBoPhanId()));
            nv.setBoPhan(boPhan);
        } else {
            nv.setBoPhan(null);
        }
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