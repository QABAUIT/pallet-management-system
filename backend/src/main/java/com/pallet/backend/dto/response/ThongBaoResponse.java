package com.pallet.backend.dto.response;

import java.time.LocalDateTime;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThongBaoResponse {
    private Long id;
    private String loaiThongBao;
    private String tieuDe;
    private String noiDung;
    private Long nguoiNhanId;
    private String tenNguoiNhan;
    private Long vaiTroNhanId;
    private String tenVaiTroNhan;
    private String doiTuongLoai;
    private Long doiTuongId;
    private Boolean daDoc;
    private LocalDateTime thoiGianTao;
}
