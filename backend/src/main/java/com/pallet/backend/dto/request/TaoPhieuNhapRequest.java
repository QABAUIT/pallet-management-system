package com.pallet.backend.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class TaoPhieuNhapRequest {
    private Long khoId;
    private Long nccId;
    private Long nhanVienTiepNhanId;
    private String trangThai; // "cho_xu_ly" (Chờ nhập/Đang kiểm định) hoặc "da_hoan_thanh" (Hoàn tất)
    private String ghiChu;
    private String bienSoXe;
    private List<ChiTietPhieuNhapItemRequest> chiTiet;
}