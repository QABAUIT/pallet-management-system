-- V10: Báo giá có hạn hiệu lực và liên kết hóa đơn

ALTER TABLE bao_gia
    ADD COLUMN han_hieu_luc DATE,
    ADD COLUMN hoa_don_id BIGINT REFERENCES hoa_don(id) ON DELETE RESTRICT;

UPDATE bao_gia SET han_hieu_luc = ngay_tao + 15;

ALTER TABLE bao_gia ALTER COLUMN han_hieu_luc SET NOT NULL;