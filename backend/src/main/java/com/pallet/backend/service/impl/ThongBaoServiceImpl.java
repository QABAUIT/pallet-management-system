package com.pallet.backend.service.impl;

import com.pallet.backend.dto.response.ThongBaoResponse;
import com.pallet.backend.dto.response.ThongBaoThongKeResponse;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.entity.ThongBao;
import com.pallet.backend.entity.VaiTro;
import com.pallet.backend.exception.ResourceNotFoundException;
import com.pallet.backend.repository.NhanVienRepository;
import com.pallet.backend.repository.ThongBaoRepository;
import com.pallet.backend.repository.VaiTroRepository;
import com.pallet.backend.service.ThongBaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ThongBaoServiceImpl implements ThongBaoService {

    private final ThongBaoRepository thongBaoRepository;
    private final NhanVienRepository nhanVienRepository;
    private final VaiTroRepository vaiTroRepository;

    private NhanVien getNhanVienByUsername(String username) {
        if (username == null || username.isBlank()) return null;
        return nhanVienRepository.findByTenDangNhap(username).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ThongBaoResponse> layDanhSach(Boolean daDoc, String loaiThongBao, String username) {
        NhanVien nv = getNhanVienByUsername(username);

        Specification<ThongBao> spec = (root, query, cb) -> {
            // Điều kiện hiển thị theo diagram:
            // (1) nguoi_nhan_id = nv.id           → gửi đích danh cho cá nhân
            // (2) vai_tro_nhan_id = nv.vai_tro_id → gửi cho vai trò của người dùng
            // (3) nguoi_nhan IS NULL               → broadcast toàn hệ thống
            //     AND vai_tro_nhan IS NULL
            var predicate = cb.conjunction();

            if (nv != null) {
                Long nvId = nv.getId();
                VaiTro vt = nv.getVaiTro();
                Long vaiTroId = vt != null ? vt.getId() : null;
                String maVaiTro = vt != null ? vt.getMaVaiTro() : "";
                Short capDuyet = vt != null ? vt.getCapDuyet() : null;

                // Giám đốc (GD) và Phó giám đốc (PGD) có cấp duyệt >= 2 -> Nhận và xem TẤT CẢ thông báo
                boolean isLanhDao = (capDuyet != null && capDuyet >= 2) 
                        || "GD".equalsIgnoreCase(maVaiTro) 
                        || "PGD".equalsIgnoreCase(maVaiTro);

                if (!isLanhDao) {
                    // (1) Gửi trực tiếp cho cá nhân
                    var isNguoiNhan = cb.equal(root.get("nguoiNhan").get("id"), nvId);

                    // (3) Broadcast – cả hai FK đều null
                    var isBroadcast = cb.and(
                            cb.isNull(root.get("nguoiNhan")),
                            cb.isNull(root.get("vaiTroNhan"))
                    );

                    if (vaiTroId != null) {
                        // (2) Gửi cho vai trò – vai_tro_nhan_id khớp vai trò user
                        var isVaiTroTarget = cb.equal(root.get("vaiTroNhan").get("id"), vaiTroId);
                        predicate = cb.and(predicate, cb.or(isNguoiNhan, isVaiTroTarget, isBroadcast));
                    } else {
                        predicate = cb.and(predicate, cb.or(isNguoiNhan, isBroadcast));
                    }
                }
            }

            if (daDoc != null) {
                predicate = cb.and(predicate, cb.equal(root.get("daDoc"), daDoc));
            }

            if (loaiThongBao != null && !loaiThongBao.isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("loaiThongBao"), loaiThongBao));
            }

            return predicate;
        };

        return thongBaoRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "thoiGianTao"))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ThongBaoThongKeResponse thongKe(String username) {
        List<ThongBaoResponse> all = layDanhSach(null, null, username);
        long tongSo  = all.size();
        long chuaDoc = all.stream().filter(n -> Boolean.FALSE.equals(n.getDaDoc())).count();
        long daDoc   = tongSo - chuaDoc;

        return ThongBaoThongKeResponse.builder()
                .tongSo(tongSo)
                .chuaDoc(chuaDoc)
                .daDoc(daDoc)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ThongBaoResponse layChiTiet(Long id) {
        ThongBao tb = thongBaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo id=" + id));
        return toResponse(tb);
    }

    @Override
    @Transactional
    public ThongBaoResponse danhDauDaDoc(Long id, Boolean daDoc) {
        ThongBao tb = thongBaoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông báo id=" + id));
        tb.setDaDoc(daDoc != null ? daDoc : true);
        return toResponse(thongBaoRepository.save(tb));
    }

    @Override
    @Transactional
    public void danhDauTatCaDaDoc(String username) {
        NhanVien nv = getNhanVienByUsername(username);
        if (nv != null) {
            Long nvId = nv.getId();
            Long vaiTroId = nv.getVaiTro() != null ? nv.getVaiTro().getId() : null;
            thongBaoRepository.danhDauTatCaDaDoc(nvId, vaiTroId);
        } else {
            // Đánh dấu tất cả trong DB
            List<ThongBao> ds = thongBaoRepository.findAll();
            ds.forEach(t -> t.setDaDoc(true));
            thongBaoRepository.saveAll(ds);
        }
    }

    @Override
    @Transactional
    public void xoa(Long id) {
        if (!thongBaoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy thông báo id=" + id);
        }
        thongBaoRepository.deleteById(id);
    }

    private ThongBaoResponse toResponse(ThongBao t) {
        return ThongBaoResponse.builder()
                .id(t.getId())
                .loaiThongBao(t.getLoaiThongBao())
                .tieuDe(t.getTieuDe())
                .noiDung(t.getNoiDung())
                .nguoiNhanId(t.getNguoiNhan() != null ? t.getNguoiNhan().getId() : null)
                .tenNguoiNhan(t.getNguoiNhan() != null ? t.getNguoiNhan().getHoTen() : null)
                .vaiTroNhanId(t.getVaiTroNhan() != null ? t.getVaiTroNhan().getId() : null)
                .tenVaiTroNhan(t.getVaiTroNhan() != null ? t.getVaiTroNhan().getTenVaiTro() : null)
                .doiTuongLoai(t.getDoiTuongLoai())
                .doiTuongId(t.getDoiTuongId())
                .daDoc(t.getDaDoc())
                .thoiGianTao(t.getThoiGianTao())
                .build();
    }
}
