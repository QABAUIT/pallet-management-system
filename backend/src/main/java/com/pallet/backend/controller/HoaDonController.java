package com.pallet.backend.controller;

import com.pallet.backend.dto.request.CapNhatHoaDonRequest;
import com.pallet.backend.dto.request.TaoHoaDonRequest;
import com.pallet.backend.dto.request.ThanhToanHoaDonRequest;
import com.pallet.backend.dto.response.ApiResponse;
import com.pallet.backend.dto.response.DanhMucBanHangResponse;
import com.pallet.backend.dto.response.HoaDonChiTietResponse;
import com.pallet.backend.dto.response.HoaDonResponse;
import com.pallet.backend.dto.response.TaoHoaDonResponse;
import com.pallet.backend.service.HoaDonService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/hoa-don")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GD','PGD','NVBANHANG')")
public class HoaDonController {

    private final HoaDonService hoaDonService;

    @GetMapping
    public ApiResponse<List<HoaDonResponse>> layDanhSach(@RequestParam(required = false) String trangThai) {
        return ApiResponse.success(hoaDonService.layDanhSach(trangThai), "OK");
    }

    @GetMapping("/danh-muc-ban-hang")
    public ApiResponse<DanhMucBanHangResponse> danhMuc(Principal principal) {
        return ApiResponse.success(hoaDonService.layDanhMucBanHang(principal.getName()), "OK");
    }

    @GetMapping("/{id}")
    public ApiResponse<HoaDonChiTietResponse> layChiTiet(@PathVariable Long id) {
        return ApiResponse.success(hoaDonService.layChiTiet(id), "OK");
    }

    @PostMapping
    public ApiResponse<TaoHoaDonResponse> taoMoi(@Valid @RequestBody TaoHoaDonRequest req, Principal principal) {
        return ApiResponse.success(hoaDonService.taoMoi(req, principal.getName()), "Tạo hóa đơn thành công");
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> capNhat(@PathVariable Long id,
                                     @Valid @RequestBody CapNhatHoaDonRequest req, Principal principal) {
        hoaDonService.capNhat(id, req, principal.getName());
        return ApiResponse.success(null, "Cập nhật hóa đơn thành công");
    }

    @PostMapping("/{id}/thanh-toan")
    public ApiResponse<Void> thanhToan(@PathVariable Long id,
                                       @Valid @RequestBody ThanhToanHoaDonRequest req, Principal principal) {
        hoaDonService.thanhToan(id, req, principal.getName());
        return ApiResponse.success(null, "Thanh toán thành công");
    }

    /** XÓA MỀM: chuyển hóa đơn sang "da_huy", nhả giữ chỗ tồn kho. Bắt buộc có lý do. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('GD','PGD')")
    public ApiResponse<Void> xoa(@PathVariable Long id,
                                 @RequestParam(required = false) String lyDo, Principal principal) {
        hoaDonService.huy(id, lyDo, principal.getName());
        return ApiResponse.success(null, "Đã xóa hóa đơn");
    }
}