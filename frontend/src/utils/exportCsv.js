export const exportToCsv = (filename, data) => {
  if (!data || !data.length) {
    return;
  }

  // Define headers and map to fields
  const headers = [
    { label: 'Mã hệ thống', key: 'systemCode' },
    { label: 'Tên sản phẩm', key: 'name' },
    { label: 'Chất liệu', key: 'material' },
    { label: 'Tiêu chuẩn', key: 'specs.sizeNote' },
    { label: 'Kích thước (DxRxC)', key: 'specs.size' },
    { label: 'Tải trọng tĩnh', key: 'specs.staticLoad' },
    { label: 'Tải trọng động', key: 'specs.dynamicLoad' },
    { label: 'Đơn giá', key: 'price' },
    { label: 'Tồn kho', key: 'stock' },
    { label: 'Trạng thái', key: 'status' },
  ];

  // Helper to extract nested properties (e.g. specs.size)
  const getNestedProp = (obj, path) => {
    return path.split('.').reduce((acc, part) => acc && acc[part], obj);
  };

  // Convert data to CSV format
  const csvRows = [];
  
  // Header row
  csvRows.push(headers.map(h => `"${h.label}"`).join(','));

  // Data rows
  for (const row of data) {
    const values = headers.map(header => {
      const val = getNestedProp(row, header.key);
      const escaped = (val === null || val === undefined) ? '' : String(val).replace(/"/g, '""');
      return `"${escaped}"`;
    });
    csvRows.push(values.join(','));
  }

  // Create a Blob with UTF-8 encoding (adding BOM for Excel compatibility)
  const csvContent = '\uFEFF' + csvRows.join('\n');
  const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
  
  // Create download link
  const link = document.createElement('a');
  const url = URL.createObjectURL(blob);
  link.setAttribute('href', url);
  link.setAttribute('download', filename);
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};
