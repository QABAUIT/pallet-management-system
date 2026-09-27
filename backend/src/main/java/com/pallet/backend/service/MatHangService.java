package com.pallet.backend.service;

import com.pallet.backend.dto.request.MatHangRequest;
import com.pallet.backend.dto.response.MatHangResponse;

import java.util.List;

public interface MatHangService {
    List<MatHangResponse> layDanhSach(String trangThai);
    MatHangResponse layChiTiet(Long id);
    MatHangResponse taoMoi(MatHangRequest request);
    MatHangResponse capNhat(Long id, MatHangRequest request);
    void ngungKinhDoanh(Long id); // xóa mềm - đổi trang_thai = ngung_kinh_doanh, KHÔNG delete
}
