/** Lưu 1 Blob (vd file Excel từ BE) xuống máy người dùng. */
export function downloadBlob(blob, filename) {
  const url = window.URL.createObjectURL(blob instanceof Blob ? blob : new Blob([blob]))
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  a.remove()
  window.URL.revokeObjectURL(url)
}
