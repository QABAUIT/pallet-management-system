package com.pallet.backend.repository;

import com.pallet.backend.entity.MuaLaiPheLieuKhach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MuaLaiPheLieuKhachRepository extends JpaRepository<MuaLaiPheLieuKhach, Long> {
}