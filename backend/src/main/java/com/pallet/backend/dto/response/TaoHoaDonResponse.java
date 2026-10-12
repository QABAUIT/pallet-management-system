package com.pallet.backend.dto.response;

import java.math.BigDecimal;

public record TaoHoaDonResponse(Long id, String maHoaDon, String trangThai,
                                BigDecimal tongThanhToan, BigDecimal tienThoiLai) {}