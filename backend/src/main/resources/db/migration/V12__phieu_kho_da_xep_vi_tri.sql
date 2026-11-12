-- Chặn xếp vị trí lặp lại trên cùng một phiếu nhập (tránh cộng/trừ tồn 2 lần).
ALTER TABLE phieu_kho ADD COLUMN da_xep_vi_tri BOOLEAN NOT NULL DEFAULT FALSE;
