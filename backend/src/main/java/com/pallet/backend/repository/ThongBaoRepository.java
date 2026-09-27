package com.pallet.backend.repository;

import com.pallet.backend.entity.ThongBao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ThongBaoRepository extends JpaRepository<ThongBao, Long> {

    List<ThongBao> findByNguoiNhan_Id(Long nguoiNhanId);
    List<ThongBao> findByVaiTroNhan_Id(Long vaiTroNhanId);
}