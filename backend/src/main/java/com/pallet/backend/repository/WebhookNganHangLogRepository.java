package com.pallet.backend.repository;

import com.pallet.backend.entity.WebhookNganHangLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface WebhookNganHangLogRepository extends JpaRepository<WebhookNganHangLog, Long>, JpaSpecificationExecutor<WebhookNganHangLog> {

    List<WebhookNganHangLog> findByDaXuLyFalse();

    List<WebhookNganHangLog> findByMaQrThanhToanId(Long maQrThanhToanId);

    List<WebhookNganHangLog> findByThanhToanId(Long thanhToanId);
}
