package com.pallet.backend.service;

import com.pallet.backend.entity.NhanVien;
import com.pallet.backend.entity.NhatKyHeThong;
import com.pallet.backend.repository.NhatKyHeThongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NhatKyService {

    private final NhatKyHeThongRepository nhatKyHeThongRepository;

    public void ghi(NhanVien nv, String module, String hanhDong, String doiTuongLoai, Long doiTuongId) {
        nhatKyHeThongRepository.save(NhatKyHeThong.builder()
                .nhanVien(nv).module(module).hanhDong(hanhDong)
                .doiTuongLoai(doiTuongLoai).doiTuongId(doiTuongId)
                .thoiGian(LocalDateTime.now()).build());
    }
}