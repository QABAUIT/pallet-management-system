package com.pallet.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import lombok.*;

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

    @Column(name = "payload_tho", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String payloadTho;

    @Column(name = "da_xu_ly", nullable = false)
    private Boolean daXuLy;

    @Column(name = "loi_xu_ly", length = 255)
    private String loiXuLy;

    @Column(name = "thoi_gian_nhan", nullable = false)
    private LocalDateTime thoiGianNhan;

}