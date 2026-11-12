package com.pallet.backend.repository;

import com.pallet.backend.entity.ChiTietDonMua;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ChiTietDonMuaRepository extends JpaRepository<ChiTietDonMua, Long> {

    List<ChiTietDonMua> findByDonMua_Id(Long donMuaId);
    List<ChiTietDonMua> findByDonMua_IdIn(Collection<Long> donMuaIds);
    List<ChiTietDonMua> findByMatHang_Id(Long matHangId);
}