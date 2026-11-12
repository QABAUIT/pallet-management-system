package com.pallet.backend.service;

import com.pallet.backend.dto.request.BanPheLieuRequest;
import com.pallet.backend.entity.KhachHang;
import com.pallet.backend.entity.Kho;
import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.entity.VaiTro;
import com.pallet.backend.exception.BusinessException;
import com.pallet.backend.repository.ChiTietHoaDonRepository;
import com.pallet.backend.repository.ChiTietPhieuKhoRepository;
import com.pallet.backend.repository.HoaDonRepository;
import com.pallet.backend.repository.KhachHangRepository;
import com.pallet.backend.repository.KhoRepository;
import com.pallet.backend.repository.LichSuHoaDonRepository;
import com.pallet.backend.repository.MatHangRepository;
import com.pallet.backend.repository.NhanVienRepository;
import com.pallet.backend.repository.PhieuKhoRepository;
import com.pallet.backend.repository.ThanhToanRepository;
import com.pallet.backend.repository.TonKhoRepository;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BanPheLieuServiceTest {

    @Mock
    private HoaDonRepository hoaDonRepository;
    @Mock
    private ChiTietHoaDonRepository chiTietHoaDonRepository;
    @Mock
    private LichSuHoaDonRepository lichSuHoaDonRepository;
    @Mock
    private ThanhToanRepository thanhToanRepository;
    @Mock
    private KhachHangRepository khachHangRepository;
    @Mock
    private MatHangRepository matHangRepository;
    @Mock
    private TonKhoRepository tonKhoRepository;
    @Mock
    private NhanVienRepository nhanVienRepository;
    @Mock
    private KhoRepository khoRepository;
    @Mock
    private PhieuKhoRepository phieuKhoRepository;
    @Mock
    private ChiTietPhieuKhoRepository chiTietPhieuKhoRepository;
    @Mock
    private NhatKyService nhatKyService;

    @Spy
    private PhanQuyenKho phanQuyenKho = new PhanQuyenKho();

    @InjectMocks
    private BanPheLieuService banPheLieuService;

    @Test
    void nvkhoVaNvbanHangKhongDuocBanPheLieuOKhoKhongDuocGan() {
        Kho khoDuocGan = Kho.builder().id(1L).tenKho("Kho được gán").build();
        Kho khoKhac = Kho.builder().id(2L).tenKho("Kho khác").build();
        KhachHang khachHang = KhachHang.builder()
                .id(3L)
                .tenKh("Khách hàng hợp tác")
                .trangThai("dang_hop_tac")
                .build();

        when(khachHangRepository.findById(3L)).thenReturn(Optional.of(khachHang));
        when(khoRepository.findById(2L)).thenReturn(Optional.of(khoKhac));

        for (String maVaiTro : List.of("NVKHO", "NVBANHANG")) {
            NhanVien nhanVien = NhanVien.builder()
                    .id(10L)
                    .tenDangNhap("nvbh")
                    .hoTen("Nhân viên bán hàng")
                    .vaiTro(VaiTro.builder().maVaiTro(maVaiTro).build())
                    .kho(khoDuocGan)
                    .build();
            when(nhanVienRepository.findByTenDangNhap("nvbh")).thenReturn(Optional.of(nhanVien));

            BanPheLieuRequest request = new BanPheLieuRequest();
            request.setKhachHangId(3L);
            request.setKhoId(2L);
            request.setSoKg(10);

            BusinessException ex = assertThrows(BusinessException.class,
                    () -> banPheLieuService.ban(request, "nvbh"),
                    "Vai trò " + maVaiTro + " phải bị chặn khi bán phế liệu ở kho không được gán");

            assertEquals("Bạn chỉ được thao tác với dữ liệu của kho được gán cho mình", ex.getMessage());
        }

        verify(tonKhoRepository, never()).findForUpdate(anyLong(), anyLong());
    }
}
