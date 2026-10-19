package com.pallet.backend.repository;

import com.pallet.backend.entity.ThongBao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThongBaoRepository extends JpaRepository<ThongBao, Long>, JpaSpecificationExecutor<ThongBao> {

    List<ThongBao> findByNguoiNhan_Id(Long nguoiNhanId);

    List<ThongBao> findByVaiTroNhan_Id(Long vaiTroNhanId);

    /**
     * Lấy tất cả thông báo dành cho người dùng theo 3 trường hợp:
     * (1) Gửi đích danh: nguoi_nhan_id = nvId
     * (2) Gửi cho vai trò: vai_tro_nhan_id = vaiTroId  (nguoi_nhan_id có thể null)
     * (3) Broadcast: nguoi_nhan_id IS NULL AND vai_tro_nhan_id IS NULL
     */
    @Query("""
            SELECT t FROM ThongBao t
            WHERE t.nguoiNhan.id = :nguoiNhanId
               OR t.vaiTroNhan.id = :vaiTroId
               OR (t.nguoiNhan IS NULL AND t.vaiTroNhan IS NULL)
            ORDER BY t.thoiGianTao DESC
            """)
    List<ThongBao> layTatCaChoNguoiDung(@Param("nguoiNhanId") Long nguoiNhanId,
                                         @Param("vaiTroId") Long vaiTroId);

    @Query("""
            SELECT COUNT(t) FROM ThongBao t
            WHERE t.daDoc = false
              AND (
                    t.nguoiNhan.id = :nguoiNhanId
                 OR t.vaiTroNhan.id = :vaiTroId
                 OR (t.nguoiNhan IS NULL AND t.vaiTroNhan IS NULL)
              )
            """)
    long demChuaDocChoNguoiDung(@Param("nguoiNhanId") Long nguoiNhanId,
                                 @Param("vaiTroId") Long vaiTroId);

    @Modifying
    @Query("""
            UPDATE ThongBao t SET t.daDoc = true
            WHERE t.nguoiNhan.id = :nguoiNhanId
               OR t.vaiTroNhan.id = :vaiTroId
               OR (t.nguoiNhan IS NULL AND t.vaiTroNhan IS NULL)
            """)
    int danhDauTatCaDaDoc(@Param("nguoiNhanId") Long nguoiNhanId,
                           @Param("vaiTroId") Long vaiTroId);
}