package com.pallet.backend.service;

import com.pallet.backend.dto.request.ChiTietPhieuNhapItemRequest;
import com.pallet.backend.dto.request.TaoPhieuNhapRequest;
import com.pallet.backend.dto.request.XepViTriRequest;
import com.pallet.backend.dto.response.PhieuNhapResponse;
import com.pallet.backend.entity.ChiTietPhieuKho;
import com.pallet.backend.entity.Kho;
import com.pallet.backend.entity.LoHang;
import com.pallet.backend.entity.MatHang;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.entity.PhieuKho;
import com.pallet.backend.entity.TonKho;
import com.pallet.backend.entity.VaiTro;
import com.pallet.backend.entity.ViTriLuuTru;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.repository.ChiTietPhieuKhoRepository;
import com.pallet.backend.repository.KhoRepository;
import com.pallet.backend.repository.LoHangRepository;
import com.pallet.backend.repository.MatHangRepository;
import com.pallet.backend.repository.NhaCungCapRepository;
import com.pallet.backend.repository.NhanVienRepository;
import com.pallet.backend.repository.PhieuKhoRepository;
import com.pallet.backend.repository.TonKhoRepository;
import com.pallet.backend.repository.ViTriLuuTruRepository;
import com.pallet.backend.service.impl.PhieuNhapServiceImpl;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PhieuNhapServiceImplTest {

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
    private NhaCungCapRepository nccRepository;
    @Mock
    private NhanVienRepository nhanVienRepository;
    @Mock
    private LoHangRepository loHangRepository;
    @Mock
    private TonKhoRepository tonKhoRepository;
    @Mock
    private ViTriLuuTruRepository viTriLuuTruRepository;
    @Mock
    private TonKhoNghiepVu tonKhoNghiepVu;
    @Mock
    private ThongBaoTriggerService thongBaoTrigger;

    @Spy
    private PhanQuyenKho phanQuyenKho = new PhanQuyenKho();

    @InjectMocks
    private PhieuNhapServiceImpl phieuNhapService;

    @Test
    void phieuNhapTrungMatHangBiTuChoi() {
        MatHang matHang = matHang(5L, "Pallet gỗ");
        when(matHangRepository.findById(5L)).thenReturn(Optional.of(matHang));

        TaoPhieuNhapRequest request = new TaoPhieuNhapRequest();
        request.setKhoId(1L);
        request.setChiTiet(List.of(
                dongHang(5L, 10),
                dongHang(5L, 20)));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> phieuNhapService.taoPhieuNhap(request, "nv-bat-ky"));

        assertTrue(ex.getMessage().contains("Pallet gỗ"));
        assertTrue(ex.getMessage().contains("xuất hiện 2 lần trong phiếu nhập"));
        assertTrue(ex.getMessage().contains("hãy gộp thành 1 dòng"));
        verifyNoInteractions(phieuKhoRepository);
    }

    @Test
    void nvkhoKhongDuocTaoPhieuNhapKhoKhac() {
        Kho khoB = kho(2L, "Kho B");
        NhanVien nhanVien = nhanVien("nv-a", "NVKHO", kho(1L, "Kho A"));

        when(khoRepository.findById(2L)).thenReturn(Optional.of(khoB));
        when(nhanVienRepository.findByTenDangNhap("nv-a")).thenReturn(Optional.of(nhanVien));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> phieuNhapService.taoPhieuNhap(taoPhieuNhapRequest(2L), "nv-a"));

        assertEquals(MSG_KHONG_DUOC_KHO, ex.getMessage());
        verifyNoInteractions(phieuKhoRepository);
    }

    @Test
    void gdDuocTaoPhieuNhapKhoKhac() {
        Kho khoB = kho(2L, "Kho B");
        NhanVien giamDoc = nhanVien("gd", "GD", null);
        MatHang matHang = matHang(5L, "Pallet gỗ");

        when(khoRepository.findById(2L)).thenReturn(Optional.of(khoB));
        when(nhanVienRepository.findByTenDangNhap("gd")).thenReturn(Optional.of(giamDoc));
        when(phieuKhoRepository.getMaChungTu("PN", "PN-")).thenReturn("PN-1");
        when(phieuKhoRepository.save(any(PhieuKho.class))).thenAnswer(inv -> {
            PhieuKho phieu = inv.getArgument(0);
            phieu.setId(1L);
            return phieu;
        });
        when(matHangRepository.findById(5L)).thenReturn(Optional.of(matHang));
        when(phieuKhoRepository.getMaChungTu("LO", "LO-")).thenReturn("LO-1");
        when(loHangRepository.save(any(LoHang.class))).thenAnswer(inv -> inv.getArgument(0));
        when(chiTietPhieuKhoRepository.findByPhieuKho_Id(1L)).thenReturn(List.of());

        PhieuNhapResponse response = phieuNhapService.taoPhieuNhap(taoPhieuNhapRequest(2L), "gd");

        assertEquals("PN-1", response.getMaPhieu());
        assertEquals("cho_xu_ly", response.getTrangThai());
    }

    @Test
    void nvkhoKhongDuocCapNhatPhieuNhapKhoKhac() {
        PhieuKho phieu = phieuNhap(10L, kho(2L, "Kho B"), "cho_xu_ly");
        NhanVien nhanVien = nhanVien("nv-a", "NVKHO", kho(1L, "Kho A"));

        when(phieuKhoRepository.findForUpdate(10L)).thenReturn(Optional.of(phieu));
        when(nhanVienRepository.findByTenDangNhap("nv-a")).thenReturn(Optional.of(nhanVien));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> phieuNhapService.capNhatPhieuNhap(10L, taoPhieuNhapRequest(2L), "nv-a"));

        assertEquals(MSG_KHONG_DUOC_KHO, ex.getMessage());
        verifyNoInteractions(chiTietPhieuKhoRepository);
    }

    @Test
    void gdDuocCapNhatPhieuNhapKhoKhac() {
        PhieuKho phieu = phieuNhap(10L, kho(2L, "Kho B"), "cho_xu_ly");
        NhanVien giamDoc = nhanVien("gd", "GD", null);

        when(phieuKhoRepository.findForUpdate(10L)).thenReturn(Optional.of(phieu));
        when(nhanVienRepository.findByTenDangNhap("gd")).thenReturn(Optional.of(giamDoc));
        when(chiTietPhieuKhoRepository.findByPhieuKho_Id(10L)).thenReturn(List.of());

        TaoPhieuNhapRequest request = new TaoPhieuNhapRequest();
        request.setTrangThai("da_huy");

        PhieuNhapResponse response = phieuNhapService.capNhatPhieuNhap(10L, request, "gd");

        assertEquals("da_huy", response.getTrangThai());
    }

    @Test
    void nvkhoKhongDuocXepViTriTuPhieuThuocKhoKhac() {
        PhieuKho phieu = phieuNhap(10L, kho(2L, "Kho B"), "da_hoan_thanh");
        NhanVien nhanVien = nhanVien("nv-a", "NVKHO", kho(1L, "Kho A"));

        when(phieuKhoRepository.findForUpdate(10L)).thenReturn(Optional.of(phieu));
        when(nhanVienRepository.findByTenDangNhap("nv-a")).thenReturn(Optional.of(nhanVien));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> phieuNhapService.xepViTri(10L, new XepViTriRequest(), "nv-a"));

        assertEquals(MSG_KHONG_DUOC_KHO, ex.getMessage());
        verifyNoInteractions(tonKhoNghiepVu);
    }

    @Test
    void nvkhoKhongDuocXepViTriSangKhoDichKhongDuocGan() {
        Kho khoA = kho(1L, "Kho A");
        Kho khoB = kho(2L, "Kho B");
        NhanVien nhanVien = nhanVien("nv-a", "NVKHO", khoA);
        PhieuKho phieu = phieuNhap(10L, khoA, "da_hoan_thanh");
        MatHang matHang = matHang(5L, "Pallet gỗ");
        ChiTietPhieuKho chiTiet = chiTiet(1L, phieu, matHang, 10);

        when(phieuKhoRepository.findForUpdate(10L)).thenReturn(Optional.of(phieu));
        when(nhanVienRepository.findByTenDangNhap("nv-a")).thenReturn(Optional.of(nhanVien));
        when(chiTietPhieuKhoRepository.findByPhieuKho_Id(10L)).thenReturn(List.of(chiTiet));
        when(matHangRepository.findById(5L)).thenReturn(Optional.of(matHang));
        when(khoRepository.findById(2L)).thenReturn(Optional.of(khoB));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> phieuNhapService.xepViTri(10L, xepViTriRequest(5L, 2L, 7L), "nv-a"));

        assertEquals(MSG_KHONG_DUOC_KHO, ex.getMessage());
        verifyNoInteractions(tonKhoNghiepVu);
    }

    @Test
    void gdDuocXepViTriSangKhoDichKhac() {
        Kho khoA = kho(1L, "Kho A");
        Kho khoB = kho(2L, "Kho B");
        NhanVien giamDoc = nhanVien("gd", "GD", null);
        PhieuKho phieu = phieuNhap(10L, khoA, "da_hoan_thanh");
        MatHang matHang = matHang(5L, "Pallet gỗ");
        ChiTietPhieuKho chiTiet = chiTiet(1L, phieu, matHang, 10);
        ViTriLuuTru viTriB = ViTriLuuTru.builder().id(7L).maViTri("V-B").kho(khoB).build();
        TonKho tonKhoB = TonKho.builder()
                .id(1L).matHang(matHang).kho(khoB).soLuongTonKho(100).soLuongDaGiuCho(0).build();

        when(phieuKhoRepository.findForUpdate(10L)).thenReturn(Optional.of(phieu));
        when(nhanVienRepository.findByTenDangNhap("gd")).thenReturn(Optional.of(giamDoc));
        when(chiTietPhieuKhoRepository.findByPhieuKho_Id(10L)).thenReturn(List.of(chiTiet));
        when(matHangRepository.findById(5L)).thenReturn(Optional.of(matHang));
        when(khoRepository.findById(2L)).thenReturn(Optional.of(khoB));
        when(viTriLuuTruRepository.findById(7L)).thenReturn(Optional.of(viTriB));
        when(tonKhoRepository.findForUpdate(5L, 2L)).thenReturn(Optional.of(tonKhoB));

        phieuNhapService.xepViTri(10L, xepViTriRequest(5L, 2L, 7L), "gd");

        assertTrue(phieu.isDaXepViTri());
        verify(tonKhoNghiepVu).tru(matHang, khoA, 10);
        verify(tonKhoNghiepVu).cong(matHang, khoB, 10);
        assertSame(viTriB, tonKhoB.getViTri());
    }

    @Test
    void phieuNhapKhoNullBiTuChoiVoiNvkho() {
        PhieuKho phieu = phieuNhap(10L, null, "cho_xu_ly");
        NhanVien nhanVien = nhanVien("nv-a", "NVKHO", kho(1L, "Kho A"));

        when(phieuKhoRepository.findForUpdate(10L)).thenReturn(Optional.of(phieu));
        when(nhanVienRepository.findByTenDangNhap("nv-a")).thenReturn(Optional.of(nhanVien));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> phieuNhapService.capNhatPhieuNhap(10L, taoPhieuNhapRequest(2L), "nv-a"));

        assertEquals(MSG_KHONG_DUOC_KHO, ex.getMessage());
    }

    @Test
    void phieuNhapKhoNullGdDuocHuy() {
        PhieuKho phieu = phieuNhap(10L, null, "cho_xu_ly");
        NhanVien giamDoc = nhanVien("gd", "GD", null);

        when(phieuKhoRepository.findForUpdate(10L)).thenReturn(Optional.of(phieu));
        when(nhanVienRepository.findByTenDangNhap("gd")).thenReturn(Optional.of(giamDoc));
        when(chiTietPhieuKhoRepository.findByPhieuKho_Id(10L)).thenReturn(List.of());

        TaoPhieuNhapRequest request = new TaoPhieuNhapRequest();
        request.setTrangThai("da_huy");

        PhieuNhapResponse response = phieuNhapService.capNhatPhieuNhap(10L, request, "gd");

        assertEquals("da_huy", response.getTrangThai());
    }

    @Test
    void xepViTriPhieuKhoNullBiTuChoiVoiNvkho() {
        PhieuKho phieu = phieuNhap(10L, null, "da_hoan_thanh");
        NhanVien nhanVien = nhanVien("nv-a", "NVKHO", kho(1L, "Kho A"));

        when(phieuKhoRepository.findForUpdate(10L)).thenReturn(Optional.of(phieu));
        when(nhanVienRepository.findByTenDangNhap("nv-a")).thenReturn(Optional.of(nhanVien));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> phieuNhapService.xepViTri(10L, new XepViTriRequest(), "nv-a"));

        assertEquals(MSG_KHONG_DUOC_KHO, ex.getMessage());
        verifyNoInteractions(tonKhoNghiepVu);
    }

    private ChiTietPhieuNhapItemRequest dongHang(Long matHangId, int soLuong) {
        ChiTietPhieuNhapItemRequest item = new ChiTietPhieuNhapItemRequest();
        item.setMatHangId(matHangId);
        item.setSoLuong(soLuong);
        return item;
    }

    private TaoPhieuNhapRequest taoPhieuNhapRequest(Long khoId) {
        TaoPhieuNhapRequest request = new TaoPhieuNhapRequest();
        request.setKhoId(khoId);
        request.setChiTiet(List.of(dongHang(5L, 10)));
        return request;
    }

    private XepViTriRequest xepViTriRequest(Long matHangId, Long khoId, Long viTriId) {
        XepViTriRequest.Item item = new XepViTriRequest.Item();
        item.setMatHangId(matHangId);
        item.setKhoId(khoId);
        item.setViTriId(viTriId);
        XepViTriRequest request = new XepViTriRequest();
        request.setItems(List.of(item));
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

    private MatHang matHang(Long id, String tenMatHang) {
        return MatHang.builder()
                .id(id)
                .maMatHang("MH-" + id)
                .tenMatHang(tenMatHang)
                .loaiMatHang("pallet")
                .build();
    }

    private PhieuKho phieuNhap(Long id, Kho kho, String trangThai) {
        return PhieuKho.builder()
                .id(id)
                .maPhieu("PN-" + id)
                .loaiPhieu("nhap_kho")
                .kho(kho)
                .trangThai(trangThai)
                .build();
    }

    private ChiTietPhieuKho chiTiet(Long id, PhieuKho phieu, MatHang matHang, int soLuong) {
        return ChiTietPhieuKho.builder()
                .id(id)
                .phieuKho(phieu)
                .matHang(matHang)
                .soLuong(soLuong)
                .build();
    }
}
