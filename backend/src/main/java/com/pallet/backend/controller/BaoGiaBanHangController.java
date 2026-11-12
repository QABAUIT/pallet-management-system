package com.pallet.backend.controller;

import com.pallet.backend.dto.request.ChuyenHoaDonRequest;
import com.pallet.backend.dto.request.TaoBaoGiaRequest;
import com.pallet.backend.dto.response.ApiResponse;
import com.pallet.backend.dto.response.BaoGiaResponse;
import com.pallet.backend.dto.response.DanhMucBanHangResponse;
import com.pallet.backend.dto.response.TaoHoaDonResponse;
import com.pallet.backend.service.BaoGiaBanHangService;
import com.pallet.backend.service.HoaDonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bao-gia-ban-hang")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GD','PGD','NVBANHANG')")
public class BaoGiaBanHangController {

    private final BaoGiaBanHangService baoGiaService;
    private final HoaDonService hoaDonService;

    @GetMapping
    public ApiResponse<List<BaoGiaResponse>> layDanhSach(@RequestParam(required = false) String trangThai) {
        return ApiResponse.success(baoGiaService.layDanhSach(trangThai), "OK");
    }

    /** Dùng lại danh mục bán hàng của hóa đơn (khách, mặt hàng, giá, tồn). */
    @GetMapping("/danh-muc")
    public ApiResponse<DanhMucBanHangResponse> danhMuc(Principal principal) {
        return ApiResponse.success(hoaDonService.layDanhMucBanHang(principal.getName()), "OK");
    }

    @GetMapping("/{id}")
    public ApiResponse<BaoGiaResponse> layChiTiet(@PathVariable Long id) {
        return ApiResponse.success(baoGiaService.layChiTiet(id), "OK");
    }

    @PostMapping
    public ApiResponse<BaoGiaResponse> taoMoi(@Valid @RequestBody TaoBaoGiaRequest req, Principal principal) {
        return ApiResponse.success(baoGiaService.taoMoi(req, principal.getName()), "Tạo báo giá thành công");
    }

    @PutMapping("/{id}")
    public ApiResponse<BaoGiaResponse> capNhat(@PathVariable Long id,
                                               @Valid @RequestBody TaoBaoGiaRequest req, Principal principal) {
        return ApiResponse.success(baoGiaService.capNhat(id, req, principal.getName()), "Cập nhật báo giá thành công");
    }

    @PostMapping("/{id}/gui")
    public ApiResponse<BaoGiaResponse> gui(@PathVariable Long id, Principal principal) {
        return ApiResponse.success(baoGiaService.gui(id, principal.getName()), "Đã gửi báo giá cho khách");
    }

    @PostMapping("/{id}/chap-nhan")
    public ApiResponse<BaoGiaResponse> chapNhan(@PathVariable Long id, Principal principal) {
        return ApiResponse.success(baoGiaService.chapNhan(id, principal.getName()), "Khách đã chấp nhận báo giá");
    }

    @PostMapping("/{id}/tu-choi")
    public ApiResponse<BaoGiaResponse> tuChoi(@PathVariable Long id,
                                              @RequestParam(required = false) String lyDo, Principal principal) {
        return ApiResponse.success(baoGiaService.tuChoi(id, lyDo, principal.getName()), "Đã ghi nhận khách từ chối");
    }

    @PostMapping("/{id}/chuyen-hoa-don")
    public ApiResponse<TaoHoaDonResponse> chuyenHoaDon(@PathVariable Long id,
                                                       @RequestBody(required = false) ChuyenHoaDonRequest req,
                                                       Principal principal) {
        return ApiResponse.success(baoGiaService.chuyenThanhHoaDon(id, req, principal.getName()),
                "Đã lập hóa đơn từ báo giá");
    }
}