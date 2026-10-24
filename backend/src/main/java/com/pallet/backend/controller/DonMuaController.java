package com.pallet.backend.controller;

import com.pallet.backend.dto.request.NhanHangDonMuaRequest;
import com.pallet.backend.dto.request.TaoDonMuaRequest;
import com.pallet.backend.dto.response.ApiResponse;
import com.pallet.backend.dto.response.DanhMucDonMuaResponse;
import com.pallet.backend.dto.response.DonMuaChiTietResponse;
import com.pallet.backend.dto.response.DonMuaResponse;
import com.pallet.backend.service.DonMuaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/don-mua")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GD','PGD','NVKHO')")
public class DonMuaController {

    private final DonMuaService donMuaService;

    @GetMapping
    public ApiResponse<List<DonMuaResponse>> layDanhSach(@RequestParam(required = false) String trangThai) {
        return ApiResponse.success(donMuaService.layDanhSach(trangThai), "OK");
    }

    @GetMapping("/danh-muc")
    public ApiResponse<DanhMucDonMuaResponse> danhMuc(Principal principal) {
        return ApiResponse.success(donMuaService.layDanhMuc(principal.getName()), "OK");
    }

    @GetMapping("/{id}")
    public ApiResponse<DonMuaChiTietResponse> layChiTiet(@PathVariable Long id) {
        return ApiResponse.success(donMuaService.layChiTiet(id), "OK");
    }

    @PostMapping
    public ApiResponse<DonMuaChiTietResponse> taoMoi(@Valid @RequestBody TaoDonMuaRequest req, Principal principal) {
        return ApiResponse.success(donMuaService.taoMoi(req, principal.getName()), "Tạo đơn mua thành công");
    }

    @PutMapping("/{id}")
    public ApiResponse<DonMuaChiTietResponse> capNhat(@PathVariable Long id,
                                                      @Valid @RequestBody TaoDonMuaRequest req, Principal principal) {
        return ApiResponse.success(donMuaService.capNhat(id, req, principal.getName()), "Cập nhật đơn mua thành công");
    }

    @PostMapping("/{id}/xac-nhan")
    @PreAuthorize("hasAnyRole('GD','PGD')")
    public ApiResponse<DonMuaChiTietResponse> xacNhan(@PathVariable Long id, Principal principal) {
        return ApiResponse.success(donMuaService.xacNhan(id, principal.getName()), "Đã xác nhận đơn mua");
    }

    @PostMapping("/{id}/nhan-hang")
    public ApiResponse<DonMuaChiTietResponse> nhanHang(@PathVariable Long id,
                                                       @Valid @RequestBody NhanHangDonMuaRequest req, Principal principal) {
        return ApiResponse.success(donMuaService.nhanHang(id, req, principal.getName()), "Nhận hàng thành công");
    }

    @PostMapping("/{id}/huy")
    @PreAuthorize("hasAnyRole('GD','PGD')")
    public ApiResponse<DonMuaChiTietResponse> huy(@PathVariable Long id,
                                                  @RequestParam(required = false) String lyDo, Principal principal) {
        return ApiResponse.success(donMuaService.huy(id, lyDo, principal.getName()), "Đã hủy đơn mua");
    }
}