package com.pallet.backend.service;

import com.pallet.backend.dto.request.ChiTietPhieuXuatItemRequest;
import com.pallet.backend.dto.request.TaoPhieuXuatRequest;
import com.pallet.backend.dto.response.PhieuXuatResponse;
import com.pallet.backend.entity.Kho;
import com.pallet.backend.entity.MatHang;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.entity.PhieuKho;
import com.pallet.backend.entity.VaiTro;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.repository.ChiTietPhieuKhoRepository;
import com.pallet.backend.repository.KhoRepository;
import com.pallet.backend.repository.LoHangRepository;
import com.pallet.backend.repository.MatHangRepository;
import com.pallet.backend.repository.NhanVienRepository;
import com.pallet.backend.repository.PhieuKhoRepository;
import com.pallet.backend.service.impl.PhieuXuatServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhieuXuatServiceImplTest {

    private static final String MSG_KHONG_DUOC_KHO =
            "Bạn chỉ được thao tác với dữ liệu của kho được gán cho mình";

    @Mock
    private PhieuKhoRepository phieuKhoRepository;
    @Mock
    private ChiTietPhieuKhoRepository chiTietPhieuKhoRepository;
    @Mock
    private MatHangRepository matHangRepository;
    @Mock
    private KhoRepository khoRepository;
    @Mock
    private NhanVienRepository nhanVienRepository;
    @Mock
    private LoHangRepository loHangRepository;
    @Mock
    private TonKhoNghiepVu tonKhoNghiepVu;
    @Mock
    private ThongBaoTriggerService thongBaoTrigger;

    @Spy
    private PhanQuyenKho phanQuyenKho = new PhanQuyenKho();

    @InjectMocks
    private PhieuXuatServiceImpl phieuXuatService;

    @Test
    void nvkhoKhongDuocTaoPhieuXuatKhoKhac() {
        Kho khoB = kho(2L, "Kho B");
        NhanVien nhanVien = nhanVien("nv-a", "NVKHO", kho(1L, "Kho A"));

        when(khoRepository.findById(2L)).thenReturn(Optional.of(khoB));
        when(nhanVienRepository.findByTenDangNhap("nv-a")).thenReturn(Optional.of(nhanVien));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> phieuXuatService.taoPhieuXuat(taoPhieuXuatRequest(2L), "nv-a"));

        assertEquals(MSG_KHONG_DUOC_KHO, ex.getMessage());
        verifyNoInteractions(phieuKhoRepository);
    }

    @Test
    void gdDuocTaoPhieuXuatKhoKhac() {
        Kho khoB = kho(2L, "Kho B");
        NhanVien giamDoc = nhanVien("gd", "GD", null);
        MatHang matHang = matHang(5L);

        when(khoRepository.findById(2L)).thenReturn(Optional.of(khoB));
        when(nhanVienRepository.findByTenDangNhap("gd")).thenReturn(Optional.of(giamDoc));
        when(phieuKhoRepository.getMaChungTu("PX", "PX-")).thenReturn("PX-1");
        when(phieuKhoRepository.save(any(PhieuKho.class))).thenAnswer(inv -> {
            PhieuKho phieu = inv.getArgument(0);
            phieu.setId(1L);
            return phieu;
        });
        when(matHangRepository.findById(5L)).thenReturn(Optional.of(matHang));
        when(chiTietPhieuKhoRepository.findByPhieuKho_Id(1L)).thenReturn(List.of());

        PhieuXuatResponse response = phieuXuatService.taoPhieuXuat(taoPhieuXuatRequest(2L), "gd");

        assertEquals("PX-1", response.getMaPhieuXuat());
        assertEquals("cho_xu_ly", response.getTrangThai());
    }

    @Test
    void nvkhoKhongDuocCapNhatPhieuXuatKhoKhac() {
        PhieuKho phieu = phieuXuat(10L, kho(2L, "Kho B"), "cho_xu_ly");
        NhanVien nhanVien = nhanVien("nv-a", "NVKHO", kho(1L, "Kho A"));

        when(phieuKhoRepository.findForUpdate(10L)).thenReturn(Optional.of(phieu));
        when(nhanVienRepository.findByTenDangNhap("nv-a")).thenReturn(Optional.of(nhanVien));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> phieuXuatService.capNhatPhieuXuat(10L, taoPhieuXuatRequest(2L), "nv-a"));

        assertEquals(MSG_KHONG_DUOC_KHO, ex.getMessage());
        verifyNoInteractions(chiTietPhieuKhoRepository);
    }

    @Test
    void gdDuocCapNhatPhieuXuatKhoKhac() {
        PhieuKho phieu = phieuXuat(10L, kho(2L, "Kho B"), "cho_xu_ly");
        NhanVien giamDoc = nhanVien("gd", "GD", null);

        when(phieuKhoRepository.findForUpdate(10L)).thenReturn(Optional.of(phieu));
        when(nhanVienRepository.findByTenDangNhap("gd")).thenReturn(Optional.of(giamDoc));
        when(chiTietPhieuKhoRepository.findByPhieuKho_Id(10L)).thenReturn(List.of());

        TaoPhieuXuatRequest request = new TaoPhieuXuatRequest();
        request.setTrangThai("da_huy");

        PhieuXuatResponse response = phieuXuatService.capNhatPhieuXuat(10L, request, "gd");

        assertEquals("da_huy", response.getTrangThai());
    }

    @Test
    void phieuXuatKhoNullBiTuChoiVoiNvkho() {
        PhieuKho phieu = phieuXuat(10L, null, "cho_xu_ly");
        NhanVien nhanVien = nhanVien("nv-a", "NVKHO", kho(1L, "Kho A"));

        when(phieuKhoRepository.findForUpdate(10L)).thenReturn(Optional.of(phieu));
        when(nhanVienRepository.findByTenDangNhap("nv-a")).thenReturn(Optional.of(nhanVien));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> phieuXuatService.capNhatPhieuXuat(10L, taoPhieuXuatRequest(2L), "nv-a"));

        assertEquals(MSG_KHONG_DUOC_KHO, ex.getMessage());
    }

    @Test
    void phieuXuatKhoNullGdDuocHuy() {
        PhieuKho phieu = phieuXuat(10L, null, "cho_xu_ly");
        NhanVien giamDoc = nhanVien("gd", "GD", null);

        when(phieuKhoRepository.findForUpdate(10L)).thenReturn(Optional.of(phieu));
        when(nhanVienRepository.findByTenDangNhap("gd")).thenReturn(Optional.of(giamDoc));
        when(chiTietPhieuKhoRepository.findByPhieuKho_Id(10L)).thenReturn(List.of());

        TaoPhieuXuatRequest request = new TaoPhieuXuatRequest();
        request.setTrangThai("da_huy");

        PhieuXuatResponse response = phieuXuatService.capNhatPhieuXuat(10L, request, "gd");

        assertEquals("da_huy", response.getTrangThai());
    }

    private TaoPhieuXuatRequest taoPhieuXuatRequest(Long khoId) {
        ChiTietPhieuXuatItemRequest dongHang = new ChiTietPhieuXuatItemRequest();
        dongHang.setMatHangId(5L);
        dongHang.setSoLuong(10);

        TaoPhieuXuatRequest request = new TaoPhieuXuatRequest();
        request.setKhoId(khoId);
        request.setLoaiXuat("Xuất bán");
        request.setLyDoXuat("Đơn hàng");
        request.setChiTiet(List.of(dongHang));
        return request;
    }

    private Kho kho(Long id, String tenKho) {
        return Kho.builder().id(id).tenKho(tenKho).build();
    }

    private NhanVien nhanVien(String tenDangNhap, String maVaiTro, Kho kho) {
        return NhanVien.builder()
                .id(99L)
                .tenDangNhap(tenDangNhap)
                .hoTen(tenDangNhap)
                .vaiTro(VaiTro.builder().maVaiTro(maVaiTro).build())
                .kho(kho)
                .build();
    }

    private MatHang matHang(Long id) {
        return MatHang.builder()
                .id(id)
                .maMatHang("MH-" + id)
                .tenMatHang("Pallet " + id)
                .loaiMatHang("pallet")
                .build();
    }

    private PhieuKho phieuXuat(Long id, Kho kho, String trangThai) {
        return PhieuKho.builder()
                .id(id)
                .maPhieu("PX-" + id)
                .loaiPhieu("xuat_kho")
                .kho(kho)
                .trangThai(trangThai)
                .build();
    }
}
