package com.pallet.backend.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class TaoPhieuXuatRequest {
    private Long khoId;
    private Long nhanVienXuatId;
    private String loaiXuat; // Xuất bán, Cho thuê, Bảo trì...
    private String lyDoXuat; // Thông tin đơn hàng
    private String trangThai; // "cho_xu_ly" hoặc "da_hoan_thanh"
    private String bienSoXe;
    private List<ChiTietPhieuXuatItemRequest> chiTiet;
}