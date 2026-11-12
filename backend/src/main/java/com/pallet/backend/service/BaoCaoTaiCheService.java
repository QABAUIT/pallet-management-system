package com.pallet.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Báo cáo tái chế: hiệu suất, giá mua theo lô, chênh lệch định mức. Chỉ đọc dữ liệu đã duyệt. */
@Service
@RequiredArgsConstructor
public class BaoCaoTaiCheService {

    private final JdbcTemplate jdbc;

    @Transactional(readOnly = true)
    public Map<String, Object> hieuSuat(LocalDate tu, LocalDate den, Long khoId) {
        LocalDate t = tu != null ? tu : LocalDate.of(2000, 1, 1);
        LocalDate d = den != null ? den : LocalDate.now();

        Map<String, Object> pl = jdbc.queryForMap(
                "SELECT COALESCE(SUM(so_luong_da_xu_ly),0) AS tong, COALESCE(SUM(so_ban_lai),0) AS ban_lai, "
                        + "COALESCE(SUM(so_thao_do),0) AS thao_do, COALESCE(SUM(so_loai_bo),0) AS loai_bo, "
                        + "COALESCE(SUM(kg_phe_lieu_loai_bo),0) AS kg FROM phieu_tai_che "
                        + "WHERE trang_thai IN ('da_phan_loai','da_hoan_tat') AND thoi_gian_duyet::date BETWEEN ? AND ?"
                        + (khoId != null ? " AND kho_id = ?" : ""),
                args(t, d, khoId));
        long tong = n(pl, "tong"), banLai = n(pl, "ban_lai"), thaoDo = n(pl, "thao_do"), loaiBo = n(pl, "loai_bo");

        Map<String, Object> td = jdbc.queryForMap(
                "SELECT COALESCE(SUM(so_pallet_thao_do),0) AS pallet, COALESCE(SUM(so_thanh_nguyen),0) AS tn, "
                        + "COALESCE(SUM(so_cuc_vuong),0) AS cv, COALESCE(SUM(so_thanh_can_xu_ly),0) AS tx, "
                        + "COALESCE(SUM(so_cuc_can_xu_ly),0) AS cx, COALESCE(SUM(kg_phe_lieu),0) AS kg "
                        + "FROM phieu_thao_do WHERE trang_thai = 'da_duyet' AND thoi_gian_duyet::date BETWEEN ? AND ?"
                        + (khoId != null ? " AND kho_id = ?" : ""),
                args(t, d, khoId));
        long pallet = n(td, "pallet"), tn = n(td, "tn"), cv = n(td, "cv"), tx = n(td, "tx"), cx = n(td, "cx");

        List<Map<String, Object>> xl = jdbc.queryForList(
                "SELECT loai_nguon, COALESCE(SUM(so_dat_lai),0) AS dat_lai, COALESCE(SUM(so_phe_lieu),0) AS phe_lieu, "
                        + "COALESCE(SUM(kg_phe_lieu),0) AS kg FROM phieu_xu_ly_them "
                        + "WHERE trang_thai = 'da_duyet' AND thoi_gian_duyet::date BETWEEN ? AND ?"
                        + (khoId != null ? " AND kho_id = ?" : "") + " GROUP BY loai_nguon ORDER BY loai_nguon",
                args(t, d, khoId));

        Map<String, Object> dv = jdbc.queryForMap(
                "SELECT COALESCE(SUM(so_pallet_thao_do),0) AS pallet, COALESCE(SUM(phi_dich_vu),0) AS phi "
                        + "FROM phieu_dich_vu_thao_do WHERE trang_thai IN ('da_thao_do','da_tra_hang') "
                        + "AND thoi_gian_duyet::date BETWEEN ? AND ?" + (khoId != null ? " AND kho_id = ?" : ""),
                args(t, d, khoId));

        Map<String, Object> phanLoai = new LinkedHashMap<>();
        phanLoai.put("tongPalletXuLy", tong);
        phanLoai.put("banLai", banLai);
        phanLoai.put("thaoDo", thaoDo);
        phanLoai.put("loaiBo", loaiBo);
        phanLoai.put("kgPheLieuLoaiBo", n(pl, "kg"));
        phanLoai.put("tyLeBanLai", tyLe(banLai, tong));
        phanLoai.put("tyLeThaoDo", tyLe(thaoDo, tong));
        phanLoai.put("tyLeLoaiBo", tyLe(loaiBo, tong));

        Map<String, Object> thaoDoMap = new LinkedHashMap<>();
        thaoDoMap.put("palletDaThaoDo", pallet);
        thaoDoMap.put("thanhNguyen", tn);
        thaoDoMap.put("cucVuong", cv);
        thaoDoMap.put("thanhCanXuLy", tx);
        thaoDoMap.put("cucCanXuLy", cx);
        thaoDoMap.put("kgPheLieu", n(td, "kg"));
        thaoDoMap.put("thanhNguyenTrenPallet", chia(tn, pallet));
        thaoDoMap.put("cucVuongTrenPallet", chia(cv, pallet));
        thaoDoMap.put("tyLeCanXuLy", tyLe(tx + cx, tn + cv + tx + cx));
        thaoDoMap.put("kgPheLieuTrenPallet", chia(n(td, "kg"), pallet));

        Map<String, Object> dichVu = new LinkedHashMap<>();
        dichVu.put("palletThaoDoChoKhach", n(dv, "pallet"));
        dichVu.put("doanhThuDichVuTruocVat", dv.get("phi"));

        Map<String, Object> kq = new LinkedHashMap<>();
        kq.put("tu", t);
        kq.put("den", d);
        kq.put("khoId", khoId);
        kq.put("phanLoai", phanLoai);
        kq.put("thaoDo", thaoDoMap);
        kq.put("xuLyThem", xl);
        kq.put("dichVuChoKhach", dichVu);
        return kq;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> theoLo(LocalDate tu, LocalDate den, Long khoId) {
        LocalDate t = tu != null ? tu : LocalDate.of(2000, 1, 1);
        LocalDate d = den != null ? den : LocalDate.now();
        List<Map<String, Object>> ds = jdbc.queryForList(
                "SELECT l.ma_lo, n.ten_ncc, l.ngay_nhap, l.so_luong_nhap, l.don_gia_nhap, "
                        + "l.so_luong_nhap * COALESCE(l.don_gia_nhap,0) AS tong_von, l.so_luong_con_lai, "
                        + "COALESCE(SUM(p.so_ban_lai),0) AS ban_lai, COALESCE(SUM(p.so_thao_do),0) AS thao_do, "
                        + "COALESCE(SUM(p.so_loai_bo),0) AS loai_bo, COALESCE(SUM(p.kg_phe_lieu_loai_bo),0) AS kg_phe_lieu "
                        + "FROM lo_hang l LEFT JOIN nha_cung_cap n ON n.id = l.ncc_id "
                        + "LEFT JOIN phieu_tai_che p ON p.lo_hang_id = l.id AND p.trang_thai IN ('da_phan_loai','da_hoan_tat') "
                        + "WHERE l.muc_dich_nhap = 'cho_tai_che' AND l.ngay_nhap BETWEEN ? AND ?"
                        + (khoId != null ? " AND l.kho_id = ?" : "")
                        + " GROUP BY l.id, n.ten_ncc ORDER BY l.ngay_nhap DESC, l.id DESC",
                args(t, d, khoId));
        for (Map<String, Object> m : ds) {
            long nhap = n(m, "so_luong_nhap");
            m.put("so_pallet_da_xu_ly", nhap - n(m, "so_luong_con_lai"));
            m.put("ty_le_ban_lai", tyLe(n(m, "ban_lai"), nhap));
            m.put("ty_le_thao_do", tyLe(n(m, "thao_do"), nhap));
            m.put("ty_le_loai_bo", tyLe(n(m, "loai_bo"), nhap));
        }
        return ds;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> chenhLechDinhMuc(LocalDate tu, LocalDate den, Long khoId) {
        LocalDate t = tu != null ? tu : LocalDate.of(2000, 1, 1);
        LocalDate d = den != null ? den : LocalDate.now();
        String khoP = khoId != null ? " AND p.kho_id = ?" : "";

        List<Map<String, Object>> theoVatTu = jdbc.queryForList(
                "SELECT m.ma_mat_hang, m.ten_mat_hang, SUM(c.so_luong_dinh_muc) AS dinh_muc, "
                        + "SUM(c.so_luong_thuc_te) AS thuc_te, SUM(c.chenh_lech) AS chenh_lech "
                        + "FROM chi_tiet_dong_pallet c JOIN phieu_dong_pallet p ON p.id = c.phieu_dong_pallet_id "
                        + "JOIN mat_hang m ON m.id = c.mat_hang_id "
                        + "WHERE p.trang_thai = 'da_duyet' AND p.thoi_gian_duyet::date BETWEEN ? AND ?" + khoP
                        + " GROUP BY m.id ORDER BY ABS(SUM(c.chenh_lech)) DESC, m.id",
                args(t, d, khoId));
        for (Map<String, Object> m : theoVatTu) {
            m.put("ty_le_chenh_lech", tyLe(n(m, "chenh_lech"), n(m, "dinh_muc")));
        }
        List<Map<String, Object>> chiTiet = jdbc.queryForList(
                "SELECT p.ma_phieu, p.thoi_gian_duyet, m.ma_mat_hang, c.so_luong_dinh_muc, c.so_luong_thuc_te, "
                        + "c.chenh_lech, c.ly_do_chenh_lech FROM chi_tiet_dong_pallet c "
                        + "JOIN phieu_dong_pallet p ON p.id = c.phieu_dong_pallet_id JOIN mat_hang m ON m.id = c.mat_hang_id "
                        + "WHERE p.trang_thai = 'da_duyet' AND c.chenh_lech <> 0 "
                        + "AND p.thoi_gian_duyet::date BETWEEN ? AND ?" + khoP + " ORDER BY p.thoi_gian_duyet DESC, c.id",
                args(t, d, khoId));
        Map<String, Object> sx = jdbc.queryForMap(
                "SELECT COALESCE(SUM(so_luong_san_xuat),0) AS pallet, COUNT(*) AS so_phieu FROM phieu_dong_pallet p "
                        + "WHERE p.trang_thai = 'da_duyet' AND p.thoi_gian_duyet::date BETWEEN ? AND ?" + khoP,
                args(t, d, khoId));

        Map<String, Object> kq = new LinkedHashMap<>();
        kq.put("tu", t);
        kq.put("den", d);
        kq.put("khoId", khoId);
        kq.put("palletDaSanXuat", sx.get("pallet"));
        kq.put("soPhieuDaDuyet", sx.get("so_phieu"));
        kq.put("theoVatTu", theoVatTu);
        kq.put("cacDongLech", chiTiet);
        return kq;
    }

    // ---------- helper ----------

    private Object[] args(LocalDate tu, LocalDate den, Long khoId) {
        List<Object> a = new ArrayList<>(List.of(tu, den));
        if (khoId != null) a.add(khoId);
        return a.toArray();
    }

    private long n(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? 0 : ((Number) v).longValue();
    }

    private BigDecimal tyLe(long a, long b) {
        return b == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(a).multiply(BigDecimal.valueOf(100)).divide(BigDecimal.valueOf(b), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal chia(long a, long b) {
        return b == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(a).divide(BigDecimal.valueOf(b), 2, RoundingMode.HALF_UP);
    }
}