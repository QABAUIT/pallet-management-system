package com.pallet.backend.controller;

import com.pallet.backend.dto.response.ApiResponse;
import com.pallet.backend.dto.response.ThongBaoResponse;
import com.pallet.backend.dto.response.ThongBaoThongKeResponse;
import com.pallet.backend.service.ThongBaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/thong-bao")
@RequiredArgsConstructor
public class ThongBaoController {

    private final ThongBaoService thongBaoService;

    @GetMapping
    public ApiResponse<List<ThongBaoResponse>> layDanhSach(
            @RequestParam(required = false) Boolean daDoc,
            @RequestParam(required = false) String loaiThongBao,
            Principal principal) {
        String username = principal != null ? principal.getName() : null;
        return ApiResponse.success(thongBaoService.layDanhSach(daDoc, loaiThongBao, username), "OK");
    }

    @GetMapping("/thong-ke")
    public ApiResponse<ThongBaoThongKeResponse> thongKe(Principal principal) {
        String username = principal != null ? principal.getName() : null;
        return ApiResponse.success(thongBaoService.thongKe(username), "OK");
    }

    @GetMapping("/{id}")
    public ApiResponse<ThongBaoResponse> layChiTiet(@PathVariable Long id) {
        return ApiResponse.success(thongBaoService.layChiTiet(id), "OK");
    }

    @PatchMapping("/{id}/doc")
    public ApiResponse<ThongBaoResponse> danhDauDaDoc(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") Boolean daDoc) {
        return ApiResponse.success(thongBaoService.danhDauDaDoc(id, daDoc), "Cập nhật trạng thái thông báo thành công");
    }

    @PostMapping("/doc-tat-ca")
    public ApiResponse<Void> danhDauTatCaDaDoc(Principal principal) {
        String username = principal != null ? principal.getName() : null;
        thongBaoService.danhDauTatCaDaDoc(username);
        return ApiResponse.success(null, "Đã đánh dấu tất cả thông báo là đã đọc");
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> xoa(@PathVariable Long id) {
        thongBaoService.xoa(id);
        return ApiResponse.success(null, "Xóa thông báo thành công");
    }
}
