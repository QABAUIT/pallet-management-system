package com.pallet.backend.repository;

import com.pallet.backend.entity.WebhookNganHangLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WebhookNganHangLogRepository extends JpaRepository<WebhookNganHangLog, Long> {

    List<WebhookNganHangLog> findByMaQrThanhToan_Id(Long maQrThanhToanId);
    List<WebhookNganHangLog> findByThanhToan_Id(Long thanhToanId);
}