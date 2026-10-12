import { useState, useEffect } from "react";
import { UserPlus, X, Save } from "lucide-react";
import "../layout/style.css";

const LOAI_KH_OPTIONS = [
  { value: "doanh_nghiep", label: "Doanh nghiệp" },
  { value: "ca_nhan", label: "Cá nhân" },
];

const TRANG_THAI_OPTIONS = [
  { value: "dang_hop_tac", label: "Đang hợp tác" },
  { value: "ngung_hop_tac", label: "Ngừng hợp tác" },
];

const EMPTY_FORM = {
  ten_kh: "",
  loai_kh: "doanh_nghiep",
  mst: "",
  dia_chi: "",
  sdt: "",
  email: "",
  nguoi_dai_dien: "",
  trang_thai: "dang_hop_tac",
};

/**
 * mode: "add" | "edit"
 * initialValues: dữ liệu có sẵn khi sửa (bỏ trống khi thêm mới)
 */
export default function AddCustomerModal({
  open,
  onClose,
  onSave,
  mode = "add",
  initialValues = null,
}) {
  const isEdit = mode === "edit";
  const [form, setForm] = useState(initialValues ?? EMPTY_FORM);
  const [errors, setErrors] = useState({});

  // Mỗi lần mở modal, nạp lại đúng dữ liệu (rỗng khi thêm, có sẵn khi sửa)
  useEffect(() => {
    if (open) {
      setForm(initialValues ?? EMPTY_FORM);
      setErrors({});
    }
  }, [open, initialValues]);

  if (!open) return null;

  const setField = (key, value) => setForm((f) => ({ ...f, [key]: value }));

  const validate = () => {
    const next = {};
    if (!form.ten_kh.trim()) next.ten_kh = "Vui lòng nhập tên khách hàng";
    if (!form.sdt.trim()) next.sdt = "Vui lòng nhập số điện thoại";
    setErrors(next);
    return Object.keys(next).length === 0;
  };

  const handleSave = () => {
    if (!validate()) return;
    const payload = isEdit ? { ...form } : { ...form, created_at: new Date().toISOString() };
    onSave(payload);
  };

  const handleClose = () => {
    setErrors({});
    onClose();
  };

  return (
    <div className="modal-overlay" onMouseDown={handleClose}>
      <div className="modal-box" onMouseDown={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <span className="modal-header-icon">
            <UserPlus size={18} />
          </span>
          <div className="modal-header-text">
            <p className="modal-title">{isEdit ? "Chỉnh sửa khách hàng" : "Thêm khách hàng mới"}</p>
            <p className="modal-subtitle">
              {isEdit ? "Cập nhật thông tin khách hàng" : "Lưu trữ thông tin khách hàng"}
            </p>
          </div>
          <button type="button" className="modal-close-btn" onClick={handleClose} aria-label="Đóng">
            <X size={18} />
          </button>
        </div>

        <div className="modal-body">
          <div className="form-grid">
            <Field label="Tên khách hàng" required error={errors.ten_kh}>
              <input
                type="text"
                className="form-input"
                value={form.ten_kh}
                onChange={(e) => setField("ten_kh", e.target.value)}
                placeholder="VD: CTCP Logistic Tân Bình Express"
              />
            </Field>

            <Field label="Loại khách hàng">
              <select
                className="form-input"
                value={form.loai_kh}
                onChange={(e) => setField("loai_kh", e.target.value)}
              >
                {LOAI_KH_OPTIONS.map((o) => (
                  <option key={o.value} value={o.value}>
                    {o.label}
                  </option>
                ))}
              </select>
            </Field>

            <Field label="Số điện thoại" required error={errors.sdt}>
              <input
                type="tel"
                className="form-input"
                value={form.sdt}
                onChange={(e) => setField("sdt", e.target.value)}
                placeholder="0123456789"
              />
            </Field>

            <Field label="Mã số thuế (MST)">
              <input
                type="text"
                className="form-input"
                value={form.mst}
                onChange={(e) => setField("mst", e.target.value)}
                placeholder="MST (nếu có)"
              />
            </Field>

            <Field label="Email">
              <input
                type="email"
                className="form-input"
                value={form.email}
                onChange={(e) => setField("email", e.target.value)}
                placeholder="email@congty.vn"
              />
            </Field>

            <Field label="Người đại diện">
              <input
                type="text"
                className="form-input"
                value={form.nguoi_dai_dien}
                onChange={(e) => setField("nguoi_dai_dien", e.target.value)}
                placeholder="Họ và tên"
              />
            </Field>

            <Field label="Địa chỉ" full>
              <textarea
                className="form-input form-textarea"
                value={form.dia_chi}
                onChange={(e) => setField("dia_chi", e.target.value)}
                placeholder="Số nhà, đường, phường/xã, tỉnh/thành"
                rows={2}
              />
            </Field>

            <Field label="Trạng thái">
              <select
                className="form-input"
                value={form.trang_thai}
                onChange={(e) => setField("trang_thai", e.target.value)}
              >
                {TRANG_THAI_OPTIONS.map((o) => (
                  <option key={o.value} value={o.value}>
                    {o.label}
                  </option>
                ))}
              </select>
            </Field>
          </div>
        </div>

        <div className="modal-footer">
          <button type="button" className="btn-outline" onClick={handleClose}>
            Hủy
          </button>
          <button type="button" className="btn-solid" onClick={handleSave}>
            <Save size={16} />
            {isEdit ? "Lưu thay đổi" : "Lưu khách hàng mới"}
          </button>
        </div>
      </div>
    </div>
  );
}

function Field({ label, required, error, full, children }) {
  return (
    <div className={"form-field" + (full ? " form-field-full" : "")}>
      <label className="form-label">
        {label}
        {required && <span className="form-required">*</span>}
      </label>
      {children}
      {error && <p className="form-error">{error}</p>}
    </div>
  );
}