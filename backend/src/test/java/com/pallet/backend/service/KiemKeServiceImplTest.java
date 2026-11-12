package com.pallet.backend.service;

import com.pallet.backend.dto.request.CapNhatChiTietKiemKeRequest;
import com.pallet.backend.dto.request.TaoPhienKiemKeRequest;
import com.pallet.backend.entity.ChiTietKiemKe;
import com.pallet.backend.entity.Kho;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.entity.PhienKiemKe;
import com.pallet.backend.entity.VaiTro;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.repository.ChiTietKiemKeRepository;
import com.pallet.backend.repository.ChiTietPhieuKhoRepository;
import com.pallet.backend.repository.KhoRepository;
import com.pallet.backend.repository.NhanVienRepository;
import com.pallet.backend.repository.PhieuKhoRepository;
import com.pallet.backend.repository.PhienKiemKeRepository;
import com.pallet.backend.repository.TonKhoRepository;
import com.pallet.backend.service.impl.KiemKeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KiemKeServiceImplTest {

    private static final String MSG_KHONG_DUOC_KHO =
            "Bạn chỉ được thao tác với dữ liệu của kho được gán cho mình";

    @Mock
    private PhienKiemKeRepository phienKiemKeRepository;
    @Mock
    private ChiTietKiemKeRepository chiTietKiemKeRepository;
    @Mock
    private ChiTietPhieuKhoRepository chiTietPhieuKhoRepository;
    @Mock
    private TonKhoRepository tonKhoRepository;
    @Mock
    private KhoRepository khoRepository;
    @Mock
    private NhanVienRepository nhanVienRepository;
    @Mock
    private PhieuKhoRepository phieuKhoRepository;

    @Spy
    private PhanQuyenKho phanQuyenKho = new PhanQuyenKho();

    @InjectMocks
    private KiemKeServiceImpl kiemKeService;

    @Test
    void nguoiChotPhienKiemKeLaNguoiDangDangNhap() {
        Kho kho = kho(1L, "Kho A");
        PhienKiemKe phien = phien(10L, kho, "dang_dien_ra");
        NhanVien nguoiDangNhap = nhanVien("nvchot", "NVKHO", kho);

        when(phienKiemKeRepository.findForUpdate(10L)).thenReturn(Optional.of(phien));
        when(nhanVienRepository.findByTenDangNhap("nvchot")).thenReturn(Optional.of(nguoiDangNhap));
        when(chiTietKiemKeRepository.findByPhienKiemKe_Id(10L)).thenReturn(List.of());

        kiemKeService.hoanTatKiemKe(10L, "nvchot");

        assertSame(nguoiDangNhap, phien.getNguoiChot());
        assertEquals("da_hoan_tat", phien.getTrangThai());
        verify(nhanVienRepository).findByTenDangNhap("nvchot");
        verify(nhanVienRepository, never()).findById(anyLong());
        verify(phienKiemKeRepository).save(phien);
    }

    @Test
    void nvkhoKhongDuocTaoPhienKiemKeKhoKhac() {
        Kho khoB = kho(2L, "Kho B");
        NhanVien nhanVien = nhanVien("nv-a", "NVKHO", kho(1L, "Kho A"));

        when(khoRepository.findById(2L)).thenReturn(Optional.of(khoB));
        when(nhanVienRepository.findByTenDangNhap("nv-a")).thenReturn(Optional.of(nhanVien));

        TaoPhienKiemKeRequest request = new TaoPhienKiemKeRequest();
        request.setKhoId(2L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> kiemKeService.taoPhienKiemKe(request, "nv-a"));

        assertEquals(MSG_KHONG_DUOC_KHO, ex.getMessage());
        verifyNoInteractions(phienKiemKeRepository);
    }

    @Test
    void gdDuocTaoPhienKiemKeKhoKhac() {
        Kho khoB = kho(2L, "Kho B");
        NhanVien giamDoc = nhanVien("gd", "GD", null);

        when(khoRepository.findById(2L)).thenReturn(Optional.of(khoB));
        when(nhanVienRepository.findByTenDangNhap("gd")).thenReturn(Optional.of(giamDoc));
        when(phienKiemKeRepository.existsByKho_IdAndTrangThai(2L, "dang_dien_ra")).thenReturn(false);
        when(phieuKhoRepository.getMaChungTu("PKK", "PKK-")).thenReturn("PKK-1");
        when(phienKiemKeRepository.save(any(PhienKiemKe.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tonKhoRepository.findByKho_Id(2L)).thenReturn(List.of());

        TaoPhienKiemKeRequest request = new TaoPhienKiemKeRequest();
        request.setKhoId(2L);

        PhienKiemKe phien = kiemKeService.taoPhienKiemKe(request, "gd");

        assertSame(khoB, phien.getKho());
        assertEquals("PKK-1", phien.getMaPhien());
        assertEquals("dang_dien_ra", phien.getTrangThai());
    }

    @Test
    void nvkhoKhongDuocCapNhatChiTietPhienKhoKhac() {
        PhienKiemKe phien = phien(10L, kho(2L, "Kho B"), "dang_dien_ra");
        ChiTietKiemKe chiTiet = chiTiet(5L, phien);
        NhanVien nhanVien = nhanVien("nv-a", "NVKHO", kho(1L, "Kho A"));

        when(chiTietKiemKeRepository.findById(5L)).thenReturn(Optional.of(chiTiet));
        when(nhanVienRepository.findByTenDangNhap("nv-a")).thenReturn(Optional.of(nhanVien));

        CapNhatChiTietKiemKeRequest request = new CapNhatChiTietKiemKeRequest();
        request.setTonThucTe(5);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> kiemKeService.capNhatChiTiet(5L, request, "nv-a"));

        assertEquals(MSG_KHONG_DUOC_KHO, ex.getMessage());
        verify(chiTietKiemKeRepository, never()).save(any());
    }

    @Test
    void nvkhoKhongDuocCapNhatChiTietKhiPhienKhoNull() {
        PhienKiemKe phien = phien(10L, null, "dang_dien_ra");
        ChiTietKiemKe chiTiet = chiTiet(5L, phien);
        NhanVien nhanVien = nhanVien("nv-a", "NVKHO", kho(1L, "Kho A"));

        when(chiTietKiemKeRepository.findById(5L)).thenReturn(Optional.of(chiTiet));
        when(nhanVienRepository.findByTenDangNhap("nv-a")).thenReturn(Optional.of(nhanVien));

        CapNhatChiTietKiemKeRequest request = new CapNhatChiTietKiemKeRequest();
        request.setTonThucTe(5);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> kiemKeService.capNhatChiTiet(5L, request, "nv-a"));

        assertEquals(MSG_KHONG_DUOC_KHO, ex.getMessage());
        verify(chiTietKiemKeRepository, never()).save(any());
    }

    @Test
    void nvkhoKhongDuocHoanTatPhienKhoKhac() {
        PhienKiemKe phien = phien(10L, kho(2L, "Kho B"), "dang_dien_ra");
        NhanVien nhanVien = nhanVien("nv-a", "NVKHO", kho(1L, "Kho A"));

        when(phienKiemKeRepository.findForUpdate(10L)).thenReturn(Optional.of(phien));
        when(nhanVienRepository.findByTenDangNhap("nv-a")).thenReturn(Optional.of(nhanVien));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> kiemKeService.hoanTatKiemKe(10L, "nv-a"));

        assertEquals(MSG_KHONG_DUOC_KHO, ex.getMessage());
        verifyNoInteractions(chiTietKiemKeRepository);
    }

    @Test
    void gdDuocHoanTatPhienKhoKhac() {
        PhienKiemKe phien = phien(10L, kho(2L, "Kho B"), "dang_dien_ra");
        NhanVien giamDoc = nhanVien("gd", "GD", null);

        when(phienKiemKeRepository.findForUpdate(10L)).thenReturn(Optional.of(phien));
        when(nhanVienRepository.findByTenDangNhap("gd")).thenReturn(Optional.of(giamDoc));
        when(chiTietKiemKeRepository.findByPhienKiemKe_Id(10L)).thenReturn(List.of());

        kiemKeService.hoanTatKiemKe(10L, "gd");

        assertEquals("da_hoan_tat", phien.getTrangThai());
        assertSame(giamDoc, phien.getNguoiChot());
    }

    @Test
    void phienKhoNullBiTuChoiVoiNvkho() {
        PhienKiemKe phien = phien(10L, null, "dang_dien_ra");
        NhanVien nhanVien = nhanVien("nv-a", "NVKHO", kho(1L, "Kho A"));

        when(phienKiemKeRepository.findForUpdate(10L)).thenReturn(Optional.of(phien));
        when(nhanVienRepository.findByTenDangNhap("nv-a")).thenReturn(Optional.of(nhanVien));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> kiemKeService.hoanTatKiemKe(10L, "nv-a"));

        assertEquals(MSG_KHONG_DUOC_KHO, ex.getMessage());
    }

    @Test
    void phienKhoNullGdDuocHoanTat() {
        PhienKiemKe phien = phien(10L, null, "dang_dien_ra");
        NhanVien giamDoc = nhanVien("gd", "GD", null);

        when(phienKiemKeRepository.findForUpdate(10L)).thenReturn(Optional.of(phien));
        when(nhanVienRepository.findByTenDangNhap("gd")).thenReturn(Optional.of(giamDoc));
        when(chiTietKiemKeRepository.findByPhienKiemKe_Id(10L)).thenReturn(List.of());

        kiemKeService.hoanTatKiemKe(10L, "gd");

        assertEquals("da_hoan_tat", phien.getTrangThai());
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

    private PhienKiemKe phien(Long id, Kho kho, String trangThai) {
        return PhienKiemKe.builder()
                .id(id)
                .kho(kho)
                .trangThai(trangThai)
                .build();
    }

    private ChiTietKiemKe chiTiet(Long id, PhienKiemKe phien) {
        return ChiTietKiemKe.builder()
                .id(id)
                .phienKiemKe(phien)
                .tonHeThong(10)
                .trangThaiKiemKe("cho_kiem")
                .build();
    }
}
