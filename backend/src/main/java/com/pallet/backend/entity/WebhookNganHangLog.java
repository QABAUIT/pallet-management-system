package com.pallet.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_ngan_hang_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebhookNganHangLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_qr_thanh_toan_id")
    private MaQRThanhToan maQrThanhToan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thanh_toan_id")
    private ThanhToan thanhToan;

    // Cột JSONB - dùng JdbcTypeCode(JSON) của Hibernate 6, map String thô.
    // Nếu cần thao tác như object, đổi type sang com.fasterxml.jackson.databind.JsonNode
    // hoặc dùng thư viện hypersistence-utils.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_tho", nullable = false, columnDefinition = "jsonb")
    private String payloadTho;

    @Column(name = "da_xu_ly", nullable = false)
    @Builder.Default
    private Boolean daXuLy = false;

    @Column(name = "loi_xu_ly", length = 255)
    private String loiXuLy;

    @Column(name = "thoi_gian_nhan", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime thoiGianNhan = LocalDateTime.now();
}
